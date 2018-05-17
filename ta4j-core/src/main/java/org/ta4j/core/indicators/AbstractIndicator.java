/*******************************************************************************
 *   The MIT License (MIT)
 *
 *   Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2018 Ta4j Organization 
 *   & respective authors (see AUTHORS)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy of
 *   this software and associated documentation files (the "Software"), to deal in
 *   the Software without restriction, including without limitation the rights to
 *   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *   the Software, and to permit persons to whom the Software is furnished to do so,
 *   subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *   FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.ta4j.core.indicators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

/**
 * Abstract {@link Indicator indicator}.
 * </p>
 */
public abstract class AbstractIndicator<T> implements Indicator<T> {

    /** The logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private TimeSeries series;

    /**
     * Constructor.
     * @param series the related time series
     */
    public AbstractIndicator(TimeSeries series) {
        this.series = series;
    }

    @Override
    public TimeSeries getTimeSeries() {
        return series;
    }



    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public Num numOf(Number number){
        return series.numOf(number);
    }

    /**
     * Returns all values from an {@link Indicator} as an array of Doubles. The
     * returned doubles could have a minor loss of precise, if {@link Indicator}
     * was based on {@link Num Num}.
     *
     * @param ref the indicator
     * @param index the index
     * @param barCount the barCount
     * @return array of Doubles within the barCount
     */
    static Double[] toDouble(Indicator<Num> ref, int index, int barCount) {

        Double[] all = new Double[barCount];

        int startIndex = Math.max(0, index - barCount + 1);
        for (int i = 0; i < barCount; i++) {
            Num number = ref.getValue(i + startIndex);
            all[i] = number.doubleValue();
        }

        return all;
    }
}
