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
package org.ta4j.core.trading.rules;

import org.ta4j.core.Order;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

public class ExitWhenProfitableRule extends AbstractRule {

    /**
     * The close price indicator
     */
    private final ClosePriceIndicator closePrice;

    /**
     * The expiration time in minutes
     */
    private final Num expirationTime;

    /**
     * Constructor.
     *
     * @param closePrice     the close price indicator
     * @param expirationTime to satisfied when profitable and time in minutes expired
     */
    public ExitWhenProfitableRule(ClosePriceIndicator closePrice, Num expirationTime) {
        this.closePrice = closePrice;
        this.expirationTime = expirationTime;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no loss
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                Order order = currentTrade.getEntry();
                Num currentPrice = closePrice.getValue(index);
                if (currentTrade.getEntry().isBuy()) {
                    satisfied = isBuyGainSatisfied(order.getNetPrice(), currentPrice) && isExpirationTimeSatisfied(order.getStartTime());
                } else {
                    satisfied = isSellGainSatisfied(order.getNetPrice(), currentPrice) && isExpirationTimeSatisfied(order.getStartTime());
                }
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    private boolean isExpirationTimeSatisfied(ZonedDateTime startTime) {
        return startTime == null || startTime.plusMinutes(expirationTime.intValue()).compareTo(ZonedDateTime.now()) <= 0;
    }

    private boolean isBuyGainSatisfied(Num entryPrice, Num currentPrice) {
        return currentPrice.isGreaterThanOrEqual(entryPrice);
    }

    private boolean isSellGainSatisfied(Num entryPrice, Num currentPrice) {
        return currentPrice.isLessThanOrEqual(entryPrice);
    }

}
