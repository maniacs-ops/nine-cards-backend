package cards.nine.services.free.interpreter

import akka.actor.ActorSystem
import cards.nine.commons.catscalaz.TaskInstances
import cards.nine.commons.config.NineCardsConfig._
import cards.nine.commons.redis.{ RedisClient, RedisOps }
import cards.nine.services.free.algebra._
import cards.nine.services.free.interpreter.analytics.{ Services ⇒ AnalyticsServices }
import cards.nine.services.free.interpreter.collection.{ Services ⇒ CollectionServices }
import cards.nine.services.free.interpreter.country.{ Services ⇒ CountryServices }
import cards.nine.services.free.interpreter.firebase.{ Services ⇒ FirebaseServices }
import cards.nine.services.free.interpreter.googleapi.{ Services ⇒ GoogleApiServices }
import cards.nine.services.free.interpreter.googleoauth.{ Services ⇒ GoogleOAuthServices }
import cards.nine.services.free.interpreter.googleplay.{ Services ⇒ GooglePlayServices }
import cards.nine.services.free.interpreter.ranking.{ Services ⇒ RankingServices }
import cards.nine.services.free.interpreter.subscription.{ Services ⇒ SubscriptionServices }
import cards.nine.services.free.interpreter.user.{ Services ⇒ UserServices }
import cards.nine.services.persistence.DatabaseTransactor._
import cats.{ ApplicativeError, ~> }
import doobie.contrib.postgresql.pgtypes._
import doobie.imports._
import scalaz.concurrent.Task
import scredis.{ Client ⇒ ScredisClient }

class Interpreters(implicit A: ApplicativeError[Task, Throwable], T: Transactor[Task]) {

  implicit val system: ActorSystem = ActorSystem("cards-nine-services-redis")
  import scala.concurrent.ExecutionContext.Implicits.global

  val redisClient: RedisClient = ScredisClient(
    host        = nineCardsConfiguration.redis.host,
    port        = nineCardsConfiguration.redis.port,
    passwordOpt = nineCardsConfiguration.redis.secret
  )

  val toTask = new (RedisOps ~> Task) {

    override def apply[A](fa: RedisOps[A]): Task[A] = fa(redisClient)
  }

  val connectionIO2Task = new (ConnectionIO ~> Task) {
    def apply[A](fa: ConnectionIO[A]): Task[A] = fa.transact(T)
  }

  val analyticsInterpreter: (GoogleAnalytics.Ops ~> Task) = AnalyticsServices.services(nineCardsConfiguration.google.analytics)

  val collectionInterpreter: (SharedCollection.Ops ~> Task) = CollectionServices.services.andThen(connectionIO2Task)

  val countryInterpreter: (Country.Ops ~> Task) = CountryServices.services.andThen(connectionIO2Task)

  val firebaseInterpreter: (Firebase.Ops ~> Task) = FirebaseServices.services(nineCardsConfiguration.google.firebase)

  val googleApiInterpreter: (GoogleApi.Ops ~> Task) = GoogleApiServices.services(nineCardsConfiguration.google.api)

  val googleOAuthInterpreter: (GoogleOAuth.Ops ~> Task) = GoogleOAuthServices

  val googlePlayInterpreter: (GooglePlay.Ops ~> Task) = GooglePlayServices.services

  val rankingInterpreter: (Ranking.Ops ~> Task) = RankingServices.services.andThen(toTask)

  val subscriptionInterpreter: (Subscription.Ops ~> Task) = SubscriptionServices.services.andThen(connectionIO2Task)

  val userInterpreter: (User.Ops ~> Task) = UserServices.services.andThen(connectionIO2Task)
}

object Interpreters extends TaskInstances {

  val taskInterpreters = new Interpreters
}
