package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.*;
import com.agtinternational.hobbit.testutils.CommandQueueListener;
import com.agtinternational.hobbit.testutils.ComponentsExecutor;
import com.agtinternational.hobbit.testutils.ContainerSimulatedComponent;
import com.agtinternational.hobbit.testutils.commandreactions.StartBenchmarkWhenSystemAndBenchmarkReady;
import com.agtinternational.hobbit.testutils.commandreactions.StartSystemWhenItReady;
import com.agtinternational.hobbit.testutils.commandreactions.TerminateServicesWhenBenchmarkControllerFinished;
import org.hobbit.core.Commands;
import org.hobbit.core.components.Component;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Note requires running Rabbit MQ because parent ({@link AbstractCommunicationProtocol} is coupled to RabbitMQ).
 *
 * @author Roman Katerinenko
 */
public class BenchmarkStartStopTest extends EnvironmentVariables {
    private static final String RABBIT_HOST_NAME = "127.0.0.1";
    private static final String RABBIT_MQ_CONTAINER_NAME = "rabbit";

    private RabbitMqDockerizer rabbitMqDockerizer;

    @Before
    public void before() throws Exception {
        rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .containerName(RABBIT_MQ_CONTAINER_NAME)
                .host(RABBIT_HOST_NAME)
                .networks(CommonConstants.HOBBIT_NETWORK_NAME, CommonConstants.HOBBIT_CORE_NETWORK_NAME)
                .build();
        rabbitMqDockerizer.run();
        rabbitMqDockerizer.waitUntilRunning();
    }

    @After
    public void after() throws Exception {
        rabbitMqDockerizer.stopAndRemoveContainer();
    }

    @Test
    public void checkBenchmarkStartStopCorrectly() throws Exception {
        String experimentId = "http://example.com/exp1";
        String systemUri = "http://agt.com/systems#sys122";
        setupCommunicationEnvironmentVariables(RABBIT_HOST_NAME, experimentId);
        setupBenchmarkEnvironmentVariables(experimentId);
        setupSystemEnvironmentVariables(systemUri);
        ComponentsExecutor componentsExecutor = new ComponentsExecutor();
        CommandQueueListener commandQueueListener = new CommandQueueListener();
        String systemContainerId = "1234kj34k";
        commandQueueListener.setCommandReactions(
                new TerminateServicesWhenBenchmarkControllerFinished(commandQueueListener, componentsExecutor),
                new StartBenchmarkWhenSystemAndBenchmarkReady(systemContainerId));
        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();
        TaskBasedBenchmarkController benchmarkController = new TaskBasedBenchmarkController();
        componentsExecutor.submit(benchmarkController);
        Component system = new SystemComponent(new DummyProtocol());
        ContainerSimulatedComponent containerComponent = new ContainerSimulatedComponent(system, systemContainerId);
        componentsExecutor.submit(containerComponent);
        commandQueueListener.waitForTermination();
        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    @Test
    public void checkBenchmarkControllerAndSystemStartStopCorrectly() throws Exception {
        String experimentId = "exp1";
        String systemUri = "http://agt.com/systems#sys123332";
        setupCommunicationEnvironmentVariables(RABBIT_HOST_NAME, experimentId);
        setupBenchmarkEnvironmentVariables(experimentId);
        setupSystemEnvironmentVariables(systemUri);
        ComponentsExecutor componentsExecutor = new ComponentsExecutor();
        CommandQueueListener commandQueueListener = new CommandQueueListener();
        String systemContainerId = "1234kj34k";
        commandQueueListener.setCommandReactions(
                new TerminateServicesWhenBenchmarkControllerFinished(commandQueueListener, componentsExecutor),
                new StartBenchmarkWhenSystemAndBenchmarkReady(systemContainerId));
        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();
        TaskBasedBenchmarkController benchmarkController = new TaskBasedBenchmarkController();
        componentsExecutor.submit(benchmarkController);
        Component system = new SystemComponent(new DummyProtocol());
        ContainerSimulatedComponent containerComponent = new ContainerSimulatedComponent(system, systemContainerId);
        componentsExecutor.submit(containerComponent);
        commandQueueListener.waitForTermination();
        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    @Test
    public void checkBenchmarkedSystemStartStopCorrectly() throws Exception {
        String experimentId = "exp144";
        setupCommunicationEnvironmentVariables(RABBIT_HOST_NAME, experimentId);
        ComponentsExecutor componentsExecutor = new ComponentsExecutor();
        CommandQueueListener commandQueueListener = new CommandQueueListener();
        String systemUri = "System1";
        commandQueueListener.setCommandReactions(new StartSystemWhenItReady(),
                (command, data) -> {
                    if (checkIfContainerTerminated(command, data, systemUri)) {
                        try {
                            commandQueueListener.terminate();
                            componentsExecutor.shutdown();
                        } catch (InterruptedException e) {
                            Assert.fail(e.getMessage());
                        }
                    }
                });
        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();
        setupSystemEnvironmentVariables(systemUri);
        SystemComponent system = new SystemComponent(new DummyProtocol());
        ContainerSimulatedComponent containerComponent = new ContainerSimulatedComponent(system, systemUri);
        componentsExecutor.submit(containerComponent);
        commandQueueListener.waitForTermination();
        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    private boolean checkIfContainerTerminated(byte command, byte[] data, String containerName) {
        if (command == Commands.DOCKER_CONTAINER_TERMINATED) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            String stringValue = RabbitMQUtils.readString(byteBuffer);
            return stringValue.equals(containerName);
        }
        return false;
    }

    private static class DummyProtocol extends AbstractCommunicationProtocol {
        private DummyProtocol() {
            super(null);
        }

        @Override
        public void execute() {
            // do nothing
        }
    }
}