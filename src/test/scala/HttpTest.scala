import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text
import org.scalatest.FunSuite
import play.api.libs.ws.ahc.AhcCurlRequestLogger

import scala.concurrent.ExecutionContext.Implicits.global

class HttpTest extends FunSuite {
  test("getFreeProxytList") {
    Http.client.url("https://www.proxynova.com/proxy-server-list/country-tw/")
      .withRequestFilter(AhcCurlRequestLogger())
      .get()
      .map {
        response =>
          //TODO https://www.proxynova.com/proxy-server-list/country-tw/
          val doc: Browser#DocumentType = JsoupBrowser().parseString(response.body)
          val index = 1
          val ip = doc >?> text(s"#tbl_proxy_list > tbody:nth-child(2) > tr:nth-child(1) > td:nth-child(1) > abbr")
          //"#tbl_proxy_list > tbody:nth-child(2) > tr:nth-child(3) > td:nth-child(1) > abbr"
          val port = doc >?> text(s"#proxylisttable > tbody > tr:nth-child($index) > td:nth-child(2)")
          assert(ip.isDefined)
          assert(port.isDefined)
          println(s"ip = ${ip.get}, port = ${port.get}")
      }
  }

  test("infiniteIterator") {
    val iterator = Iterator.continually(List(1, 2, 3)).flatten
    iterator.foreach(println)
  }
}
