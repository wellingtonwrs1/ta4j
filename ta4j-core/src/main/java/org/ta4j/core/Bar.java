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
package org.ta4j.core;


import org.ta4j.core.num.Num;

import java.io.Serializable;

/**
 * End bar of a time period.
 * </p>
 * Bar object is aggregated open/high/low/close/volume/etc. data over a time period.
 */
public interface Bar extends Serializable {
    /**
     * @return the open price of the period
     */
    Num getOpenPrice();

    /**
     * @return the min price of the period
     */
    Num getMinPrice();

    /**
     * @return the max price of the period
     */
    Num getMaxPrice();

    /**
     * @return the close price of the period
     */
    Num getClosePrice();

    /**
     * @return the whole tradeNum volume in the period
     */
    Num getVolume();

    /**
     * @return the number of trades in the period
     */
    int getTrades();

    /**
     * @return the whole traded amount of the period
     */
    Num getAmount();


    /**
     * @return the begin timestamp of the bar period
     */
    long getBeginTime();

    /**
     * @return the end timestamp of the bar period
     */
    long getEndTime();

    /**
     * @param timestamp a timestamp
     * @return true if the provided timestamp is between the begin time and the end time of the current period, false otherwise
     */
    boolean inPeriod(long timestamp);

    /**
     * @return a human-friendly string of the end timestamp
     */
    String getDateName();

    /**
     * @return a even more human-friendly string of the end timestamp
     */
    String getSimpleDateName();

    /**
     * @return true if this is a bearish bar, false otherwise
     */
    boolean isBearish();

    /**
     * @return true if this is a bullish bar, false otherwise
     */
    boolean isBullish();

    /**
     * Adds a trade at the end of bar period.
     * @param tradeVolume the traded volume
     * @param tradePrice the price
     * @deprecated use corresponding function of TimeSeries
     */
    @Deprecated
    void addTrade(double tradeVolume, double tradePrice, Function<Number, Num> numFunction);

    /**
     * Adds a trade at the end of bar period.
     * @param tradeVolume the traded volume
     * @param tradePrice the price
     * @deprecated use corresponding function of TimeSeries
     */
    @Deprecated
    void addTrade(String tradeVolume, String tradePrice, Function<Number, Num> numFunction);

    /**
     * Adds a trade at the end of bar period.
     * @param tradeVolume the traded volume
     * @param tradePrice the price
     */
    void addTrade(Num tradeVolume, Num tradePrice);


    void addPrice(String price, Function<Number, Num> numFunction);

    void addPrice(Number price, Function<Number, Num> numFunction);

    void addPrice(Num price);
}
