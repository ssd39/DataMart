package com.my.shared_data.lib

import org.tessellation.currency.dataApplication.DataUpdate

import com.my.shared_data.schema.Updates.DMUpdate

import io.circe._
import io.circe.syntax.EncoderOps

object CirceOps {

  object implicits {

    implicit val dataUpateEncoder: Encoder[DataUpdate] = {
      case event: DMUpdate => event.asJson
      case _                 => Json.Null
    }

    implicit val dataUpdateDecoder: Decoder[DataUpdate] = (c: HCursor) => c.as[DMUpdate]
  }
}
