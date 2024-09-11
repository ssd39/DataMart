package com.my.shared_data.schema

import cats.effect.Async
import cats.implicits.toFunctorOps
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address
import org.tessellation.security.Hasher
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Encoder
import io.circe.generic.semiauto._

@derive(decoder, encoder)
final case class UserRecord(
  creationOrdinal: SnapshotOrdinal,
  balance: Long,
  orgName: String,
  orgDescription: String,
  link: String,
  logo: String,
  dataRequests: Map[String, DataRequestRecord],
  proposals: Map[String, ProviderProposalRecord]
)

@derive(decoder, encoder)
final case class DataRequestRecord(
  creationOrdinal: SnapshotOrdinal,
  description: String,
  schema: String,
  buyer: Address
)

@derive(decoder, encoder)
final case class DataRequestID(
  schema: String,
  buyer: Address
)

@derive(decoder, encoder)
final case class ProviderProposalRecord(
  creationOrdinal: SnapshotOrdinal,
  provider: Address,
  orgName: String,
  orgLink: String,
  orgDescription: String,
  description: String,
  dataRequestId: String,
  amount: Long,
  isApproved: Boolean
)

@derive(decoder, encoder)
final case class ProviderProposalID(
   provider: Address,
   description: String,
   dataRequestId: String,
)

@derive(decoder, encoder)
final case class DataRecordParent(
    userPubKey: String,
    dataRecords: Map[String, DataRecord]
)

@derive(decoder, encoder)
final case class DataRecord(
    creationOrdinal: SnapshotOrdinal,
    data: Array[Byte],
    id: String,
    buyer: Address,
    sender: Address
)

@derive(decoder, encoder)
final case class DataRecordID(
  id: String,
  buyer: Address,
  sender: Address,
  data: Array[Byte]
)

object UserRecord {
  def empty(): UserRecord = UserRecord(SnapshotOrdinal.MinValue, 0, "", "", "", "", Map.empty, Map.empty)
}

object DataRecordParent {
  def empty(): DataRecordParent = DataRecordParent("" , Map.empty)
}

object DataRecord {
  def generateId[F[_] : Async : Hasher](dataRecord: DataRecordID): F[String] =
    Hasher[F].hash(dataRecord).map(_.value)
}

object ProviderProposalRecord {
  def empty(userAddress: Address): ProviderProposalRecord = ProviderProposalRecord(SnapshotOrdinal.MinValue, userAddress, "", "", "", "", "", 0, false)
  def generateId[F[_]: Async: Hasher](proposalReq: ProviderProposalID): F[String] =
    Hasher[F].hash(proposalReq).map(_.value)
}

object DataRequestRecord {
  def empty(userAddress: Address): DataRequestRecord = DataRequestRecord(SnapshotOrdinal.MinValue, "", "", userAddress)
  def generateId[F[_]: Async: Hasher](dataReq: DataRequestID): F[String] =
    Hasher[F].hash(dataReq).map(_.value)
}

