package com.agtinternational.hobbit.core;

/**
 * @author Roman Katerinenko
 */
public interface BenchmarkTask extends Runnable {
    boolean isSuccessful();

    KeyValue getResult();
}