package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

/**
 * @author Roman Katerinenko
 */
public class DebsConstants {
    public static final String PROBABILITY_THRESHOLD_INPUT_NAME = "http://www.debs2017.org/gc/probabilityThreshold";
    public static final String DATA_POINT_COUNT_INPUT_NAME = "http://www.debs2017.org/gc/dataPointCount";
    public static final String WINDOW_SIZE_INPUT_NAME = "http://www.debs2017.org/gc/windowSize";
    public static final String TRANSITIONS_COUNT_INPUT_NAME = "http://www.debs2017.org/gc/transitionsCount";
    public static final String MAX_CLUSTER_ITERATIONS_INPUT_NAME = "http://www.debs2017.org/gc/maxClusterIterations";
    public static final String INTERVAL_NANOS_INPUT_NAME = "http://www.debs2017.org/gc/interval";
    public static final String SEED_INPUT_NAME = "http://www.debs2017.org/gc/seed";
    public static final String FORMAT_INPUT_NAME = "http://www.debs2017.org/gc/format";
    public static final String MACHINE_COUNT_INPUT_NAME = "http://www.debs2017.org/gc/machineCount";
    public static final String TIMEOUT_MINUTES_INPUT_NAME = "http://www.debs2017.org/gc/timeoutMinutes";
    public static final String BENCHMARK_MODE_INPUT_NAME = "http://www.debs2017.org/gc/benchmarkMode";
    //
    public static final String DEBS_KPI_OUTPUT_NAME = "http://www.debs2017.org/gc/debsKpi";
    public static final String ANOMALY_MATCH_COUNT_OUTPUT_NAME = "http://www.debs2017.org/gc/matchedAnomaliesCount";
    public static final String ANOMALY_MATCH_OUTPUT_NAME = "http://www.debs2017.org/gc/anomalyMatch";
    public static final String AVERAGE_LATENCY_NANOS_OUTPUT_NAME = "http://www.debs2017.org/gc/averageLatencyNanos";
    public static final String THROUGHPUT_BYTES_PER_SEC_OUTPUT_NAME = "http://www.debs2017.org/gc/throughputBytesPerSecond";
    public static final String TERMINATION_TYPE_OUTPUT_NAME = "http://www.debs2017.org/gc/terminationType";
    //
    public static final String BENCHMARK_MODE_STATIC = "static";
    public static final String BENCHMARK_MODE_DEBS_DYNAMIC = "dynamic";
    public static final String TERMINATION_TYPE_NORMAL = "Terminated correctly.";
    public static final String ANOMALY_MATCH_SUCCESS = "Anomalies matched successfully";
    public static final double PROBABILITY_THRESHOLD_DEFAULT = 0.005;
    public static final int MACHINE_COUNT_DEFAULT = 1;
    public static final int WINDOW_SIZE_DEFAULT = 10;
    public static final int TRANSITIONS_COUNT_DEFAULT = 5;
    public static final int MAX_CLUSTER_ITERATIONS_DEFAULT = 50;
    public static final int INTERVAL_NANOS_DEFAULT = 10;
    public static final int SEED_DEFAULT = 123;
    public static final int FORMAT_RDF = 0;
    public static final int FORMAT_CSV = 1;
    public static final int NO_TIMEOUT = -1;
    //
    private static final int BYTES_IN_ONE_MEASUREMENT_NTRIPLES = 164096;
    public static final int EXPECTED_DATA_POINTS_COUNT = 10 * 1024 * 1024 / BYTES_IN_ONE_MEASUREMENT_NTRIPLES; //63
    public static final int EXPECTED_ANOMALIES_COUNT = 3;

    private DebsConstants() {
    }
}