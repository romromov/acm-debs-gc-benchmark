package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;

import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.BENCHMARK_MODE_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.BENCHMARK_MODE_STATIC;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.DATA_POINT_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.EXPECTED_DATA_POINTS_COUNT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.FORMAT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.INTERVAL_NANOS_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.INTERVAL_NANOS_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MACHINE_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MAX_CLUSTER_ITERATIONS_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MAX_CLUSTER_ITERATIONS_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.NO_TIMEOUT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.SEED_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.SEED_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TIMEOUT_MINUTES_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TRANSITIONS_COUNT_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TRANSITIONS_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.WINDOW_SIZE_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.WINDOW_SIZE_INPUT_NAME;
import static org.hobbit.core.Constants.BENCHMARK_PARAMETERS_MODEL_KEY;

/**
 * @author Roman Katerinenko
 */
abstract class BaseDebsBenchmarkBuilder extends BaseDebsBuilder {
    private int benchmarkOutputFormat;

    BaseDebsBenchmarkBuilder(String dockerizerName) {
        super(dockerizerName);
    }

    @Override
    BaseBuilder parameters(String parameters) {
        addEnvironmentVariable(BENCHMARK_PARAMETERS_MODEL_KEY, parameters);
        return this;
    }


    BaseDebsBenchmarkBuilder benchmarkOutputFormat(int benchmarkOutputFormat) {
        this.benchmarkOutputFormat = benchmarkOutputFormat;
        return this;
    }

    private String createParameters() {
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(DATA_POINT_COUNT_INPUT_NAME, EXPECTED_DATA_POINTS_COUNT);
        kv.setValue(PROBABILITY_THRESHOLD_INPUT_NAME, PROBABILITY_THRESHOLD_DEFAULT);
        kv.setValue(WINDOW_SIZE_INPUT_NAME, WINDOW_SIZE_DEFAULT);
        kv.setValue(TRANSITIONS_COUNT_INPUT_NAME, TRANSITIONS_COUNT_DEFAULT);
        kv.setValue(MAX_CLUSTER_ITERATIONS_INPUT_NAME, MAX_CLUSTER_ITERATIONS_DEFAULT);
        kv.setValue(INTERVAL_NANOS_INPUT_NAME, INTERVAL_NANOS_DEFAULT);
        kv.setValue(SEED_INPUT_NAME, SEED_DEFAULT);
        kv.setValue(FORMAT_INPUT_NAME, benchmarkOutputFormat);
        kv.setValue(MACHINE_COUNT_INPUT_NAME, 1);
        kv.setValue(TIMEOUT_MINUTES_INPUT_NAME, NO_TIMEOUT);
        kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_STATIC);
        return kv.encodeToString();
    }

    @Override
    Dockerizer build() {
        runnerClass(DebsBenchmarkDockerRunner.class);
        addEnvironmentVariable(BENCHMARK_PARAMETERS_MODEL_KEY, createParameters());
        return super.build();
    }
}