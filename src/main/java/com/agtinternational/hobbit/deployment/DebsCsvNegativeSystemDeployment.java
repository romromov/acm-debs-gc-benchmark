package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

/**
 * @author Roman Katerinenko
 */
public class DebsCsvNegativeSystemDeployment {
    public static void main(String[] args) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        String emptyParameters = "{}";
        Dockerizer dockerizer = new DebsCsvNegativeSystemBuilder()
                .parameters(emptyParameters)
                .hobbitSessionId(CommonConstants.HOBBIT_SESSION_ID)
                .systemUri(CommonConstants.SYSTEM_URI)
                .build();
        dockerizer.removeImagesAndContainers();
        dockerizer.buildImage();
        dockerizer.createContainer();
    }
}