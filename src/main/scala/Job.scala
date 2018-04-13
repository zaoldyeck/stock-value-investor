import java.io.{File, PrintWriter}

import scala.concurrent.{ExecutionContext, Future}

class Job(implicit ec: ExecutionContext) {

  def magicFormula(): Future[Unit] = {
    case class StockFinance(id: String, ROA: Double, PER: Double)
    case class StockPoint(id: String, ROA: Double, PER: Double, point: Int) {
      override def toString: String = {
        s"$id,$ROA,$PER,$point\n"
      }
    }

    val stockFetcher = new StockFetcher
    val financeFetcher = new FinanceFetcher
    val priceFetcher = new PriceFetcher
    val writer = new PrintWriter(new File("stock-rankings.csv"))
    writer.write("id,ROA,PER,point\n")

    {
      for {
        stocks <- stockFetcher.getAllStocks
        prices <-
          Future.sequence(stocks.take(200).grouped(100).map {
            stocks => priceFetcher.getRealTimePrice(stocks.map(_.id))
          }) map (_.reduce(_ ::: _))
        finances <- Future.sequence(prices.map {
          price => financeFetcher.getFinanceFromGoodinfo(price.id)
        })
      } yield {
        val stockFinances = prices zip finances map {
          pzf => StockFinance(pzf._1.id, pzf._2.ROA, pzf._1.price / pzf._2.EPS)
        }
        val pointByROA = stockFinances.sortBy(_.ROA).zipWithIndex.sortBy(_._1.id)
        val pointByPER = stockFinances.sortBy(-_.PER).zipWithIndex.sortBy(_._1.id)

        pointByROA zip pointByPER map {
          rzp =>
            StockPoint(rzp._1._1.id, rzp._1._1.ROA, rzp._1._1.PER, rzp._1._2 + rzp._2._2)
        } sortBy (-_.point) foreach {
          stockPoint =>
            writer.write(stockPoint.toString)
        }
        writer.close()
      }
    }
  }
}

