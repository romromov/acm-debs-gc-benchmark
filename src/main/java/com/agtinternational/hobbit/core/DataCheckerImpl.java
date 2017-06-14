package com.agtinternational.hobbit.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * @author Roman Katerinenko
 */
public class DataCheckerImpl<T extends Comparable<T>> implements DataChecker<T> {
    private static final Logger logger = LoggerFactory.getLogger(DataCheckerImpl.class);

    private final LinkedList<T> golds = new LinkedList<>();
    private final LinkedList<T> actuals = new LinkedList<>();
    private final ComparablesMatcher<T> comparablesMatcher;

    private boolean lastValuesMatched = true;
    private boolean checkMessageCount;
    private int matchedMessagesCount;
    private int expectedMessagesCount;

    public DataCheckerImpl(ComparablesMatcher<T> comparablesMatcher) {
        this(false, comparablesMatcher);
    }

    public DataCheckerImpl(int expectedMessagesCount, ComparablesMatcher<T> comparablesMatcher) {
        this(true, comparablesMatcher);
        this.expectedMessagesCount = expectedMessagesCount;
    }

    private DataCheckerImpl(boolean checkMessageCount, ComparablesMatcher<T> comparablesMatcher) {
        this.checkMessageCount = checkMessageCount;
        this.comparablesMatcher = comparablesMatcher;
    }

    @Override
    public synchronized void addActual(T actual) {
        logger.debug("Got from system: {}", actual);
        if (lastValuesMatched) {
            actuals.addLast(actual);
            checkSameStrings();
        }
    }

    @Override
    public synchronized void addGold(T gold) {
        logger.debug("Got from benchmark: {}", gold);
        if (lastValuesMatched) {
            golds.addLast(gold);
            checkSameStrings();
        }
    }

    public synchronized T getNotMatchedActual() {
        return actuals.size() > 0 ? actuals.getFirst() : null;
    }

    public synchronized T getNotMatchedGold() {
        return golds.size() > 0 ? golds.getFirst() : null;
    }

    @Override
    public synchronized boolean isCorrect() {
        boolean allEmpty = golds.isEmpty() && actuals.isEmpty();
        boolean isMessageCountMatch = !checkMessageCount || matchedMessagesCount == expectedMessagesCount;
        return allEmpty && isMessageCountMatch && lastValuesMatched;
    }

    public synchronized int getCheckedCount() {
        return matchedMessagesCount;
    }

    private void checkSameStrings() {
        if (!golds.isEmpty() && !actuals.isEmpty()) {
            T gold = golds.getFirst();
            T actual = actuals.getFirst();
            lastValuesMatched = comparablesMatcher.compare(gold, actual) == 0;
            if (lastValuesMatched) {
                golds.removeFirst();
                actuals.removeFirst();
                matchedMessagesCount++;
                whenMatched(gold, actual);
            }
        }
    }

    protected void whenMatched(T gold, T actual) {
    }
}