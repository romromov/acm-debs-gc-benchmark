package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.BenchmarkTask;
import com.agtinternational.hobbit.core.KeyValue;
import com.agtinternational.hobbit.core.SystemComponent;
import com.agtinternational.hobbit.core.TaskBasedBenchmarkController;
import com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsCsvSystem;
import com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsCsvSystemNegative;
import com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsTask;
import com.agtinternational.hobbit.testutils.CommandQueueListener;
import com.agtinternational.hobbit.testutils.ComponentsExecutor;
import com.agtinternational.hobbit.testutils.ContainerSimulatedComponent;
import com.agtinternational.hobbit.testutils.commandreactions.StartBenchmarkWhenSystemAndBenchmarkReady;
import com.agtinternational.hobbit.testutils.commandreactions.TerminateServicesWhenBenchmarkControllerFinished;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.hobbit.core.Commands;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static com.agtinternational.hobbit.deployment.CommonConstants.RABBIT_MQ_CONTAINER_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_COUNT_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_SUCCESS;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.BENCHMARK_MODE_STATIC;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.BENCHMARK_MODE_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.DATA_POINT_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.EXPECTED_ANOMALIES_COUNT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.EXPECTED_DATA_POINTS_COUNT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.FORMAT_CSV;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.FORMAT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.FORMAT_RDF;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.INTERVAL_NANOS_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.INTERVAL_NANOS_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MACHINE_COUNT_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MACHINE_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MAX_CLUSTER_ITERATIONS_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MAX_CLUSTER_ITERATIONS_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.NO_TIMEOUT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.SEED_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.SEED_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TERMINATION_TYPE_NORMAL;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TERMINATION_TYPE_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.THROUGHPUT_BYTES_PER_SEC_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TIMEOUT_MINUTES_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TRANSITIONS_COUNT_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TRANSITIONS_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.WINDOW_SIZE_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.WINDOW_SIZE_INPUT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Roman Katerinenko
 */
@RunWith(Parameterized.class)
public class DebsBenchmarkTest extends EnvironmentVariables {
    private static final String EXPERIMENT_URI = "http://agt.com/exp1";
    private static final String SYSTEM_URI = "http://agt.com/systems#sys10";
    private static final String SESSION_ID = EXPERIMENT_URI;
    private static final String RABBIT_HOST_NAME = "127.0.0.1";
    private static final String SYSTEM_CONTAINER_ID = "anythingGoesHere-weDontCheck";

    private enum SystemType {
        POSITIVE,
        NEGATIVE
    }

    private int benchmarkOutputFormat;
    private boolean testShouldPass;
    private SystemType systemType;

    @Parameterized.Parameters
    public static Collection parameters() throws Exception {
        return Arrays.asList(new Object[][]{
                {FORMAT_CSV, false, SystemType.NEGATIVE},
                {FORMAT_CSV, true, SystemType.POSITIVE},
                {FORMAT_RDF, false, SystemType.POSITIVE}
        });
    }

    public DebsBenchmarkTest(int benchmarkOutputFormat, boolean testShouldPass, SystemType systemType) {
        this.benchmarkOutputFormat = benchmarkOutputFormat;
        this.testShouldPass = testShouldPass;
        this.systemType = systemType;
    }

    @Test
    public void checkAnomaliesMatch() throws InterruptedException, TimeoutException, DockerCertificateException, DockerException {
        RabbitMqDockerizer rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .host(RABBIT_HOST_NAME)
                .containerName(RABBIT_MQ_CONTAINER_NAME)
                .build();
        rabbitMqDockerizer.run();
        rabbitMqDockerizer.waitUntilRunning();
        setupCommunicationEnvironmentVariables(RABBIT_HOST_NAME, SESSION_ID);
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupSystemEnvironmentVariables(SYSTEM_URI);
        ComponentsExecutor executor = new ComponentsExecutor();
        CommandQueueListener commandQueue = new CommandQueueListener();
        commandQueue.setCommandReactions(
                new TerminateServicesWhenBenchmarkControllerFinished(commandQueue, executor) {
                    @Override
                    public void accept(Byte command, byte[] data) {
                        if (command == Commands.BENCHMARK_FINISHED_SIGNAL) {
                            DebsBenchmarkTest.this.checkBenchmarkResult(data);
                        }
                        super.accept(command, data);
                    }
                },
                new StartBenchmarkWhenSystemAndBenchmarkReady(SYSTEM_CONTAINER_ID));
        executor.submit(commandQueue);
        commandQueue.waitForInitialisation();
        //
        KeyValue inputParams = createTaskParameters();
        BenchmarkTask task = new DebsTask(inputParams);
        int timeout = inputParams.getIntValueFor(TIMEOUT_MINUTES_INPUT_NAME);
        executor.submit(new TaskBasedBenchmarkController(timeout, task));
        executor.submit(new ContainerSimulatedComponent(newSystem(), SYSTEM_CONTAINER_ID));
        //
        commandQueue.waitForTermination();
        assertFalse(executor.anyExceptions());
        assertTrue(task.isSuccessful() == testShouldPass);
        rabbitMqDockerizer.stopAndRemoveContainer();
    }

