package org.ta4j.core.trading.rules;

import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class StopTrailingRule extends AbstractRule {

    private final ClosePriceIndicator closePrice;

    private final Num gainPercentage;

    private final Num lossPercentage;

    private final Num trailingPercentage;

    private Num trailingSum;

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
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no loss
        if (tradingRecord != null) {
            Rule stopGainRule = new StopGainRule(this.closePrice, this.gainPercentage.plus(this.trailingSum));
            Rule stopLossRule = new StopLossRule(this.closePrice, this.getLossPercentage());
            if (stopGainRule.isSatisfied(index, tradingRecord) && !this.isTrailingStop()) {
                this.trailingSum = closePrice.numOf(0);
                satisfied = true;
            } else if (stopLossRule.isSatisfied(index, tradingRecord)) {
                this.trailingSum = closePrice.numOf(0);
                satisfied = true;
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    private boolean isTrailingStop() {
        if (this.trailingPercentage != null) {
            this.trailingSum = this.trailingSum.plus(this.trailingPercentage);
            return true;
        }
        return false;
    }

    private Num getLossPercentage() {
        if (this.trailingSum.isPositive()) {
            return this.gainPercentage.plus(this.trailingSum).minus(this.trailingPercentage).multipliedBy(closePrice.numOf(-1));
        }
        return this.lossPercentage;
    }

}