import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws._
import play.api.libs.ws.ahc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HttpClient {
  implicit private val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }

  implicit private val materializer = ActorMaterializer()
  private val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

  def ws(callback: (StandaloneAhcWSClient) => Future[Any]): Future[Any] = {
    callback(wsClient)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }
  }
}
