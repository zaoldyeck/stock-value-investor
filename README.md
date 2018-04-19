# Stock Value Investor  
  
## Data Source
- Realtime Price http://mis.twse.com.tw/stock/
- Historical Prices http://www.twse.com.tw/zh/page/trading/exchange/STOCK_DAY.html
- Financial Report
  - http://mops.twse.com.tw/mops/web/t05st22_q1
  - https://goodinfo.tw/StockInfo/StockBzPerformance.asp

## Stock-Picking Strategies
1. Magic Formula
	- https://www.stockfeel.com.tw/%E6%8F%AD%E9%96%8B%E5%96%AC%E4%BC%8A%E3%83%BB%E8%91%9B%E6%9E%97%E5%B8%83%E9%9B%B7-%E7%A5%9E%E5%A5%87%E5%85%AC%E5%BC%8F%E7%9A%84%E9%9D%A2%E7%B4%97/
	- https://www.stockfeel.com.tw/%E9%A9%80%E7%84%B6%E5%9B%9E%E9%A6%96%E6%89%8D%E7%99%BC%E7%8F%BE%E6%9C%89%E5%A4%9A%E7%A5%9E%E5%A5%87-%E8%AB%87%E8%91%9B%E6%9E%97%E5%B8%83%E9%9B%B7%E7%9A%84%E6%8A%95%E8%B3%87%E6%80%9D%E6%83%B3/

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