package com.my.shared_data.lifecycle

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFunctorOps}
import com.my.shared_data.lib.LifecycleSharedFunctions.logger
import com.my.shared_data.lib.Utils.getFirstAddressFromProofs
import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.ext.cats.syntax.next.catsSyntaxNext
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.signature.Signed
import org.tessellation.security.{Hasher, SecurityProvider}
import com.my.shared_data.schema.Updates.DMUpdate
import com.my.shared_data.schema._
import monocle.Monocle.toAppliedFocusOps
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.spec.X509EncodedKeySpec
import java.security.{InvalidKeyException, KeyFactory, NoSuchAlgorithmException, PublicKey, Security}
import java.util.Base64
import javax.crypto.{BadPaddingException, Cipher, IllegalBlockSizeException, NoSuchPaddingException}

trait StateUpdateCombiner[F[_], U, T] {
  def insert(state: T, signedUpdate: Signed[U])(implicit ctx: L0NodeContext[F]): F[T]
}

object StateUpdateCombiner {
  Security.addProvider(new BouncyCastleProvider())
  type TX = DMUpdate
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_]: Async: SecurityProvider: Hasher]: StateUpdateCombiner[F, TX, DS] =
    new StateUpdateCombiner[F, TX, DS] {

      override def insert(state: DS, signedUpdate: Signed[TX])(implicit ctx: L0NodeContext[F]): F[DS] =
        signedUpdate.value match {
          case u: Updates.RegisterUser   => registerUser(Signed(u, signedUpdate.proofs))(state, ctx)
          case u: Updates.CreateDataRequest => createDataRequest(Signed(u, signedUpdate.proofs))(state, ctx)
          case u: Updates.CreateProviderProposal => createProviderProposal(Signed(u, signedUpdate.proofs))(state, ctx)
          case u: Updates.ApproveProposal => approveProposal(Signed(u, signedUpdate.proofs))(state, ctx)
          case u: Updates.SubmitData => submitData(Signed(u, signedUpdate.proofs))(state, ctx)
        }

      private def registerUser(update: Signed[Updates.RegisterUser]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            currentOrdinal <- ctx.getLastCurrencySnapshot
              .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
              .map(_.next)
            _record = UserRecord(
              currentOrdinal,
              0,
              update.orgName,
              update.orgDescription,
              update.link,
              update.logo,
              Map.empty,
              Map.empty
            )
            userAddress <- getFirstAddressFromProofs(update.proofs)
            onchain = inState.onChain
              .focus(_.users)
              .modify(_.updated(userAddress, _record))
            _dataRecord = DataRecordParent(update.pubkey, Map.empty)
            calculated =  inState.calculated.dataRecords.get(userAddress) match {
              case Some(_) => inState.calculated
              case _ => inState.calculated.focus (_.dataRecords).modify(_.updated (userAddress, _dataRecord))
            }
          } yield DataState(onchain, calculated)

      private def createDataRequest(update: Signed[Updates.CreateDataRequest]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            currentOrdinal <- ctx.getLastCurrencySnapshot
              .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
              .map(_.next)

            userAddress <- getFirstAddressFromProofs(update.proofs)
            _record = DataRequestRecord(
              currentOrdinal,
              update.description,
              update.schema,
              userAddress
            )
            id <- DataRequestRecord.generateId(DataRequestID(update.schema, userAddress))
            userRecord = inState.onChain.users.getOrElse(userAddress, UserRecord.empty())
            dataRequests =  userRecord.dataRequests.updated(id, _record)
            updatedUserRecord = userRecord.copy(dataRequests = dataRequests)
            onchain = inState.onChain
              .focus(_.users)
              .modify(_.updated(userAddress, updatedUserRecord))
            calculated = inState.calculated
          } yield DataState(onchain, calculated)

      private def createProviderProposal(update: Signed[Updates.CreateProviderProposal]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            currentOrdinal <- ctx.getLastCurrencySnapshot
              .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
              .map(_.next)

            userAddress <- getFirstAddressFromProofs(update.proofs)
            userRecord = inState.onChain.users.getOrElse(userAddress, UserRecord.empty())
            _record = ProviderProposalRecord(
              currentOrdinal,
              userAddress,
              userRecord.orgName,
              userRecord.link,
              userRecord.orgDescription,
              update.description,
              update.id,
              update.amount,
              false
            )
            id <- ProviderProposalRecord.generateId(ProviderProposalID(userAddress, update.description, update.id))
            buyerRecord = inState.onChain.users.getOrElse(update.buyer, UserRecord.empty())
            proposals = buyerRecord.proposals.updated(id, _record)
            updatedBuyerRecord = buyerRecord.copy(proposals = proposals)
            onchain = inState.onChain
              .focus(_.users)
              .modify(_.updated(update.buyer, updatedBuyerRecord))
            calculated = inState.calculated
          } yield DataState(onchain, calculated)

      private def approveProposal(update: Signed[Updates.ApproveProposal]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            userAddress <- getFirstAddressFromProofs(update.proofs)
            userRecord = inState.onChain.users.getOrElse(userAddress, UserRecord.empty())
            proposalRecord = userRecord.proposals.getOrElse(update.id, ProviderProposalRecord.empty(userAddress)).copy(isApproved = true)
            updatedUserRecord = userRecord.focus(_.proposals).modify(_.updated(update.id, proposalRecord))
            onchain = inState.onChain
              .focus(_.users)
              .modify(_.updated(userAddress, updatedUserRecord))
            calculated = inState.calculated
          } yield DataState(onchain, calculated)

      def encrypt(key: PublicKey, plaintext: Array[Byte]): Array[Byte] = {
        try {
          val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC")
          cipher.init(Cipher.ENCRYPT_MODE, key)
          cipher.doFinal(plaintext)
        } catch {
          case e: NoSuchAlgorithmException => throw new RuntimeException(e)
          case e: NoSuchPaddingException => throw new RuntimeException(e)
          case e: InvalidKeyException => throw new RuntimeException(e)
          case e: IllegalBlockSizeException => throw new RuntimeException(e)
          case e: BadPaddingException => throw new RuntimeException(e)
        }
      }

      private def submitData(update: Signed[Updates.SubmitData]): (DS, L0NodeContext[F]) => F[DS] =
        (inState: DS, ctx: L0NodeContext[F]) =>
          for {
            currentOrdinal <- ctx.getLastCurrencySnapshot
              .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
              .map(_.next)
            userAddress <- getFirstAddressFromProofs(update.proofs)

            buyerPubKey = inState.calculated.dataRecords.getOrElse(update.buyer, DataRecordParent.empty()).userPubKey.replace("-----BEGIN PUBLIC KEY-----", "")
              .replace("-----END PUBLIC KEY-----", "")
              .replaceAll("\\s", "")
            providerPubKey = update.pubkey.replace("-----BEGIN PUBLIC KEY-----", "")
              .replace("-----END PUBLIC KEY-----", "")
              .replaceAll("\\s", "")
            response = requests.get(update.dataSource, headers = Map("token" -> update.token))
            encByteProvider = encrypt(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder.decode(providerPubKey))), response.bytes)
            encByteBuyer = encrypt(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder.decode(buyerPubKey))), response.bytes)

            _providerDataRecord = DataRecord(
              currentOrdinal,
              encByteProvider,
              update.id,
              update.buyer,
              userAddress
            )
            _buyerDataRecord = DataRecord(
              currentOrdinal,
              encByteBuyer,
              update.id,
              update.buyer,
              userAddress
            )

            _providerRecordId <- DataRecord.generateId(DataRecordID(update.id, update.buyer, userAddress, encByteProvider))
            _updatedProviderRecord = inState.calculated.dataRecords.getOrElse(userAddress, DataRecordParent(update.pubkey, Map.empty)).focus(_.dataRecords).modify(_.updated(_providerRecordId, _providerDataRecord))

            _buyerRecordId <- DataRecord.generateId(DataRecordID(update.id, update.buyer, userAddress, encByteBuyer))
            _updateBuyerRecord = inState.calculated.dataRecords.getOrElse(update.buyer, DataRecordParent.empty()).focus(_.dataRecords).modify(_.updated(_buyerRecordId, _buyerDataRecord))

            calculated = inState.calculated.focus(_.dataRecords).modify(_.updated(userAddress, _updatedProviderRecord)).focus(_.dataRecords).modify(_.updated(update.buyer, _updateBuyerRecord))
            onchain = inState.onChain
          } yield DataState(onchain, calculated)
    }
}
