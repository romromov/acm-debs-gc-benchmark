package com.agtinternational.hobbit.core;

import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author Roman Katerinenko
 */
public class SystemComponent extends AbstractCommandReceivingComponent {
    private static final Logger logger = LoggerFactory.getLogger(SystemComponent.class);

    private final AbstractCommunicationProtocol communicationProtocol;
    private final CountDownLatch startExecutionBarrier = new CountDownLatch(1);

    public SystemComponent(AbstractCommunicationProtocol communicationProtocol) {
        this.communicationProtocol = communicationProtocol;
    }

    @Override
    public void init() throws Exception {
        super.init();
        String hobbitSessionId = getHobbitSessionId();
        if (hobbitSessionId.equals(Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS) ||
                hobbitSessionId.equals(Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS)) {
            throw new IllegalStateException("Wrong hobbit session id. It must not be equal to HOBBIT_SESSION_ID_FOR_BROADCASTS or HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS");
        }
    }

    @Override
    public void run() throws Exception {
        logger.debug("Sending SYSTEM_READY_SIGNAL...");
        sendToCmdQueue(Commands.SYSTEM_READY_SIGNAL);
        logger.debug("Waiting for TASK_GENERATION_FINISHED...");
        startExecutionBarrier.await();
        logger.debug("Executing communication protocol...");
        communicationProtocol.execute();
        logger.debug("Finished");
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        if (command == Commands.TASK_GENERATION_FINISHED) {
            startExecutionBarrier.countDown();
        }
    }
}