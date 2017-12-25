package http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import org.slf4j.LoggerFactory


case class CollectHttpRequest(
                               method: String,
                               url: String,
                               headers: Map[String, String],
                               success: Boolean,
                               startTime: Long,
                               endTime: Long,
                               code: Int = -1
                             ) {
  override def toString = s"[$code] $method $url time:${(endTime - startTime).toDouble / 1000}"
}

object CollectRequestInfo {
  private val logger = LoggerFactory.getLogger("http")

  def collectRequestInfo(route: Route): Route = (context: RequestContext) => {
    val startTime = System.currentTimeMillis()
    route.andThen { rt: Future[RouteResult] =>
      val default = CollectHttpRequest(
        method = context.request.method.value,
        url = context.request.uri.toString(),
        headers = context.request.headers.map(e => e.name() -> e.value()).toMap,
        success = true,
        startTime = startTime,
        endTime = System.currentTimeMillis())
      rt.onComplete {
        case Success(RouteResult.Complete(e)) =>
          logger.info(default.copy(endTime = System.currentTimeMillis(), code = e.status.intValue()).toString)
        case Success(e)                       =>
          logger.info(default.copy(endTime = System.currentTimeMillis()).toString)
        case Failure(e)                       =>
          logger.info(default.copy(endTime = System.currentTimeMillis()).toString)
      }
      rt
    }(context)
  }
}