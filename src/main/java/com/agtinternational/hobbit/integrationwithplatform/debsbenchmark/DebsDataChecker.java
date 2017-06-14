package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agtinternational.hobbit.core.ComparablesMatcher;
import com.agtinternational.hobbit.core.DataCheckerImpl;
import com.agtinternational.hobbit.core.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author Roman Katerinenko
 */
class DebsDataChecker extends DataCheckerImpl<InstantAndText> {
    private static final Logger logger = LoggerFactory.getLogger(DebsDataChecker.class);

    private AnomalyDetector anomalyDetector;
    private Duration sumOfDeltas = Duration.ZERO;

    DebsDataChecker(KeyValue inputParams, AnomalyDetectorBuilder anomalyDetectorBuilder) {
        super(new AnomalyMatcher());
        try {
            anomalyDetector = anomalyDetectorBuilder.buildFrom(inputParams, this::addGold);
        } catch (Exception e) {
            anomalyDetector = null;
            logger.error("Exception", e);
        }
    }

    void addDataPoint(InstantAndText dataPoint) {
        logger.debug("Got benchmark data point: {}", dataPoint.getText());
        anomalyDetector.addNewDataPoint(dataPoint);
    }

    @Override
    protected void whenMatched(InstantAndText gold, InstantAndText actual) {
        Duration delta = Duration.between(gold.getInstant(), actual.getInstant());
        sumOfDeltas = sumOfDeltas.plus(delta);
    }

    Duration getAverageLatency() {
        int checkedCount = getCheckedCount();
        return checkedCount > 0 ? sumOfDeltas.dividedBy(checkedCount) : null;
    }

    interface AnomalyDetector {
        void addNewDataPoint(InstantAndText dataPoint);
    }

    interface AnomalyListener {
        void onNewAnomaly(InstantAndText anomaly);
    }

    interface AnomalyDetectorBuilder {
        AnomalyDetector buildFrom(KeyValue keyValue, AnomalyListener anomalyListener) throws Exception;
    }

    private static class AnomalyMatcher implements ComparablesMatcher<InstantAndText> {
        @Override
        public int compare(InstantAndText o1, InstantAndText o2) {
            String anomaly1 = o1.getText();
            String anomaly2 = o2.getText();
            try {
                boolean same = new com.agt.ferromatikdata.anomalydetector.AnomalyMatcher(anomaly1, anomaly2).isSame();
                return same ? 0 : anomaly1.compareTo(anomaly2);
            } catch (Exception e) {
                return -1;
            }
        }
    }
}