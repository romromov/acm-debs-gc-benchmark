package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;

/**
 * @author Roman Katerinenko
 */
public class ParrotSystemDeployment extends BaseParrotBuilder {

    public static void main(String[] args) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        String emptyParameters = "{}";
        Dockerizer dockerizer = new ParrotSystemDeployment()
                .parameters(emptyParameters)
                .hobbitSessionId(CommonConstants.HOBBIT_SESSION_ID)
                .systemUri(CommonConstants.SYSTEM_URI)
                .build();
        dockerizer.removeImagesAndContainers();
        dockerizer.buildImage();
        dockerizer.createContainer();
    }

    public ParrotSystemDeployment() {
        super("ParrotSystemDockerizer");
        imageName("git.project-hobbit.eu:4567/rkaterinenko/debsparrotsystem");
        containerName("cont_name_debsparrotsystem");
        runnerClass(ParrotSystemDockerRunner.class);
    }

    ParrotSystemDeployment parameters(String parameters) {
        addEnvironmentVariable(SYSTEM_PARAMETERS_MODEL_KEY, parameters);
        return this;
    }
}