package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;

/**
 * @author Roman Katerinenko
 */
public class ParrotNegativeSystemDeployment extends BaseParrotBuilder {

    public static void main(String[] args) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        String emptyParameters = "{}";
        Dockerizer dockerizer = new ParrotNegativeSystemDeployment()
                .parameters(emptyParameters)
                .hobbitSessionId(CommonConstants.HOBBIT_SESSION_ID)
                .systemUri(CommonConstants.SYSTEM_URI)
                .build();
        dockerizer.removeImagesAndContainers();
        dockerizer.buildImage();
        dockerizer.createContainer();
    }

    public ParrotNegativeSystemDeployment() {
        super("ParrotNegativeSystemDockerizer");
        imageName("git.project-hobbit.eu:4567/rkaterinenko/debsparrotsystemnegative");
        containerName("cont_name_debsparrotsystemnegative");
        runnerClass(ParrotNegativeSystemDockerRunner.class);
    }

    ParrotNegativeSystemDeployment parameters(String parameters) {
        addEnvironmentVariable(SYSTEM_PARAMETERS_MODEL_KEY, parameters);
        return this;
    }
}