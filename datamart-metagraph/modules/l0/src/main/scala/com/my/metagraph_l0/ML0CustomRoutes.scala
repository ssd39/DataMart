package com.my.metagraph_l0

import cats.effect.Async
import cats.syntax.all._
import org.tessellation.currency.dataApplication.{DataApplicationValidationError, L0NodeContext}
import org.tessellation.json.JsonSerializer
import org.tessellation.node.shared.ext.http4s.SnapshotOrdinalVar
import com.my.metagraph_l0.ML0NodeContext.syntax._
import com.my.shared_data.lib.{CheckpointService, MetagraphPublicRoutes}
import com.my.shared_data.schema.{CalculatedState, DataRequestRecord, ProviderProposalRecord, UserRecord}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.{EntityEncoder, HttpRoutes, Response}
import org.tessellation.ext.http4s.AddressVar
import org.tessellation.schema.address.Address

@derive(decoder, encoder)
case class DataRequestItem(buyer: Address, proposals: Map[String, ProviderProposalRecord], dataRequests: Map[String, DataRequestRecord], orgName: String, orgDescription: String, link: String, logo: String)

@derive(decoder, encoder)
case class ProposalItem(buyer: Address,  proposals: Map[String, ProviderProposalRecord])

@derive(decoder, encoder)
case class UserDetailItem(orgName: String, orgDescription: String, link: String, logo: String)


class ML0CustomRoutes[F[_]: Async: JsonSerializer](calculatedStateService: CheckpointService[F, CalculatedState])(
  implicit context: L0NodeContext[F]
) extends MetagraphPublicRoutes[F] {

  protected val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "user-record" / AddressVar(userAddress)  =>
      context.getOnChainState.map(_.map(_.users.get(userAddress))).flatMap(_ match {
        case Left(ex) => BadRequest(ex.message)
        case Right(value) => value match {
          case Some(user) => Ok(user)
          case _ => NotFound()
        }
      })

    case GET -> Root / "data-requests" =>
      context.getOnChainState.map(_.map(_.users.filter{ record => record._2.dataRequests.nonEmpty }.map{ record => DataRequestItem(record._1, record._2.proposals, record._2.dataRequests, record._2.orgName, record._2.orgDescription, record._2.link, record._2.logo) })).flatMap(prepareResponse(_))

    case GET -> Root / "user-proposals" / AddressVar(userAddress) =>
      context.getOnChainState.map(_.map(_.users.filter{ record => record._2.proposals.filter(proposal => proposal._2.provider == userAddress).nonEmpty}.map( record => ProposalItem(record._1, record._2.proposals.filter(proposal => proposal._2.provider == userAddress))))).flatMap(prepareResponse(_))

    case GET -> Root / "user-detail" / AddressVar(userAddress) =>
      context.getOnChainState.map(_.map(_.users.getOrElse(userAddress, UserRecord.empty())).map( user => UserDetailItem(user.orgName, user.orgDescription, user.link, user.logo))).flatMap(prepareResponse(_))

    case GET -> Root / "user-data-list" / AddressVar(userAddress) =>
      calculatedStateService.get.map(_.state).map { state => state.dataRecords.get(userAddress) }.flatMap { maybeUserInfo =>
      maybeUserInfo.fold(NotFound())(userInfo => Ok(userInfo))
    }

    case GET -> Root / "snapshot" / "currency" / "latest" =>
      context.getLatestCurrencySnapshot.flatMap(prepareResponse(_))

    case GET -> Root / "snapshot" / "currency" / SnapshotOrdinalVar(ordinal) =>
      context.getCurrencySnapshotAt(ordinal).flatMap(prepareResponse(_))

    case GET -> Root / "snapshot" / "currency" / SnapshotOrdinalVar(ordinal) / "count-updates" =>
      context.countUpdatesInSnapshotAt(ordinal).flatMap(prepareResponse(_))
  }
}
