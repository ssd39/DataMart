package com.my.shared_data.schema

import org.tessellation.currency.dataApplication.DataCalculatedState
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.schema.address.Address

@derive(decoder, encoder)
final case class CalculatedState(
  dataRecords: Map[Address, DataRecordParent]
) extends DataCalculatedState

object CalculatedState {
  val genesis: CalculatedState = CalculatedState(Map.empty)
}
