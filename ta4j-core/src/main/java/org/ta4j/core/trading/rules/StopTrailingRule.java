package org.ta4j.core.trading.rules;

import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class StopTrailingRule extends AbstractRule {

    private final ClosePriceIndicator closePrice;

    private final Num gainValue;

    private final Num lossValue;

    private final Num trailingValue;

    private Num trailingSum;

    private final StopGainRule stopGainRule;

    private final StopLossRule stopLossRule;

    /**
     * Constructor
     *
     * @param closePrice    the close price indicator
     * @param gainValue     the gain value
     * @param lossValue     the loss value
     * @param trailingValue the trailing value
     */
    public StopTrailingRule(ClosePriceIndicator closePrice, Num gainValue, Num lossValue, Num trailingValue) {
        this.closePrice = closePrice;
        this.gainValue = gainValue;
        this.lossValue = lossValue;
        this.trailingValue = trailingValue;
        this.trailingSum = closePrice.numOf(0);
        this.stopGainRule = new StopGainRule(closePrice, gainValue, false, 0);
        this.stopLossRule = new StopLossRule(closePrice, lossValue, false, 0);
    }

    /**
     * Constructor
     *
     * @param closePrice    the close price indicator
     * @param gainValue     the gain value
     * @param lossValue     the loss value
     * @param trailingValue the trailing value
     * @param pips          the stop is calculate in pips
     * @param pipPosition   the pip position
     */
    public StopTrailingRule(ClosePriceIndicator closePrice, Num gainValue, Num lossValue, Num trailingValue, boolean pips, int pipPosition) {
        this.closePrice = closePrice;
        this.gainValue = gainValue;
        this.lossValue = lossValue;
        this.trailingValue = trailingValue;
        this.trailingSum = closePrice.numOf(0);
        this.stopGainRule = new StopGainRule(closePrice, gainValue, pips, pipPosition);
        this.stopLossRule = new StopLossRule(closePrice, lossValue, pips, pipPosition);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no trailing
        if (tradingRecord != null) {
            this.updateTrailingValue();
            if (this.stopGainRule.isSatisfied(index, tradingRecord) && !this.isTrailingStopped()) {
                this.trailingSum = this.closePrice.numOf(0);
                satisfied = true;
            } else if (this.stopLossRule.isSatisfied(index, tradingRecord)) {
                this.trailingSum = this.closePrice.numOf(0);
                satisfied = true;
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    private boolean isTrailingStopped() {
        if (this.trailingValue != null && this.trailingValue.isPositive()) {
            this.trailingSum = this.trailingSum.plus(this.trailingValue);
            return true;
        }
        return false;
    }

    private void updateTrailingValue() {
        if (this.trailingValue != null && this.trailingValue.isPositive()) {
            if (this.trailingSum.isPositive()) {
                this.stopGainRule.setGainValue(this.gainValue.plus(this.trailingSum));
                this.stopLossRule.setLossValue(this.trailingSum.multipliedBy(this.closePrice.numOf(-1)));
            } else {
                this.stopGainRule.setGainValue(this.gainValue);
                this.stopLossRule.setLossValue(this.lossValue);
            }
        }
    }

}
