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

import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;

/**
 * A stop-loss rule.
 * <p>
 * Satisfied when the close price reaches the loss threshold.
 */
public class StopLossRule extends AbstractRule {

    /**
     * Constant value for 100
     */
    private final Num HUNDRED;

    /**
     * The close price indicator
     */
    private final ClosePriceIndicator closePrice;

    /**
     * The loss value
     */
    private Num lossValue;

    private final boolean pips;

    private final int pipPosition;

    /**
     * Constructor.
     *
     * @param closePrice the close price indicator
     * @param lossValue  the loss value
     */
    public StopLossRule(ClosePriceIndicator closePrice, Number lossValue) {
        this(closePrice, closePrice.numOf(lossValue));
    }

    /**
     * Constructor.
     *
     * @param closePrice the close price indicator
     * @param lossValue  the loss value
     */
    public StopLossRule(ClosePriceIndicator closePrice, Num lossValue) {
        this(closePrice, lossValue, false, 0);
    }

    /**
     * Constructor.
     *
     * @param closePrice  the close price indicator
     * @param lossValue   the loss value
     * @param pips        the stop is calculate in pips
     * @param pipPosition the pip position
     */
    public StopLossRule(ClosePriceIndicator closePrice, Num lossValue, boolean pips, int pipPosition) {
        this.closePrice = closePrice;
        this.lossValue = lossValue;
        this.pips = pips;
        this.pipPosition = pipPosition;
        this.HUNDRED = closePrice.numOf(100);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no loss
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {

                Num entryPrice = currentTrade.getEntry().getNetPrice();
                Num currentPrice = closePrice.getValue(index);

                if (currentTrade.getEntry().isBuy()) {
                    satisfied = isBuyStopSatisfied(entryPrice, currentPrice);
                } else {
                    satisfied = isSellStopSatisfied(entryPrice, currentPrice);
                }
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    private boolean isSellStopSatisfied(Num entryPrice, Num currentPrice) {
        if (pips) {
            return calculatePips(entryPrice, currentPrice, true);
        }
        Num lossRatioThreshold = HUNDRED.plus(lossValue).dividedBy(HUNDRED);
        Num threshold = entryPrice.multipliedBy(lossRatioThreshold);
        return currentPrice.isGreaterThanOrEqual(threshold);
    }

    private boolean isBuyStopSatisfied(Num entryPrice, Num currentPrice) {
        if (pips) {
            return calculatePips(entryPrice, currentPrice, false);
        }
        Num lossRatioThreshold = HUNDRED.minus(lossValue).dividedBy(HUNDRED);
        Num threshold = entryPrice.multipliedBy(lossRatioThreshold);
        return currentPrice.isLessThanOrEqual(threshold);
    }

    private boolean calculatePips(Num entryPrice, Num currentPrice, boolean isSell) {
        BigDecimal entry = ((BigDecimal) entryPrice.getDelegate());
        BigDecimal current = ((BigDecimal) currentPrice.getDelegate());
        int position = pipPosition > 0 ? pipPosition : current.scale();
        if (isSell) {
            return current.subtract(entry).movePointRight(position).compareTo((BigDecimal) lossValue.getDelegate()) >= 0;
        }
        return entry.subtract(current).movePointRight(position).compareTo((BigDecimal) lossValue.getDelegate()) >= 0;
    }

    public void setLossValue(Num lossValue) {
        this.lossValue = lossValue;
    }
}
