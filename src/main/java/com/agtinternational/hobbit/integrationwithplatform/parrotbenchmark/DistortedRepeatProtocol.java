package com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark;

import com.agtinternational.hobbit.integrationwithplatform.correctnesscheck.CorrectnessSystemProtocol;
import com.agtinternational.hobbit.io.NetworkCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roman Katerinenko
 */
class DistortedRepeatProtocol extends CorrectnessSystemProtocol {
    private static final Logger logger = LoggerFactory.getLogger(DistortedRepeatProtocol.class);

    DistortedRepeatProtocol(NetworkCommunication.Builder communicationBuilder) {
        super(communicationBuilder);
    }

    @Override
    protected void waitForFinishingProcessing() throws InterruptedException {
        // do nothing
    }

    @Override
    protected void handleDelivery(byte[] bytes) {
        try {
            String receiveMessage = new String(bytes, getCharset());
            logger.debug("Received message: {}", receiveMessage);
            String distortedMessage = receiveMessage + "_distorted";
            logger.debug("Sending distorted message: {}", distortedMessage);
            sendBytes(distortedMessage.getBytes(getCharset()));
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }
}