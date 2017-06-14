package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agtinternational.hobbit.core.KeyValue;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Roman Katerinenko
 */
public class DebsAnomalyMatcherTest {
    private final String goldAnomalyAndDataPointText = "<http://project-hobbit.eu/resources/debs2017#Anomaly_0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.agtinternational.com/ontologies/DEBSAnalyticResults#Anomaly> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_0> <http://www.agtinternational.com/ontologies/I4.0#machine> <http://www.agtinternational.com/ontologies/WeidmullerMetadata#Machine_59> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_0> <http://www.agtinternational.com/ontologies/DEBSAnalyticResults#inAbnormalDimension> <http://www.agtinternational.com/ontologies/WeidmullerMetadata#_59_109> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_0> <http://www.agtinternational.com/ontologies/DEBSAnalyticResults#hasTimeStamp> <http://project-hobbit.eu/resources/debs2017#Timestamp_27> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_0> <http://www.agtinternational.com/ontologies/DEBSAnalyticResults#hasProbabilityOfObservedAbnormalSequence> \"0.003472222222222222\"^^<http://www.w3.org/2001/XMLSchema#double> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.agtinternational.com/ontologies/DEBSAnalyticResults#Anomaly> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_1> <http://www.agtinternational.com/ontologies/I4.0#machine> <http://www.agtinternational.com/ontologies/WeidmullerMetadata#Machine_59> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_1> <http://www.agtinternational.com/ontologies/DEBSAnalyticResults#inAbnormalDimension> <http://www.agtinternational.com/ontologies/WeidmullerMetadata#_59_30> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_1> <http://www.agtinternational.com/ontologies/DEBSAnalyticResults#hasTimeStamp> <http://project-hobbit.eu/resources/debs2017#Timestamp_92> .\n" +
            "<http://project-hobbit.eu/resources/debs2017#Anomaly_1> <http://www.agtinternational.com/ontologies/DEBSAnalyticResults#hasProbabilityOfObservedAbnormalSequence> \"0.003472222222222222\"^^<http://www.w3.org/2001/XMLSchema#double> .";

    private Instant abnormalDataPointInstant = Instant.now();
    private Instant actualAnomalyInstant = abnormalDataPointInstant.plusMillis(10);

    @Test
    public void checkLatencyCalculatedCorrectly() {
        String actualAnomalyText = goldAnomalyAndDataPointText;
        DebsDataChecker dataChecker = new DebsDataChecker(new KeyValue(), new DataPointAsAnomaly());
        dataChecker.addDataPoint(new InstantAndText(abnormalDataPointInstant, goldAnomalyAndDataPointText));
        dataChecker.addActual(new InstantAndText(actualAnomalyInstant, actualAnomalyText));
        Duration expectedLatency = Duration.between(abnormalDataPointInstant, actualAnomalyInstant);
        assertTrue(expectedLatency.compareTo(dataChecker.getAverageLatency()) == 0);
    }

    @Test
    public void checkWrongAnomalyMatch() {
        String actualAnomalyText = goldAnomalyAndDataPointText + "Junk";
        DebsDataChecker dataChecker = new DebsDataChecker(new KeyValue(), new DataPointAsAnomaly());
        dataChecker.addDataPoint(new InstantAndText(abnormalDataPointInstant, goldAnomalyAndDataPointText));
        dataChecker.addActual(new InstantAndText(actualAnomalyInstant, actualAnomalyText));
        assertNull(dataChecker.getAverageLatency());
    }

    @Test
    public void checkSequenceOfAnomalies() {
        String actualAnomalyText = goldAnomalyAndDataPointText;
        long gapSeconds = 5;
        DebsDataChecker dataChecker = new DebsDataChecker(new KeyValue(), new DataPointAsAnomaly());
        Instant dataPoint1Instant = Instant.now();
        dataChecker.addDataPoint(new InstantAndText(dataPoint1Instant, goldAnomalyAndDataPointText));
        Instant dataPoint2Instant = dataPoint1Instant.plusSeconds(2);
        dataChecker.addDataPoint(new InstantAndText(dataPoint2Instant, goldAnomalyAndDataPointText));
        Instant dataPoint3Instant = dataPoint1Instant.plusSeconds(3);
        dataChecker.addDataPoint(new InstantAndText(dataPoint3Instant, goldAnomalyAndDataPointText));
        //
        dataChecker.addActual(new InstantAndText(dataPoint1Instant.plusSeconds(gapSeconds), actualAnomalyText));
        dataChecker.addActual(new InstantAndText(dataPoint2Instant.plusSeconds(gapSeconds), actualAnomalyText));
        dataChecker.addActual(new InstantAndText(dataPoint3Instant.plusSeconds(gapSeconds), actualAnomalyText));
        long averageSeconds = (long) (gapSeconds * 3. / 3.);
        Duration expectedLatency = Duration.ofSeconds(averageSeconds);
        assertTrue(expectedLatency.compareTo(dataChecker.getAverageLatency()) == 0);
    }

    private static class DataPointAsAnomaly implements DebsDataChecker.AnomalyDetectorBuilder {
        @Override
        public DebsDataChecker.AnomalyDetector buildFrom(KeyValue keyValue, DebsDataChecker.AnomalyListener anomalyListener) throws Exception {
            return anomalyListener::onNewAnomaly;
        }
    }
}