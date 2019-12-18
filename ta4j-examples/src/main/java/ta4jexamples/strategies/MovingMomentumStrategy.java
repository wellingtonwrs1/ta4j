/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2019 Ta4j Organization & respective
 * authors (see AUTHORS)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ta4jexamples.strategies;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvBarsLoader;

/**
 * Moving momentum strategy.
 *
 * @see <a href=
 * "http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum">
 * http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum</a>
 */
public class MovingMomentumStrategy {

    /**
     * @param series the bar series
     * @return the moving momentum strategy
     */
    public static Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // The bias is bullish when the shorter-moving average moves above the longer moving average.
        // The bias is bearish when the shorter-moving average moves below the longer moving average.
        EMAIndicator shortEma = new EMAIndicator(closePrice, 5);
        EMAIndicator longEma = new EMAIndicator(closePrice, 15);

        MACDIndicator macd = new MACDIndicator(closePrice, 5, 15);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        Rule buyRule = new CrossedUpIndicatorRule(shortEma, longEma) // Trend
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 1

        Rule sellRule = new CrossedDownIndicatorRule(shortEma, longEma) // Trend
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 1

        Rule closeRule = new StopTrailingRule(closePrice, series.numOf(1), series.numOf(100), series.numOf(0.5));

        return new BaseStrategy(buyRule, sellRule, closeRule);
    }

    public static void main(String[] args) {

        // Getting the bar series
        BarSeries series = CsvBarsLoader.loadAppleIncSeries();

        TradingRecord tradingRecord = null;
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            Strategy strategy = buildStrategy(series);
            if (strategy.shouldEnter(i)) {
                tradingRecord = new BaseTradingRecord(Order.OrderType.BUY);
                tradingRecord.enter(i, series.getBar(i).getClosePrice(), series.numOf(1));
                System.out.println(series.getBar(i).getEndTime().toString() + ": " + tradingRecord.getLastEntry().toString());
            } else if (strategy.shouldExit(i)) {
                tradingRecord = new BaseTradingRecord(Order.OrderType.SELL);
                tradingRecord.enter(i, series.getBar(i).getClosePrice(), series.numOf(1));
                System.out.println(series.getBar(i).getEndTime().toString() + ": " + tradingRecord.getLastEntry().toString());
            }
            if (tradingRecord != null) {
                for (int j = i; j <= series.getEndIndex(); j++) {
                    double minPrice = series.getBar(j).getLowPrice().doubleValue();
                    while (minPrice <= series.getBar(j).getHighPrice().doubleValue()) {
                        series.getBar(j).addPrice(series.numOf(minPrice));
                        if (strategy.shouldClose(j, tradingRecord)) {
                            tradingRecord.exit(j, series.getBar(j).getClosePrice(), series.numOf(1));
                            System.out.println(series.getBar(j).getEndTime().toString() + ": " + tradingRecord.getLastExit().toString());
                            break;
                        }
                        minPrice += 0.01;
                    }
                }
                System.out.println("------------------------------------------------------------------------------------");
                tradingRecord = null;
            }
        }
    }

}
