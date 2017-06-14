package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.BenchmarkTask;
import com.agtinternational.hobbit.core.KeyValue;
import com.agtinternational.hobbit.core.TaskBasedBenchmarkController;
import com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark.ParrotSystemComponent;
import com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark.ParrotTask;
import com.agtinternational.hobbit.testutils.CommandQueueListener;
import com.agtinternational.hobbit.testutils.ComponentsExecutor;
import com.agtinternational.hobbit.testutils.ContainerSimulatedComponent;
import com.agtinternational.hobbit.testutils.commandreactions.StartBenchmarkWhenSystemAndBenchmarkReady;
import com.agtinternational.hobbit.testutils.commandreactions.TerminateServicesWhenBenchmarkControllerFinished;
import org.hobbit.core.Commands;
import org.junit.Assert;
import org.junit.Test;

import static com.agtinternational.hobbit.deployment.CommonConstants.RABBIT_MQ_CONTAINER_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Parrot test is when the system should respond with the same messages it receives from the core.
 *
 * @author Roman Katerinenko
 */
public class ParrotBenchmarkTest extends EnvironmentVariables {
    private static final String EXPERIMENT_URI = "exp1";
    private static final String SYSTEM_URI = "http://agt.com/systems#sys10";
    private static final String SESSION_ID = EXPERIMENT_URI;
    private static final String RABBIT_HOST_NAME = "127.0.0.1";
    private static final String SYSTEM_CONTAINER_ID = "1234kj34k";
    private static final int EXPECTED_MESSAGES_COUNT = 12;

    private boolean messageCountMatch;

    @Test
    public void systemRespondsWithReceivedData() throws Exception {
        RabbitMqDockerizer rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .host(RABBIT_HOST_NAME)
                .containerName(RABBIT_MQ_CONTAINER_NAME)
                .build();
        rabbitMqDockerizer.run();
        rabbitMqDockerizer.waitUntilRunning();
        setupCommunicationEnvironmentVariables(RABBIT_HOST_NAME, SESSION_ID);
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupSystemEnvironmentVariables(SYSTEM_URI);
        ComponentsExecutor componentsExecutor = new ComponentsExecutor();
        CommandQueueListener commandQueueListener = new CommandQueueListener();
        commandQueueListener.setCommandReactions(
                new TerminateServicesWhenBenchmarkControllerFinished(commandQueueListener, componentsExecutor) {
                    @Override
                    public void accept(Byte command, byte[] data) {
                        if (command == Commands.BENCHMARK_FINISHED_SIGNAL) {
                            ParrotBenchmarkTest.this.checkBenchmarkResult(data);
                        }
                        super.accept(command, data);
                    }
                },
                new StartBenchmarkWhenSystemAndBenchmarkReady(SYSTEM_CONTAINER_ID));
        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();
        BenchmarkTask benchmarkTask = new ParrotTask(createTaskParameters());
        TaskBasedBenchmarkController benchmarkController = new TaskBasedBenchmarkController(benchmarkTask);
        componentsExecutor.submit(benchmarkController);
        ContainerSimulatedComponent containerComponent = new ContainerSimulatedComponent(new ParrotSystemComponent(), SYSTEM_CONTAINER_ID);
        componentsExecutor.submit(containerComponent);
        //
        commandQueueListener.waitForTermination();
        assertFalse(componentsExecutor.anyExceptions());
        assertTrue(benchmarkTask.isSuccessful());
        Assert.assertTrue(messageCountMatch);
        rabbitMqDockerizer.stopAndRemoveContainer();
    }

    private void checkBenchmarkResult(byte[] bytes) {
        JenaKeyValue keyValue = new JenaKeyValue.Builder().buildFrom(bytes);
        int actualValue = keyValue.getIntValueFor(ParrotTask.CHECKED_MESSAGES_OUTPUT_NAME);
        messageCountMatch = EXPECTED_MESSAGES_COUNT == actualValue;
    }

    private static KeyValue createTaskParameters() {
        KeyValue keyValue = new KeyValue();
        keyValue.setValue(ParrotTask.MESSAGE_COUNT_INPUT_NAME, EXPECTED_MESSAGES_COUNT);
        return keyValue;
    }
}