package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.Communication;
import com.agtinternational.hobbit.io.RabbitMqCommunication;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

/**
 * @author Roman Katerinenko
 */
public class CommunicationTest {
    private static final String RABBIT_HOST_NAME = "127.0.0.1";
    private static final String RABBIT_MQ_CONTAINER_NAME = "rabbit";

    private String actualMessage;

    @Test
    public void checkDataTransmission() throws Exception {
        RabbitMqDockerizer rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .host(RABBIT_HOST_NAME)
                .containerName(RABBIT_MQ_CONTAINER_NAME)
                .networks(CommonConstants.HOBBIT_CORE_NETWORK_NAME, CommonConstants.HOBBIT_NETWORK_NAME)
                .build();
        rabbitMqDockerizer.run();
        rabbitMqDockerizer.waitUntilRunning();
        String queueName = "dummyQueue";
        Charset charset = Charset.forName("UTF-8");
        CountDownLatch messageDeliveredLatch = new CountDownLatch(1);
        Communication communication = new RabbitMqCommunication.Builder()
                .host(RABBIT_HOST_NAME)
                .name(queueName)
                .charset(charset)
                .consumer(bytes -> {
                    actualMessage = new String(bytes, charset);
                    messageDeliveredLatch.countDown();
                })
                .build();
        String expectedMessage = "testMessage";
        communication.send(expectedMessage);
        messageDeliveredLatch.await();
        communication.close();
        Assert.assertEquals(expectedMessage, actualMessage);
        rabbitMqDockerizer.stopAndRemoveContainer();
    }
}