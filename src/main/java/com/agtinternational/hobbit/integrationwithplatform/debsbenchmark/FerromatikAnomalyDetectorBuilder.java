package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agt.ferromatikdata.anomalydetector.AnomalyDetector;
import com.agt.ferromatikdata.anomalydetector.GeneratedDatasetConstants;
import com.agt.ferromatikdata.anomalydetector.Metadata;
import com.agt.ferromatikdata.anomalydetector.MetadataProducer;
import com.agt.ferromatikdata.clustering.DebsClusteringFactory;
import com.agt.ferromatikdata.core.Machines;
import com.agtinternational.hobbit.core.KeyValue;

import java.io.IOException;

import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MAX_CLUSTER_ITERATIONS_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TRANSITIONS_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.WINDOW_SIZE_INPUT_NAME;

/**
 * @author Roman Katerinenko
 */
class FerromatikAnomalyDetectorBuilder {
    public AnomalyDetector buildFrom(
            KeyValue inputParams,
            com.agt.ferromatikdata.anomalydetector.AnomalyDetector.AnomalyListener anomalyListener)
            throws IOException, ClassNotFoundException {
        return new com.agt.ferromatikdata.anomalydetector.AnomalyDetectorImpl.Builder()
                .metadata(createMetadata(inputParams))
                .anomalyListener(anomalyListener)
                .transitionsAmount(inputParams.getIntValueFor(TRANSITIONS_COUNT_INPUT_NAME))
                .maxClusteringIterations(inputParams.getIntValueFor(MAX_CLUSTER_ITERATIONS_INPUT_NAME))
                .clusteringPrecision(0.00001)
                .clusteringFactory(new DebsClusteringFactory())
                .windowSize(inputParams.getIntValueFor(WINDOW_SIZE_INPUT_NAME))
                .nonDoubleDimensionIds(GeneratedDatasetConstants.MACHINE_DATE_TIME_TEXT_DIMENSIONS.getAllIds())
                .build();
    }

    private static Metadata createMetadata(KeyValue inputParams) throws IOException, ClassNotFoundException {
        double probabilityThreshold = inputParams.getDoubleValueFor(PROBABILITY_THRESHOLD_INPUT_NAME);
        Machines machines = new DebsMachinesInputHandler(inputParams).getMachines();
        MetadataProducer metadataProvider = new MetadataProducer(machines, probabilityThreshold);
        metadataProvider.run();
        return metadataProvider.getMetadata();
    }
}