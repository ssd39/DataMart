package com.my.shared_data.app

import scala.concurrent.duration._

case class ApplicationConfig(
  http4s:    ApplicationConfig.Http4sConfig,
)

object ApplicationConfig {

  case class Http4sConfig(client: Http4sConfig.Client)

  object Http4sConfig {
    case class Client(timeout: FiniteDuration, idleTime: FiniteDuration)
  }
}
