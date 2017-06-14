package com.agtinternational.hobbit.testutils.commandreactions;

import com.agtinternational.hobbit.testutils.CommandQueueListener;
import com.agtinternational.hobbit.testutils.ComponentsExecutor;
import org.hobbit.core.Commands;
import org.junit.Assert;

/**
 * @author Roman Katerinenko
 */
public class TerminateServicesWhenBenchmarkControllerFinished implements CommandReaction {
    private final CommandQueueListener commandQueueListener;
    private final ComponentsExecutor componentsExecutor;

    public TerminateServicesWhenBenchmarkControllerFinished(CommandQueueListener commandQueueListener,
                                                            ComponentsExecutor componentsExecutor) {
        this.commandQueueListener = commandQueueListener;
        this.componentsExecutor = componentsExecutor;
    }

    @Override
    public void accept(Byte command, byte[] data) {
        if (command == Commands.BENCHMARK_FINISHED_SIGNAL) {
            try {
                commandQueueListener.terminate();
                componentsExecutor.shutdown();
            } catch (InterruptedException e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}