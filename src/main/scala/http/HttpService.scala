package http

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import better.files._
import http.CollectRequestInfo.collectRequestInfo
import models.{ClientRequest, Commands, DelayCommand, Image}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

trait AkkaSources {
  implicit val system          : ActorSystem              = ActorSystem()
  implicit val materializer    : ActorMaterializer        = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
}

class HttpService(workActor: ActorRef)(implicit akkaSources: AkkaSources) {

  private  val logger           = LoggerFactory.getLogger("http-service")
  implicit val timeout: Timeout = 5.seconds

  logger.info("start")

  import akkaSources._

  val myAndroid = post(
    path(PathMatcher("files")) {
      uploadedFile("screen") { case (fileInfo, jfile) =>
        val file = File(s"F:/tmp/android-screen.png")
        File(jfile.getAbsolutePath).moveTo(file, true)

//        complete(Json.toJson(List(DelayCommand(1000))).toString())
        val feature = workActor
          .ask(ClientRequest(Image(file.pathAsString))).mapTo[Commands]
          .map(e => e.seq.map(_.toJsonString).mkString("[",",","]"))
        onComplete(feature) {
          case Success(x) => complete(x)
          case Failure(x) =>
            x.printStackTrace()
            System.exit(-1)
            ???
        }
      }
    }
  )

  val route = post(
    // url 路径 为 scala/ajjl
    path(PathMatcher("scala") / "ajjl") {
      //接受上传过来的文件
      uploadedFile("screen") { case (fileInfo, jfile) =>
        //上传过来的问会被保存到一个临时文件中,将它copy到我们想要的目录
        val file = File("screen.png")
        File(jfile.getAbsolutePath).moveTo(file, true)
        //actor将是需要我们实现的,暂时忽视它
        //将图片发送给actor,然后将返回的结果转成json
        val feature = workActor
          .ask(ClientRequest(Image(file.pathAsString))).mapTo[Commands]
          .map(_.seq.map(_.toJsonString).mkString(";"))
        //将结果返回client
        onComplete(feature) {
          case Success(x) => complete(x)
          case Failure(x) =>
            x.printStackTrace()
            System.exit(-1)
            ???
        }
      }
    }) ~ get(path("hello")(complete("hello world")))

  lazy val http = Http().bindAndHandle(collectRequestInfo(route ~ myAndroid), "0.0.0.0", 9898)


}

