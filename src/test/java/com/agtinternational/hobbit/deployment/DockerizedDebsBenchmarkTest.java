package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;
import com.agtinternational.hobbit.testutils.CommandQueueListener;
import com.agtinternational.hobbit.testutils.ComponentsExecutor;
import com.agtinternational.hobbit.testutils.commandreactions.StartBenchmarkWhenSystemAndBenchmarkReady;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import org.hobbit.core.Commands;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.agtinternational.hobbit.deployment.CommonConstants.HOBBIT_CORE_NETWORK_NAME;
import static com.agtinternational.hobbit.deployment.CommonConstants.HOBBIT_NETWORK_NAME;
import static com.agtinternational.hobbit.deployment.CommonConstants.RABBIT_MQ_CONTAINER_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_COUNT_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_SUCCESS;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.EXPECTED_ANOMALIES_COUNT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.FORMAT_CSV;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.FORMAT_RDF;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MACHINE_COUNT_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MACHINE_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MAX_CLUSTER_ITERATIONS_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MAX_CLUSTER_ITERATIONS_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TRANSITIONS_COUNT_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TRANSITIONS_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.WINDOW_SIZE_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.WINDOW_SIZE_INPUT_NAME;

/**
 * @author Roman Katerinenko
 */
//todo check exactly one or less hobbit and hobbit-core networks exist
@RunWith(Parameterized.class)
public class DockerizedDebsBenchmarkTest extends EnvironmentVariables {
    private static final String SESSION_ID = "http://example.com/sessionId1";
    private static final String FAKE_SYSTEM_URI = "http://example.com/fakeSystemId";
    private static final String RABBIT_HOST = "127.0.0.1";

    private enum SystemType {
        POSITIVE,
        NEGATIVE
    }

    private enum BenchmarkType {
        DEBS,
        ANALYTICS
    }

    private AtomicBoolean correct = new AtomicBoolean(false);
    private int benchmarkOutputFormat;
    private boolean testShouldPass;
    private SystemType systemType;
    private BenchmarkType benchmarkType;

    @Parameterized.Parameters
    public static Collection parameters() throws Exception {
        return Arrays.asList(new Object[][]{
                {FORMAT_CSV, false, SystemType.NEGATIVE, BenchmarkType.ANALYTICS},
                {FORMAT_CSV, true, SystemType.POSITIVE, BenchmarkType.ANALYTICS},
                {FORMAT_RDF, false, SystemType.POSITIVE, BenchmarkType.ANALYTICS},
                {FORMAT_CSV, false, SystemType.NEGATIVE, BenchmarkType.DEBS},
                {FORMAT_CSV, true, SystemType.POSITIVE, BenchmarkType.DEBS},
                {FORMAT_RDF, false, SystemType.POSITIVE, BenchmarkType.DEBS}
        });
    }

    public DockerizedDebsBenchmarkTest(
            int benchmarkOutputFormat, boolean testShouldPass, SystemType systemType, BenchmarkType benchmarkType) {
        this.benchmarkOutputFormat = benchmarkOutputFormat;
        this.testShouldPass = testShouldPass;
        this.systemType = systemType;
        this.benchmarkType = benchmarkType;
    }

