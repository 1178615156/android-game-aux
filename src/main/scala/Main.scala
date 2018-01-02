import akka.actor.Props
import http.{AkkaSources, HttpService}
import nyhx.ClientActor

object Main {
  def main(args: Array[String]): Unit = {
    implicit val akkaSource: AkkaSources = new AkkaSources {}
    import akkaSource._

    val httpService = new HttpService(system.actorOf(Props(new ClientActor(args)),"nyhx"))
    httpService.http
  }
}
