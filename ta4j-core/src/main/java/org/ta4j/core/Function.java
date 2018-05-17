package org.ta4j.core;


public interface Function<I,O> {

    O apply(I input);
}
