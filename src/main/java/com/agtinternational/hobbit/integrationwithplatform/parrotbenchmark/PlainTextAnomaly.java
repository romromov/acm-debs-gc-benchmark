package com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
class PlainTextAnomaly implements Comparable<PlainTextAnomaly> {
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final String anomaly;

    PlainTextAnomaly(byte[] anomalyBytes) {
        this.anomaly = new String(anomalyBytes, CHARSET);
    }

    String getAnomaly() {
        return anomaly;
    }

    @Override
    public int compareTo(PlainTextAnomaly that) {
        return anomaly.compareTo(that.anomaly);
    }

    @Override
    public String toString() {
        return anomaly;
    }
}