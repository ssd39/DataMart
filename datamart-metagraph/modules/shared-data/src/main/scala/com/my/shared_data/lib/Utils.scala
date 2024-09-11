package com.my.shared_data.lib

import cats.data.NonEmptySet
import cats.effect.Async
import org.tessellation.schema.address.Address
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.signature.SignatureProof

object Utils {
  def getFirstAddressFromProofs[F[_] : Async : SecurityProvider](proofs: NonEmptySet[SignatureProof]): F[Address] =
    proofs.head.id.toAddress[F]
}