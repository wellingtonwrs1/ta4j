package org.ta4j.core.trading.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Rule;

public abstract class AbstractRule implements Rule {

    /** The logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** The class name */
    protected final String className = getClass().getSimpleName();

    /**
     * Traces the isSatisfied() method calls.
     * @param index the bar index
     * @param isSatisfied true if the rule is satisfied, false otherwise
     */
    protected void traceIsSatisfied(int index, boolean isSatisfied) {
        log.trace("{}#isSatisfied({}): {}", className, index, isSatisfied);
    }

    /**
     * @param rule another trading rule
     * @return a rule which is the AND combination of this rule with the provided one
     */
    public Rule and(Rule rule) {
        return new AndRule(this, rule);
    }

    /**
     * @param rule another trading rule
     * @return a rule which is the OR combination of this rule with the provided one
     */
    public Rule or(Rule rule) {
        return new OrRule(this, rule);
    }

    /**
     * @param rule another trading rule
     * @return a rule which is the XOR combination of this rule with the provided one
     */
    public Rule xor(Rule rule) {
        return new XorRule(this, rule);
    }

    /**
     * @return a rule which is the logical negation of this rule
     */
    public Rule negation() {
        return new NotRule(this);
    }

    /**
     * @param index the bar index
     * @return true if this rule is satisfied for the provided index, false otherwise
     */
    public boolean isSatisfied(int index) {
        return isSatisfied(index, null);
    }

}