    private SystemComponent newSystem() {
        KeyValue systemParameters = createSystemParameters();
        if (systemType == SystemType.POSITIVE) {
            return new DebsCsvSystem(systemParameters);
        } else {
            return new DebsCsvSystemNegative(systemParameters);
        }
    }

    private void checkBenchmarkResult(byte[] bytes) {
        JenaKeyValue keyValue = new JenaKeyValue.Builder().buildFrom(bytes);
        String matchResult = keyValue.getStringValueFor(ANOMALY_MATCH_OUTPUT_NAME);
        assertTrue(ANOMALY_MATCH_SUCCESS.equals(matchResult) == testShouldPass);
        int matchedDataPoints = keyValue.getIntValueFor(ANOMALY_MATCH_COUNT_OUTPUT_NAME);
        assertTrue((EXPECTED_ANOMALIES_COUNT == matchedDataPoints) == testShouldPass);
        double throughput = keyValue.getDoubleValueFor(THROUGHPUT_BYTES_PER_SEC_OUTPUT_NAME);
        assertTrue(Double.compare(throughput, .0) >= 0);
        String actualTermination = keyValue.getStringValueFor(TERMINATION_TYPE_OUTPUT_NAME);
        assertEquals(TERMINATION_TYPE_NORMAL, actualTermination);
    }

    private KeyValue createTaskParameters() {
        KeyValue kv = new KeyValue();
        kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_STATIC);
        kv.setValue(TIMEOUT_MINUTES_INPUT_NAME, NO_TIMEOUT);
        kv.setValue(DATA_POINT_COUNT_INPUT_NAME, EXPECTED_DATA_POINTS_COUNT);
        kv.setValue(MACHINE_COUNT_INPUT_NAME, MACHINE_COUNT_DEFAULT);
        kv.setValue(PROBABILITY_THRESHOLD_INPUT_NAME, PROBABILITY_THRESHOLD_DEFAULT);
        kv.setValue(WINDOW_SIZE_INPUT_NAME, WINDOW_SIZE_DEFAULT);
        kv.setValue(TRANSITIONS_COUNT_INPUT_NAME, TRANSITIONS_COUNT_DEFAULT);
        kv.setValue(MAX_CLUSTER_ITERATIONS_INPUT_NAME, MAX_CLUSTER_ITERATIONS_DEFAULT);
        kv.setValue(INTERVAL_NANOS_INPUT_NAME, INTERVAL_NANOS_DEFAULT);
        kv.setValue(SEED_INPUT_NAME, SEED_DEFAULT);
        kv.setValue(FORMAT_INPUT_NAME, benchmarkOutputFormat);
        return kv;
    }

    private static KeyValue createSystemParameters() {
        KeyValue kv = new JenaKeyValue();
        kv.setValue(MACHINE_COUNT_INPUT_NAME, MACHINE_COUNT_DEFAULT);
        kv.setValue(PROBABILITY_THRESHOLD_INPUT_NAME, PROBABILITY_THRESHOLD_DEFAULT);
        kv.setValue(WINDOW_SIZE_INPUT_NAME, WINDOW_SIZE_DEFAULT);
        kv.setValue(TRANSITIONS_COUNT_INPUT_NAME, TRANSITIONS_COUNT_DEFAULT);
        kv.setValue(MAX_CLUSTER_ITERATIONS_INPUT_NAME, MAX_CLUSTER_ITERATIONS_DEFAULT);
        return kv;
    }
}