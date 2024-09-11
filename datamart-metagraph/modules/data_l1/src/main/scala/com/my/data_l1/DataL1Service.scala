package com.my.data_l1

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.dataApplication.dataApplication.{DataApplicationBlock, DataApplicationValidationErrorOr}
import org.tessellation.json.JsonSerializer
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.Hasher
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed
import org.tessellation.security.signature.Signed._
import com.my.data_l1.DataL1NodeContext.syntax.DataL1NodeContextOps
import com.my.shared_data.lib.CirceOps.implicits._
import com.my.shared_data.schema.Updates.DMUpdate
import com.my.shared_data.schema.{CalculatedState, OnChain}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

import java.nio.charset.StandardCharsets
import java.util.Base64

object DataL1Service {

  def make[F[+_]: Async: JsonSerializer: Hasher]: F[BaseDataApplicationL1Service[F]] =
    for {
      validator <- Async[F].pure(DataL1Validator.make[F])
      dataApplicationL1Service = makeBaseApplicationL1Service(validator)
    } yield dataApplicationL1Service

  private def makeBaseApplicationL1Service[F[+_]: Async: JsonSerializer](
    validator: DataL1Validator[F, DMUpdate, OnChain]
  ): BaseDataApplicationL1Service[F] =
    BaseDataApplicationL1Service[F, DMUpdate, OnChain, CalculatedState](
      new DataApplicationL1Service[F, DMUpdate, OnChain, CalculatedState] {

        override def serializeState(state: OnChain): F[Array[Byte]] =
          JsonSerializer[F].serialize[OnChain](state)

        override def deserializeState(bytes: Array[Byte]): F[Either[Throwable, OnChain]] =
          JsonSerializer[F].deserialize[OnChain](bytes)

        override def serializeUpdate(update: DMUpdate): F[Array[Byte]] =
          Async[F].delay {
            val encoder = Base64.getEncoder
            val data_sign_prefix = "\u0019Constellation Signed Data:\n"

            val updateBytes = update.asJson.deepDropNullValues.noSpaces.getBytes(StandardCharsets.UTF_8)
            val encodedBytes = encoder.encode(updateBytes)

            val encodedString = new String(encodedBytes, "UTF-8")
            val completeString = s"$data_sign_prefix${encodedString.length}\n$encodedString"
            completeString.getBytes(StandardCharsets.UTF_8)
          }

        override def deserializeUpdate(bytes: Array[Byte]): F[Either[Throwable, DMUpdate]] =
          JsonSerializer[F].deserialize[DMUpdate](bytes)

        override def serializeBlock(block: Signed[DataApplicationBlock]): F[Array[Byte]] =
          JsonSerializer[F].serialize[Signed[DataApplicationBlock]](block)

        override def deserializeBlock(bytes: Array[Byte]): F[Either[Throwable, Signed[DataApplicationBlock]]] =
          JsonSerializer[F].deserialize[Signed[DataApplicationBlock]](bytes)

        override def serializeCalculatedState(calculatedState: CalculatedState): F[Array[Byte]] =
          JsonSerializer[F].serialize[CalculatedState](calculatedState)

        override def deserializeCalculatedState(bytes: Array[Byte]): F[Either[Throwable, CalculatedState]] =
          JsonSerializer[F].deserialize[CalculatedState](bytes)

        override def dataEncoder: Encoder[DMUpdate] = implicitly(Encoder[DMUpdate])

        override def dataDecoder: Decoder[DMUpdate] = implicitly(Decoder[DMUpdate])

        override def calculatedStateEncoder: Encoder[CalculatedState] = implicitly(Encoder[CalculatedState])

        override def calculatedStateDecoder: Decoder[CalculatedState] = implicitly(Decoder[CalculatedState])

        override val signedDataEntityDecoder: EntityDecoder[F, Signed[DMUpdate]] = circeEntityDecoder

        override def getCalculatedState(implicit
          context: L1NodeContext[F]
        ): F[(SnapshotOrdinal, CalculatedState)] =
          (SnapshotOrdinal.MinValue, CalculatedState.genesis).pure[F]

        override def setCalculatedState(ordinal: SnapshotOrdinal, state: CalculatedState)(implicit
          context: L1NodeContext[F]
        ): F[Boolean] = true.pure[F]

        override def hashCalculatedState(state: CalculatedState)(implicit context: L1NodeContext[F]): F[Hash] =
          Hash.empty.pure[F]

        override def validateData(
          state:   DataState[OnChain, CalculatedState],
          updates: NonEmptyList[Signed[DMUpdate]]
        )(implicit context: L1NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          ().validNec[DataApplicationValidationError].pure[F]

        override def validateUpdate(
          update: DMUpdate
        )(implicit context: L1NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] =
          context.getOnChainState.flatMap {
            _.fold(
              err => err.invalidNec[Unit].pure[F],
              onchain => validator.verify(onchain, update)
            )
          }

        override def combine(
          state:   DataState[OnChain, CalculatedState],
          updates: List[Signed[DMUpdate]]
        )(implicit context: L1NodeContext[F]): F[DataState[OnChain, CalculatedState]] = state.pure[F]

        override def routes(implicit context: L1NodeContext[F]): HttpRoutes[F] =
          new DataL1CustomRoutes[F].public
      }
    )
}
