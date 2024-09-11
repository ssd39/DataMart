package com.my.shared_data.schema

import org.tessellation.currency.dataApplication.DataUpdate
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.schema.address.Address

object Updates {

  @derive(decoder, encoder)
  sealed abstract class DMUpdate extends DataUpdate

  @derive(decoder, encoder)
  final case class RegisterUser(
    orgName: String,
    orgDescription: String,
    link: String,
    logo: String,
    pubkey: String
  ) extends DMUpdate

  @derive(decoder, encoder)
  final case class CreateDataRequest(
    schema: String,
    description: String,

    callBackWebhook: Option[String]
  ) extends DMUpdate

  @derive(decoder, encoder)
  final case class CreateProviderProposal(
    id: String,
    description: String,
    amount: Long,
    buyer: Address
  ) extends DMUpdate

  @derive(decoder, encoder)
  final case class ApproveProposal(
    id: String,
  ) extends DMUpdate

  @derive(decoder, encoder)
  final case class SubmitData(
   id: String,
   buyer: Address,
   dataSource: String,
   token: String,
   pubkey: String
  ) extends DMUpdate
}
