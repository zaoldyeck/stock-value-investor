import java.io.{File, PrintWriter}

import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._

import scala.concurrent.{ExecutionContext, Future}

class Job(implicit ec: ExecutionContext, timeInterval: TimeInterval) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def magicFormula(): Future[Unit] = {
    case class StockFinance(id: String, ROA: Double, PER: Double)
    case class StockScore(id: String, name: String, ROA: Double, PER: Double, score: Int) {
      override def toString: String = {
        s"$id,$name,$ROA,$PER,$score,https://statementdog.com/analysis/tpe/$id,https://stock.cnyes.com/market/TSE:$id:STOCK\n"
      }
    }

    val stockFetcher = new StockFetcher
    val financeFetcher = new FinanceFetcher
    val priceFetcher = new PriceFetcher

    {
      for {
        stocks <- stockFetcher.getAllStocks
        prices <-
          Future.sequence(stocks.grouped(100).map {
            stocks => priceFetcher.getRealTimePrice(stocks.map(_.id))
          }) map (_.reduce(_ ::: _))
        finances <-
          /*
            Future.sequence(prices.grouped(100).map {
              prices =>
                Thread.sleep(1800000)
                prices.map(p => financeFetcher.getFinanceFromGoodinfo(p.id))
            } reduce (_ ::: _))
          */
          Future.sequence(prices.map {
            price => financeFetcher.getFinanceFromGoodinfo(price.id)
          })
      } yield {
        val stockFinances = prices zip finances map {
          pzf => StockFinance(pzf._1.id, pzf._2.ROA, pzf._1.price / pzf._2.EPS)
        }
        val scoreByROA = stockFinances.sortBy(_.ROA).zipWithIndex.sortBy(_._1.id)
        val scoreByPER = stockFinances.sortBy(-_.PER).zipWithIndex.sortBy(_._1.id)

        val idToName = stocks.map(s => s.id -> s.name).toMap

        val writer = new PrintWriter(new File(s"stock_rankings_${java.time.LocalDate.now}.csv"))
        writer.write("id,name,ROA,PER,score,statementdog,tradingview\n")

        scoreByROA zip scoreByPER map {
          rzp =>
            val stock = rzp._1._1
            StockScore(stock.id, idToName.getOrElse(stock.id, ""), stock.ROA, stock.PER, rzp._1._2 + rzp._2._2)
        } sortBy (-_.score) foreach {
          stockScore =>
            writer.write(stockScore.toString)
        }
        writer.close()
      }
    }
  }

  def magicFormulaByWespai(): Future[Unit] = {
    case class MagicFormulaRanking(id: String, name: String, price: Double, PER: Double, ROE: Double, score: Double) {
      override def toString: String = {
        s"$id,$name,$price,$PER,$ROE,$score,https://tw.stock.yahoo.com/d/s/earning_$id.html,https://stock.cnyes.com/market/TSE:$id:STOCK,https://statementdog.com/analysis/tpe/$id\n"
      }
    }

    case class ResStock(id: String, name: String, price: Double, PER: Double, ROE: Double)

    val data = Map("qry[]" -> Seq("dv", "ep", "3zcra10"),
      "id[]" -> Seq("dv", "ep", "3zcra10"),
      "val[]" -> Seq("0;12000", "-30000;30000", "-3000;3000"))

    Http.client.url("https://stock.wespai.com/pick/choice").post(data).map {
      response =>
        val writer = new PrintWriter(new File(s"magic_formula_rankings_${java.time.LocalDate.now}.csv"))
        writer.write("id,name,price,PER,ROE,score,yahoo,tradingview,statementdog\n")

        val resStocks = response.body[JsValue].as[List[(String, String, String, String, String)]].map {
          resStock =>
            val (id, name, price, per, roe) = resStock
            ResStock(id, name, price.toDouble, per.toDouble, roe.toDouble)
        } filter (resStock=>resStock.PER > 0 && resStock.ROE > 0)

        val scoreByPER = resStocks.sortBy(-_.PER).zipWithIndex.sortBy(_._1.id)
        val scoreByROE = resStocks.sortBy(_.ROE).zipWithIndex.sortBy(_._1.id)

        scoreByPER zip scoreByROE map {
          pzr =>
            val stock = pzr._1._1
            MagicFormulaRanking(stock.id, stock.name, stock.price, stock.PER, stock.ROE, pzr._1._2 + pzr._2._2)
        } sortBy (-_.score) foreach {
          magicFormulaRanking =>
            writer.write(magicFormulaRanking.toString)
        }
        writer.close()
    }
  }

  def FCFRankings(): Future[Unit] = {
    case class FCFRanking(id: String, name: String, price: Double, FCF: Double, ratio: Double) {
      override def toString: String = {
        s"$id,$name,$price,$FCF,$ratio,https://tw.stock.yahoo.com/d/s/earning_$id.html,https://stock.cnyes.com/market/TSE:$id:STOCK,https://statementdog.com/analysis/tpe/$id\n"
      }
    }

    val data = Map("qry[]" -> Seq("dv", "zcpx3"),
      "id[]" -> Seq("dv", "zcpx3"),
      "val[]" -> Seq("0;12000", "-800000;1500000"))

    Http.client.url("https://stock.wespai.com/pick/choice").post(data).map {
      response =>
        val writer = new PrintWriter(new File(s"fcf_rankings_${java.time.LocalDate.now}.csv"))
        writer.write("id,name,price,FCF,ratio,yahoo,tradingview,statementdog\n")

        response.body[JsValue].as[List[(String, String, String, String)]].map {
          resStock =>
            val (id, name, price, fcf) = resStock
            val ratio = price.toDouble / fcf.toDouble
            FCFRanking(id, name, price.toDouble, fcf.toDouble, ratio)
        } filter (_.FCF > 0) sortBy (_.ratio) foreach {
          fCFRanking =>
            writer.write(fCFRanking.toString)
        }
        writer.close()
    }
  }
}

