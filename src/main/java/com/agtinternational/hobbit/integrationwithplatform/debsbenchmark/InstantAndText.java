package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import java.time.Instant;

/**
 * @author Roman Katerinenko
 */
class InstantAndText implements Comparable<InstantAndText> {
    private final Instant instant;
    private final String text;

    InstantAndText(Instant instant, String text) {
        this.instant = instant;
        this.text = text;
    }

    @Override
    public int compareTo(InstantAndText other) {
        return text.compareTo(other.getText());
    }

    Instant getInstant() {
        return instant;
    }

    String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}