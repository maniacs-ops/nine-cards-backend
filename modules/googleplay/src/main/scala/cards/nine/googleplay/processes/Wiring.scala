package cards.nine.googleplay.processes

import cards.nine.commons.config.NineCardsConfig._
import cards.nine.googleplay.processes.withTypes.{ HttpToTask, RedisToTask }
import cards.nine.googleplay.service.free.algebra.{ Cache, GoogleApi, WebScraper }
import cards.nine.googleplay.service.free.interpreter._
import cards.nine.googleplay.service.free.interpreter.cache.CacheInterpreter
import cats._
import cats.data.Coproduct
import com.redis.RedisClientPool
import org.http4s.client.blaze.PooledHttp1Client

import scalaz.concurrent.Task

object Wiring {

  private[this] val apiHttpClient = PooledHttp1Client()
  private[this] val webHttpClient = PooledHttp1Client()

  val redisClientPool: RedisClientPool = new RedisClientPool(
    host   = nineCardsConfiguration.redis.host,
    port   = nineCardsConfiguration.redis.port,
    secret = nineCardsConfiguration.redis.secret
  )

  val cacheInt: Cache.Ops ~> Task = {
    val toTask = new RedisToTask(redisClientPool)
    CacheInterpreter andThen toTask
  }

  val googleApiInt: GoogleApi.Ops ~> Task = {
    val interp = new googleapi.Interpreter(nineCardsConfiguration.google.play.api)
    val toTask = new HttpToTask(apiHttpClient)
    interp andThen toTask
  }

  val webScrapperInt: WebScraper.Ops ~> Task = {
    val interp = new webscrapper.Interpreter(nineCardsConfiguration.google.play.web)
    val toTask = new HttpToTask(webHttpClient)
    interp andThen toTask
  }

  type GooglePlayAppC01[A] = Coproduct[GoogleApi.Ops, WebScraper.Ops, A]
  type GooglePlayApp[A] = Coproduct[Cache.Ops, GooglePlayAppC01, A]

  val interpretersC01: GooglePlayAppC01 ~> Task = googleApiInt or webScrapperInt
  val interpreters: GooglePlayApp ~> Task = cacheInt or interpretersC01

  val appCardService: AppServiceByProcess = AppServiceByProcess(
    redisPool     = redisClientPool,
    apiHttpClient = apiHttpClient,
    webHttpClient = webHttpClient
  )

  def shutdown(): Unit = {
    apiHttpClient.shutdownNow
    webHttpClient.shutdownNow
    redisClientPool.close
  }

}