    @Test
//    @Ignore
    public void checkBenchmarkResult() throws Exception {
        // todo add check that rabbit docker image exists and stop its container before test
        RabbitMqDockerizer rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .containerName(RABBIT_MQ_CONTAINER_NAME)
                .host(RABBIT_HOST)
                .networks(HOBBIT_NETWORK_NAME, HOBBIT_CORE_NETWORK_NAME)
                .build();
        rabbitMqDockerizer.run();
        rabbitMqDockerizer.waitUntilRunning();
        setupCommunicationEnvironmentVariables(RABBIT_HOST, SESSION_ID);
        ComponentsExecutor executor = new ComponentsExecutor();
        CommandQueueListener commandQueue = new CommandQueueListener();
        CountDownLatch benchmarkFinishedBarrier = new CountDownLatch(1);
        commandQueue.setCommandReactions(
                new StartBenchmarkWhenSystemAndBenchmarkReady(FAKE_SYSTEM_URI),
                (command, modelBytes) -> {
                    if (command == Commands.BENCHMARK_FINISHED_SIGNAL) {
                        correct.set(checkReceivedModel(modelBytes));
                        benchmarkFinishedBarrier.countDown();
                    }
                });
        executor.submit(commandQueue);
        commandQueue.waitForInitialisation();
        Dockerizer benchmarkDockerizer = newBenchmarkDockerizer();
        executor.submit(benchmarkDockerizer);
        Dockerizer systemDockerizer = newSystemDockerizer();
        executor.submit(systemDockerizer);
        benchmarkFinishedBarrier.await();
        try {
            benchmarkDockerizer.waitForContainerFinish();
            systemDockerizer.waitForContainerFinish();
        } catch (ContainerNotFoundException e) {
            // ignore - container already finished
        }
        commandQueue.terminate();
        executor.shutdown();
        rabbitMqDockerizer.stopAndRemoveContainer();
        Assert.assertTrue(correct.get());
        Assert.assertFalse(executor.anyExceptions());
    }

    private Dockerizer newBenchmarkDockerizer() {
        if (benchmarkType == BenchmarkType.DEBS) {
            return new DebsBenchmarkBuilder()
                    .benchmarkOutputFormat(benchmarkOutputFormat)
                    .hobbitSessionId(SESSION_ID)
                    .systemUri(FAKE_SYSTEM_URI)
                    .build();
        } else if (benchmarkType == BenchmarkType.ANALYTICS) {
            return new AnalyticsBenchmarkBuilder()
                    .benchmarkOutputFormat(benchmarkOutputFormat)
                    .hobbitSessionId(SESSION_ID)
                    .systemUri(FAKE_SYSTEM_URI)
                    .build();
        } else {
            return null;
        }
    }

    private Dockerizer newSystemDockerizer() {
        switch (systemType) {
            case POSITIVE:
                return new DebsCsvSystemBuilder()
                        .parameters(newSystemParams())
                        .hobbitSessionId(SESSION_ID)
                        .systemUri(FAKE_SYSTEM_URI)
                        .build();
            case NEGATIVE:
            default:
                return new DebsCsvNegativeSystemBuilder()
                        .parameters(newSystemParams())
                        .hobbitSessionId(SESSION_ID)
                        .systemUri(FAKE_SYSTEM_URI)
                        .build();
        }
    }

    private boolean checkReceivedModel(byte[] bytes) {
        JenaKeyValue keyValue = new JenaKeyValue.Builder().buildFrom(bytes);
        String matchResult = keyValue.getStringValueFor(ANOMALY_MATCH_OUTPUT_NAME);
        int matchedDataPoints = keyValue.getIntValueFor(ANOMALY_MATCH_COUNT_OUTPUT_NAME);
        return (ANOMALY_MATCH_SUCCESS.equals(matchResult)
                && EXPECTED_ANOMALIES_COUNT == matchedDataPoints) == testShouldPass;
    }

    private static String newSystemParams() {
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(PROBABILITY_THRESHOLD_INPUT_NAME, PROBABILITY_THRESHOLD_DEFAULT);
        kv.setValue(WINDOW_SIZE_INPUT_NAME, WINDOW_SIZE_DEFAULT);
        kv.setValue(TRANSITIONS_COUNT_INPUT_NAME, TRANSITIONS_COUNT_DEFAULT);
        kv.setValue(MAX_CLUSTER_ITERATIONS_INPUT_NAME, MAX_CLUSTER_ITERATIONS_DEFAULT);
        kv.setValue(MACHINE_COUNT_INPUT_NAME, MACHINE_COUNT_DEFAULT);
        return kv.encodeToString();
    }
}
