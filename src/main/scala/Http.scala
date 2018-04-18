import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text
import play.api.libs.ws.DefaultWSProxyServer
import play.api.libs.ws.ahc.{StandaloneAhcWSClient, StandaloneAhcWSRequest}

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object Http {
  implicit private val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }

  val client = new StandaloneAhcWSClientWithProxyPool()

  def terminate(): Unit = {
    client.close()
    system.terminate()
  }

  class StandaloneAhcWSClientWithProxyPool {
    private implicit val materializer = ActorMaterializer()
    private val client = StandaloneAhcWSClient()

    /*
    private val freeProxies: immutable.IndexedSeq[(String, Int)] = Await.result(
      client.url("https://free-proxy-list.net/").get().map {
        response =>
          val doc: Browser#DocumentType = JsoupBrowser().parseString(response.body)
          1 to 300 map {
            index =>
              val ip = doc >> text(s"#proxylisttable > tbody > tr:nth-child($index) > td:nth-child(1)")
              val port = doc >> text(s"#proxylisttable > tbody > tr:nth-child($index) > td:nth-child(2)")
              (ip, port.toInt)
          }
      }, Duration.Inf)

    private val infiniteIterator: Iterator[(String, Int)] = Iterator.continually(freeProxies).flatten

    private def getProxy: (String, Int) = synchronized {
      infiniteIterator.next()
    }
    */

    def url(url: String, disableUrlEncoding: Boolean = false): StandaloneAhcWSRequest = {
      //val (ip, port) = getProxy

      StandaloneAhcWSRequest(client = client,
        url = url,
        //proxyServer = Some(DefaultWSProxyServer("118.171.31.75", 3128)),
        disableUrlEncoding = Some(disableUrlEncoding))
    }

    def close(): Unit = client.close()
  }

}
