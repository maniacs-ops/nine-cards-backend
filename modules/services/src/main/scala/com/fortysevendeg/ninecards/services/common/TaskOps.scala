package com.fortysevendeg.ninecards.services.common

import cats.free.Free
import com.fortysevendeg.ninecards.services.free.algebra.DBResult.DBOps
import com.fortysevendeg.ninecards.services.persistence.PersistenceExceptions.PersistenceException
import doobie.imports.{ ConnectionIO, Transactor }
import scalaz.concurrent.Task
import scalaz.{ -\/, \/- }

object TaskOps {

  implicit def liftFTask[F[_], A](t: Task[A])(implicit dbOps: DBOps[F]): Free[F, A] = t.liftF[F]

  implicit class TaskOps[A](task: Task[A]) {
    def liftF[F[_]](implicit dbOps: DBOps[F]): cats.free.Free[F, A] = task.attemptRun match {
      case \/-(value) ⇒ dbOps.success(value)
      case -\/(e) ⇒
        dbOps.failure(
          PersistenceException(
            message = "An error was found while accessing to database",
            cause   = Option(e)
          )
        )
    }
  }

  implicit class ConnectionIOOps[A](c: ConnectionIO[A]) {
    def liftF[F[_]](implicit dbOps: DBOps[F], transactor: Transactor[Task]): cats.free.Free[F, A] =
      transactor.trans(c).attemptRun match {
        case \/-(value) ⇒ dbOps.success(value)
        case -\/(e) ⇒
          dbOps.failure(
            PersistenceException(
              message = "An error was found while accessing to database",
              cause   = Option(e)
            )
          )
      }
  }

}
