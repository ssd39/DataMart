package com.my.metagraph_l0

import cats.data.NonEmptySet
import cats.effect.Async
import cats.syntax.all._
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxValidatedIdBinCompat0, toFoldableOps}
import com.my.shared_data.lib.Utils.getFirstAddressFromProofs
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, DataState}
import org.tessellation.security.{Hasher, SecurityProvider}
import org.tessellation.security.signature.{Signed, signature}
import com.my.shared_data.lib.UpdateValidator
import com.my.shared_data.lifecycle.ValidatorRules
import com.my.shared_data.schema.Updates.{ApproveProposal, CreateDataRequest, CreateProviderProposal, DMUpdate, RegisterUser, SubmitData}
import com.my.shared_data.schema.{CalculatedState, OnChain, Updates}
import eu.timepit.refined.collection.NonEmpty
import org.tessellation.ext.cats.data
import org.tessellation.schema.address.Address

trait ML0Validator[F[_], U, T] extends UpdateValidator[F, U, T]

object ML0Validator {

  type TX = DMUpdate
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_]: Async: SecurityProvider: Hasher]: ML0Validator[F, Signed[TX], DS] =

    new ML0Validator[F, Signed[TX], DS] {
      override def verify(state: DS, signedUpdate: Signed[TX]): F[DataApplicationValidationErrorOr[Unit]] = {
        signedUpdate.value match {
          case u: Updates.RegisterUser   => registerUser(Signed(u, signedUpdate.proofs))(state.onChain)
          case u: Updates.CreateDataRequest => createDataRequest(Signed(u, signedUpdate.proofs))(state.onChain)
          case u: Updates.CreateProviderProposal => createProviderProposal(Signed(u, signedUpdate.proofs))(state.onChain)
          case u: Updates.ApproveProposal => createApproveProposal(Signed(u, signedUpdate.proofs))(state.onChain)
          case u: Updates.SubmitData => submitData(Signed(u, signedUpdate.proofs))(state.onChain)
        }
      }

      private def registerUser(
        signedUpdate   : Signed[RegisterUser],
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          userId <- getFirstAddressFromProofs(signedUpdate.proofs)
          res1 = ValidatorRules.CheckUserAlreadyExists(userId, state)
        } yield List(res1).combineAll

      private def createDataRequest(
        signedUpdate: Signed[CreateDataRequest],
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          userId <- getFirstAddressFromProofs(signedUpdate.proofs)
          res1 = ValidatorRules.CheckUserExists(userId, state)
        } yield List(res1).combineAll

      private def createProviderProposal(
          signedUpdate: Signed[CreateProviderProposal],
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          userId <- getFirstAddressFromProofs(signedUpdate.proofs)
          res1 = ValidatorRules.CheckUserExists(userId, state)
        } yield List(res1).combineAll

      private def createApproveProposal(
        signedUpdate: Signed[ApproveProposal],
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          userId <- getFirstAddressFromProofs(signedUpdate.proofs)
          res1 = ValidatorRules.CheckProposalExists(userId, signedUpdate.id, state)
        } yield List(res1).combineAll

      private def submitData(
        signedUpdate: Signed[SubmitData],
     ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          res1 <- ValidatorRules.valid()
        } yield List(res1).combineAll
    }
}
