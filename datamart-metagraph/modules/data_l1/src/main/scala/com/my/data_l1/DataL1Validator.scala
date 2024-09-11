package com.my.data_l1

import cats.effect.Async
import cats.implicits.{toFoldableOps, toFunctorOps}
import cats.syntax.applicative._
import cats.syntax.validated._
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.security.Hasher
import com.my.shared_data.lib.UpdateValidator
import com.my.shared_data.lifecycle.ValidatorRules
import com.my.shared_data.schema.Updates.DMUpdate
import com.my.shared_data.schema.{OnChain, Updates}


trait DataL1Validator[F[_], U, T] extends UpdateValidator[F, U, T]

object DataL1Validator {

  def make[F[_]: Async: Hasher]: DataL1Validator[F, DMUpdate, OnChain] =
    new DataL1Validator[F, DMUpdate, OnChain] {

      override def verify(state: OnChain, update: DMUpdate): F[DataApplicationValidationErrorOr[Unit]] =
        update match {
          case u: Updates.RegisterUser   => registerUser(u)(state)
          case u: Updates.CreateDataRequest => createDataRequest(u)(state)
          case u: Updates.CreateProviderProposal => createProviderProposal(u)(state)
          case u: Updates.ApproveProposal => createApproveProposal()(state)
          case u: Updates.SubmitData => submitData(u)(state)
        }

      private def registerUser(
        update: Updates.RegisterUser
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          res1 <- ValidatorRules.CheckValidOrg(update)
          res2 = ValidatorRules.CheckValidLink(update)
        } yield List(res1, res2).combineAll

      private def createDataRequest(
        update: Updates.CreateDataRequest
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          res1 <- ValidatorRules.CheckIsValidJsonSchema(update.schema)
        } yield List(res1).combineAll


      private def createProviderProposal(
        update: Updates.CreateProviderProposal
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          res1 <-  ValidatorRules.CheckDataRequestExists(update.buyer ,update.id, state)
        } yield List(res1).combineAll

      private def createApproveProposal(): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          res1 <- ValidatorRules.valid()
        } yield List(res1).combineAll

      private def submitData(
        update: Updates.SubmitData
      ): OnChain => F[DataApplicationValidationErrorOr[Unit]] = (state: OnChain) =>
        for {
          res1 <-  ValidatorRules.CheckProposalApproved(update.buyer, update.id, state)
          res2 = ValidatorRules.DataDoesNotMatchTheSchema(update, state)
        } yield List(res1, res2).combineAll
    }
}
