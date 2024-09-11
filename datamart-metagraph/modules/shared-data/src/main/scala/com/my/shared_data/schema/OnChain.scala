package com.my.shared_data.schema

import org.tessellation.currency.dataApplication.DataOnChainState
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.schema.address.Address

@derive(decoder, encoder)
final case class OnChain(
  users: Map[Address, UserRecord],
) extends DataOnChainState {
  def filterUsersWithDataRequests: Map[Address, UserRecord] = {
    users.filter { case (_, userRecord) => userRecord.dataRequests.nonEmpty }
  }
}

object OnChain {
  val genesis: OnChain = OnChain(Map.empty)
}
