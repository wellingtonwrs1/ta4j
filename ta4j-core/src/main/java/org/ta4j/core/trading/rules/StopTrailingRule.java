package org.ta4j.core.trading.rules;

import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class StopTrailingRule extends AbstractRule {

    private final ClosePriceIndicator closePrice;

    private final Num gainPercentage;

    private final Num lossPercentage;

    private final Num trailingPercentage;

    private Num trailingSum;

    private final StopGainRule stopGainRule;

    private final StopLossRule stopLossRule;

    /**
     * Constructor
     *
     * @param closePrice         the close price indicator
     * @param gainPercentage     the gain percentage
     * @param lossPercentage     the loss percentage
     * @param trailingPercentage the trailing percentage
     */
    public StopTrailingRule(ClosePriceIndicator closePrice, Num gainPercentage, Num lossPercentage, Num trailingPercentage) {
        this.closePrice = closePrice;
        this.gainPercentage = gainPercentage;
        this.lossPercentage = lossPercentage;
        this.trailingPercentage = trailingPercentage;
        this.trailingSum = closePrice.numOf(0);
        this.stopGainRule = new StopGainRule(closePrice, gainPercentage, false, 0);
        this.stopLossRule = new StopLossRule(closePrice, lossPercentage, false, 0);
    }

    /**
     * Constructor
     *
     * @param closePrice         the close price indicator
     * @param gainPercentage     the gain percentage
     * @param lossPercentage     the loss percentage
     * @param trailingPercentage the trailing percentage
     * @param pips               the stop is calculate in pips
     * @param pipPosition        the pip position
     */
    public StopTrailingRule(ClosePriceIndicator closePrice, Num gainPercentage, Num lossPercentage, Num trailingPercentage, boolean pips, int pipPosition) {
        this.closePrice = closePrice;
        this.gainPercentage = gainPercentage;
        this.lossPercentage = lossPercentage;
        this.trailingPercentage = trailingPercentage;
        this.trailingSum = closePrice.numOf(0);
        this.stopGainRule = new StopGainRule(closePrice, gainPercentage, pips, pipPosition);
        this.stopLossRule = new StopLossRule(closePrice, lossPercentage, pips, pipPosition);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no trailing
        if (tradingRecord != null) {
            this.updatePercentage();
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
        if (this.trailingPercentage != null) {
            this.trailingSum = this.trailingSum.plus(this.trailingPercentage);
            return true;
        }
        return false;
    }

    private void updatePercentage() {
        if (this.trailingSum.isPositive()) {
            Num nextGainPercentage = this.gainPercentage.plus(this.trailingSum);
            this.stopGainRule.setGainPercentage(nextGainPercentage);
            this.stopLossRule.setLossPercentage(nextGainPercentage.minus(this.trailingPercentage).multipliedBy(this.closePrice.numOf(-1)));
        } else {
            this.stopGainRule.setGainPercentage(this.gainPercentage);
            this.stopLossRule.setLossPercentage(this.lossPercentage);
        }
    }

}
