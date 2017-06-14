package com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark;

import com.agtinternational.hobbit.integrationwithplatform.correctnesscheck.CorrectnessSystemProtocol;
import com.agtinternational.hobbit.io.NetworkCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roman Katerinenko
 */
class RepeatProtocol extends CorrectnessSystemProtocol {
    private static final Logger logger = LoggerFactory.getLogger(RepeatProtocol.class);

    RepeatProtocol(NetworkCommunication.Builder communicationBuilder) {
        super(communicationBuilder);
    }

    @Override
    protected void waitForFinishingProcessing() throws InterruptedException {
        // do nothing
    }

    @Override
    protected void handleDelivery(byte[] bytes) {
        try {
            logger.debug("Repeating message: {}", new String(bytes, getCharset()));
            sendBytes(bytes);
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }
}