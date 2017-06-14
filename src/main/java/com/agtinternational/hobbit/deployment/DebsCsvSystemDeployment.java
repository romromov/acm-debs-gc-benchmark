package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;

/**
 * @author Roman Katerinenko
 */
public class DebsCsvSystemDeployment {
    public static void main(String[] args) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        String emptyParameters = "{}";
        Dockerizer dockerizer = new DebsCsvSystemBuilder()
                .addEnvironmentVariable(SYSTEM_PARAMETERS_MODEL_KEY, emptyParameters)
                .hobbitSessionId(CommonConstants.HOBBIT_SESSION_ID)
                .systemUri(CommonConstants.SYSTEM_URI)
                .build();
        dockerizer.removeImagesAndContainers();
        dockerizer.buildImage();
        dockerizer.createContainer();
    }
}