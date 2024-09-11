package com.my.shared_data.lifecycle

import cats.data.Validated
import cats.effect.Async
import cats.syntax.validated._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.my.shared_data.lib.LifecycleSharedFunctions.logger
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import com.my.shared_data.schema.Updates.{RegisterUser, SubmitData}
import com.my.shared_data.schema.{OnChain, ProviderProposalRecord}
import com.networknt.schema.{JsonSchemaFactory, SpecVersion}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.schema.address.Address

import java.net.URL
import scala.util.Try

object ValidatorRules {
  def CheckValidOrg[F[_]: Async](update: RegisterUser): F[DataApplicationValidationErrorOr[Unit]] =
    Async[F].delay {
      Validated.condNec(update.orgName != "", (), Errors.OrgNameNotProvided)
    }
  def CheckValidLink(update: RegisterUser): DataApplicationValidationErrorOr[Unit] =
    Validated.condNec(Try(new URL(update.link).toURI).isSuccess, (), Errors.OrgLinkNotProvided)
  def CheckUserAlreadyExists(user: Address, state: OnChain): DataApplicationValidationErrorOr[Unit] =
    Validated.condNec(!state.users.contains(user), (), Errors.UserAlreadyExists)
  def CheckUserExists(user: Address, state: OnChain): DataApplicationValidationErrorOr[Unit] =
    Validated.condNec(state.users.contains(user), (), Errors.UserDoesNotExists)
  def CheckIsValidJsonSchema[F[_]: Async](schema: String): F[DataApplicationValidationErrorOr[Unit]] =
    Async[F].delay {
      Validated.condNec(Try(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(schema)).isSuccess, (), Errors.JsonSchemStringInvalid)
    }
  def CheckDataRequestExists[F[_]: Async](userAddress: Address, id: String, state: OnChain): F[DataApplicationValidationErrorOr[Unit]] = {
    Async[F].delay {
      state.users.get(userAddress) match {
        case Some(user) => Validated.condNec(user.dataRequests.contains(id), (), Errors.DataRequestDoesNotExists)
        case _ => Validated.condNec(false, (), Errors.UserDoesNotExists)
      }
    }
  }
  def CheckProposalExists(userAddress: Address, id: String, state: OnChain): DataApplicationValidationErrorOr[Unit] = {
    state.users.get(userAddress) match {
      case Some(user) => Validated.condNec(user.proposals.contains(id), (), Errors.ProposalDoesNotExists)
      case _ => Validated.condNec(false, (), Errors.UserDoesNotExists)
    }
  }

  def CheckProposalApproved[F[_]: Async](userAddress: Address, id: String, state: OnChain): F[DataApplicationValidationErrorOr[Unit]]= {
    Async[F].delay {
      state.users.get(userAddress) match {
        case Some(user) => Validated.condNec(user.proposals.getOrElse(id, ProviderProposalRecord.empty(userAddress)).isApproved, (), Errors.ProposalDoesNotExists)
        case _ => Validated.condNec(false, (), Errors.UserDoesNotExists)
      }
    }
  }

  def DataDoesNotMatchTheSchema(update: SubmitData, state: OnChain): DataApplicationValidationErrorOr[Unit] =
      state.users.get(update.buyer) match {
          case Some(user) => {
            user.dataRequests.get(user.proposals.getOrElse(update.id, ProviderProposalRecord.empty(update.buyer)).dataRequestId) match {
              case  Some(dataRequest) => {
                try {
                  val schema = dataRequest.schema
                  val response = requests.get(update.dataSource,   headers = Map("token" -> update.token))
                  val body = response.text()
                  val schemaInstance = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(schema)
                  val mapper = new ObjectMapper()
                  val actualObj = mapper.readTree(body)
                  Validated.condNec(schemaInstance.validate(actualObj).size() <= 0, (), Errors.FailedToValidateData)
                } catch {
                  case _ : Exception => Validated.condNec(false, (), Errors.FailedToValidateData)
                }
              }
              case _ => Validated.condNec(false, (), Errors.ProposalDoesNotExists)
            }
          }
          case _ => Validated.condNec(false, (), Errors.UserDoesNotExists)
    }

  def valid[F[_] : Async](): F[DataApplicationValidationErrorOr[Unit]] = {
    Async[F].delay {
      ().validNec[DataApplicationValidationError]
    }
  }

  object Errors {
    @derive(decoder, encoder)
    case object FailedToValidateData extends DataApplicationValidationError {
      val message = s"Not able to validate the data!"
    }

    @derive(decoder, encoder)
    case object ProposalDoesNotApproved extends DataApplicationValidationError {
      val message = s"Given proposal doesn't approved!"
    }

    @derive(decoder, encoder)
    case object ProposalDoesNotExists extends DataApplicationValidationError {
      val message = s"Given proposal doesn't exists!"
    }

    @derive(decoder, encoder)
    case object DataRequestDoesNotExists extends DataApplicationValidationError {
      val message = s"Invalid data request provided!"
    }

    @derive(decoder, encoder)
    case object JsonSchemStringInvalid extends DataApplicationValidationError {
      val message = s"Given data schema is not valid!"
    }

    @derive(decoder, encoder)
    case object UserDoesNotExists extends DataApplicationValidationError {
      val message = s"User is not registered please register first!"
    }

    @derive(decoder, encoder)
    case object UserAlreadyExists extends DataApplicationValidationError {
      val message = s"Failed to register user, user already exists."
    }

    @derive(decoder, encoder)
    case object OrgNameNotProvided extends DataApplicationValidationError {
      val message = s"Failed to register user, missing organisation name."
    }

    @derive(decoder, encoder)
    case object OrgLinkNotProvided extends DataApplicationValidationError {
      val message = s"Failed to register user, missing organisation link."
    }

    @derive(decoder, encoder)
    case object RecordAlreadyExists extends DataApplicationValidationError {
      val message = s"Failed to create task, previous record found."
    }

    @derive(decoder, encoder)
    case object RecordDoesNotExist extends DataApplicationValidationError {
      val message = s"Failed to create event, no previous record found."
    }

    @derive(decoder, encoder)
    case object TaskDueDateInPast extends DataApplicationValidationError {
      val message = s"Task due date must be at least 1 hour in the future."
    }

    @derive(decoder, encoder)
    case object InvalidStatusForModify extends DataApplicationValidationError {
      val message = s"Invalid status found in ModifyTask. Use RemoveTask to archive."
    }
  }
}
