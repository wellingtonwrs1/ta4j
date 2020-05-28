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
 * A stop-gain rule.
 * <p>
 * Satisfied when the close price reaches the gain threshold.
 */
public class StopGainRule extends AbstractRule {

    /**
     * Constant value for 100
     */
    private final Num HUNDRED;

    /**
     * The close price indicator
     */
    private final ClosePriceIndicator closePrice;

    /**
     * The gain percentage
     */
    private Num gainPercentage;

    private final boolean pips;

    private final int pipPosition;

    /**
     * Constructor.
     *
     * @param closePrice     the close price indicator
     * @param gainPercentage the gain percentage
     */
    public StopGainRule(ClosePriceIndicator closePrice, Number gainPercentage) {
        this(closePrice, closePrice.numOf(gainPercentage));
    }

    /**
     * Constructor.
     *
     * @param closePrice     the close price indicator
     * @param gainPercentage the gain percentage
     */
    public StopGainRule(ClosePriceIndicator closePrice, Num gainPercentage) {
        this(closePrice, gainPercentage, false, 0);
    }

    /**
     * Constructor.
     *
     * @param closePrice     the close price indicator
     * @param gainPercentage the gain percentage
     * @param pips           the stop is calculate in pips
     * @param pipPosition    the pip position
     */
    public StopGainRule(ClosePriceIndicator closePrice, Num gainPercentage, boolean pips, int pipPosition) {
        this.closePrice = closePrice;
        this.gainPercentage = gainPercentage;
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
                    satisfied = isBuyGainSatisfied(entryPrice, currentPrice);
                } else {
                    satisfied = isSellGainSatisfied(entryPrice, currentPrice);
                }
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    private boolean isSellGainSatisfied(Num entryPrice, Num currentPrice) {
        if (pips) {
            BigDecimal entry = ((BigDecimal) entryPrice.getDelegate());
            BigDecimal current = ((BigDecimal) currentPrice.getDelegate());
            return entry.subtract(current).movePointRight(pipPosition > 0 ? pipPosition : current.scale()).compareTo((BigDecimal) gainPercentage.getDelegate()) >= 0;
        }
        Num lossRatioThreshold = HUNDRED.minus(gainPercentage).dividedBy(HUNDRED);
        Num threshold = entryPrice.multipliedBy(lossRatioThreshold);
        return currentPrice.isLessThanOrEqual(threshold);
    }

    private boolean isBuyGainSatisfied(Num entryPrice, Num currentPrice) {
        if (pips) {
            BigDecimal entry = ((BigDecimal) entryPrice.getDelegate());
            BigDecimal current = ((BigDecimal) currentPrice.getDelegate());
            return current.subtract(entry).movePointRight(pipPosition > 0 ? pipPosition : current.scale()).compareTo((BigDecimal) gainPercentage.getDelegate()) >= 0;
        }
        Num lossRatioThreshold = HUNDRED.plus(gainPercentage).dividedBy(HUNDRED);
        Num threshold = entryPrice.multipliedBy(lossRatioThreshold);
        return currentPrice.isGreaterThanOrEqual(threshold);
    }

    public void setGainPercentage(Num gainPercentage) {
        this.gainPercentage = gainPercentage;
    }
}
