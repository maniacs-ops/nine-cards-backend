package com.fortysevendeg.ninecards.services.free.algebra

import cats.data.Xor
import cats.free.{ Free, Inject }
import com.fortysevendeg.ninecards.services.free.domain.GooglePlay._

object GooglePlay {

  sealed trait Ops[A]

  case class Resolve(packageName: String, auth: AuthParams)
    extends Ops[UnresolvedApp Xor AppCard]

  case class ResolveMany(packageNames: Seq[String], auth: AuthParams)
    extends Ops[AppsCards]

  class Services[F[_]](implicit I: Inject[Ops, F]) {

    def resolve(packageName: String, auth: AuthParams): Free[F, UnresolvedApp Xor AppCard] =
      Free.inject[Ops, F](Resolve(packageName, auth))

    def resolveMany(packageNames: Seq[String], auth: AuthParams): Free[F, AppsCards] =
      Free.inject[Ops, F](ResolveMany(packageNames, auth))
  }

  object Services {

    implicit def services[F[_]](implicit I: Inject[Ops, F]): Services[F] =
      new Services

  }
}

