package com.agtinternational.hobbit.deployment;

import com.spotify.docker.client.exceptions.DockerCertificateException;

import java.util.concurrent.TimeoutException;

import static com.agtinternational.hobbit.deployment.CommonConstants.*;

/**
 * @author Roman Katerinenko
 */
public class RabbitMqDeployment {
    public static void main(String... args) throws InterruptedException, TimeoutException, DockerCertificateException {
        RabbitMqDockerizer rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .containerName(RABBIT_MQ_CONTAINER_NAME)
                .host("127.0.0.1")
                .networks(HOBBIT_NETWORK_NAME, HOBBIT_CORE_NETWORK_NAME)
                .build();
        rabbitMqDockerizer.run();
        rabbitMqDockerizer.waitUntilRunning();
        rabbitMqDockerizer.attachToContainerAndReadLogs();
    }
}