package com.agtinternational.hobbit.core;

import com.agtinternational.hobbit.io.NetworkCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

/**
 * Incapsulates communication objects completely.
 * Uses special message to indicate communication termination.
 *
 * @author Roman Katerinenko
 */
public abstract class TerminationMessageProtocol extends AbstractCommunicationProtocol {
    private static final Logger logger = LoggerFactory.getLogger(TerminationMessageProtocol.class);

    private Communication outputCommunication;
    private Communication inputCommunication;

    private final String terminationMessage;
    private final CountDownLatch terminationMessageBarrier = new CountDownLatch(1);

    public TerminationMessageProtocol(NetworkCommunication.Builder communicationBuilder, String terminationMessage) {
        super(communicationBuilder);
        this.terminationMessage = terminationMessage;
    }

    private Communication createInputCommunication() throws Exception {
        Communication.Consumer delegate = getInputConsumer();
        return getCommunicationBuilder()
                .host(getInputHost())
                .prefetchCount(getInputPrefetchCount())
                .consumer(new TerminationMessageConsumer(delegate))
                .name(getInputCommunicationName())
                .charset(getCharset())
                .build();
    }

    protected abstract Communication.Consumer getInputConsumer();

    protected abstract String getInputHost();

    protected abstract String getInputCommunicationName();

    protected abstract Charset getCharset();

    protected abstract int getInputPrefetchCount();

    private Communication createOutputCommunication() throws Exception {
        return getCommunicationBuilder()
                .prefetchCount(getOutputPrefetchCount())
                .host(getOutputHost())
                .consumer(getOutputConsumer())
                .name(getOutputCommunicationName())
                .charset(getCharset())
                .build();
    }

    protected abstract String getOutputCommunicationName();

    protected abstract Communication.Consumer getOutputConsumer();

    protected abstract String getOutputHost();

    protected abstract int getOutputPrefetchCount();

    protected final void sendTerminationMessage() throws Exception {
        logger.debug("Sending termination message to: {} sender: {}", outputCommunication.getName(), this);
        outputCommunication.send(terminationMessage);
    }

    @Override
    public final void execute() throws Exception {
        try {
            logger.debug("Executing...");
            outputCommunication = createOutputCommunication();
            inputCommunication = createInputCommunication();
            executeProtocol();
        } finally {
            try {
                logger.debug("Closing communications....");
                inputCommunication.close();
                outputCommunication.close();
            } catch (Exception e) {
                logger.debug("Exception", e);
            }
        }
    }

    protected abstract void executeProtocol() throws Exception;

    protected final void waitForTerminationMessage() throws InterruptedException {
        terminationMessageBarrier.await();
    }

    protected final void sendBytes(byte[] bytes) throws Exception {
        outputCommunication.send(bytes);
    }

    protected final void sendString(String string) throws Exception {
        outputCommunication.send(string);
    }

    private class TerminationMessageConsumer implements Communication.Consumer {
        private final Communication.Consumer delegate;

        private TerminationMessageConsumer(Communication.Consumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handleDelivery(byte[] bytes) {
            String str = new String(bytes, getCharset());
            if (terminationMessage.equals(str)) {
                logger.debug("Received termination message from: {} receiver: {}", getInputCommunicationName(), TerminationMessageProtocol.this);
                terminationMessageBarrier.countDown();
            } else {
                delegate.handleDelivery(bytes);
            }
        }
    }

    public class OutputSender implements Communication {
        @Override
        public void close() throws Exception {
        }

        @Override
        public String getName() {
            return "OutputSender";
        }

        @Override
        public Charset getCharset() {
            return TaskBasedBenchmarkController.CHARSET;
        }

        @Override
        public Consumer getConsumer() {
            return null;
        }

        @Override
        public void send(byte[] bytes) throws Exception {
            sendBytes(bytes);
        }

        @Override
        public void send(String string) throws Exception {
            sendString(string);
        }
    }
}