# Stock Value Investor  
  
## Data Source
- Realtime Price http://mis.twse.com.tw/stock/
- Historical Prices http://www.twse.com.tw/zh/page/trading/exchange/STOCK_DAY.html
- Financial Report
  - http://mops.twse.com.tw/mops/web/t05st22_q1
  - https://goodinfo.tw/StockInfo/StockBzPerformance.asp

## Stock-Picking Strategies
1. Magic Formula
	- https://goo.gl/s91ZlN
	- https://goo.gl/YZ21gE

## Requirements
- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [sbt](https://www.scala-sbt.org/)

## Running
`sdt run`
or
`sbt assembly` (compiling)
`java -jar stock-value-investor-assembly-1.0.jar >> log 2>&1 &`
> Running need over than 8 hours if you fetch all stocks. Because data source API will block your IP if you fetch data too frequent.
## Output
stock-rankings.csv

### Format
| id | ROA | PER | score | statementdog | tradingview |
|-|-|-|-|-|-|
| 2330 | 17.7 | 18.4807256236 | 3000 | https://statementdog.com/analysis/tpe/2330 | https://stock.cnyes.com/market/TSE:2330:STOCK |

## Next Milestone
1. Supervised learning from financial data for long-term stock price prediction.
2. Sequence model or CNN model training for forecasting short-term trends of stock.