package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agt.ferromatikdata.formatting.RdfAnomalyFormatter;
import com.agtinternational.hobbit.core.KeyValue;

import java.nio.charset.Charset;
import java.time.Instant;

/**
 * @author Roman Katerinenko
 */
class DebsAnomalyDetectorBuilder implements DebsDataChecker.AnomalyDetectorBuilder {
    public DebsDataChecker.AnomalyDetector buildFrom(
            KeyValue keyValue,
            DebsDataChecker.AnomalyListener listener) throws Exception {
        AnomalyDetectorImpl anomalyDetector = new AnomalyDetectorImpl(keyValue, listener);
        anomalyDetector.init();
        return anomalyDetector;
    }

    private static class AnomalyDetectorImpl implements DebsDataChecker.AnomalyDetector {
        private static final Charset CHARSET = Charset.forName("UTF-8");

        private final KeyValue inputParams;
        private final RdfAnomalyFormatter formatter = new RdfAnomalyFormatter(CHARSET);
        private final com.agt.ferromatikdata.anomalydetector.AnomalyDetector.AnomalyListener delegateListener;

        private com.agt.ferromatikdata.anomalydetector.AnomalyDetector delegateAnomalyDetector;
        private Instant lastDataPointInstant;

        private AnomalyDetectorImpl(KeyValue inputParams, DebsDataChecker.AnomalyListener listener) {
            this.inputParams = inputParams;
            delegateListener = anomaly -> {
                String anomalyText = formatter.format(anomaly);
                InstantAndText anomalyWithInstant = new InstantAndText(lastDataPointInstant, anomalyText);
                listener.onNewAnomaly(anomalyWithInstant);
            };
        }

        private void init() throws Exception {
            formatter.init();
            delegateAnomalyDetector = new FerromatikAnomalyDetectorBuilder()
                    .buildFrom(inputParams, delegateListener);
        }

        @Override
        public void addNewDataPoint(InstantAndText dataPoint) {
            lastDataPointInstant = Instant.now();
            delegateAnomalyDetector.addNewDataPoint(dataPoint.getText());
        }
    }
}