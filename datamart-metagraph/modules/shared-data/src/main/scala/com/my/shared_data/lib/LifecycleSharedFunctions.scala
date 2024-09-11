package com.my.shared_data.lib

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger


object LifecycleSharedFunctions {
  def logger[F[_] : Async]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromName[F]("LifecycleSharedFunctions")
}