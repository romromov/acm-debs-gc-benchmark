package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;

import java.io.Reader;

import static com.agtinternational.hobbit.deployment.CommonConstants.*;
import static org.hobbit.core.Constants.*;

/**
 * @author Roman Katerinenko
 */
abstract class BaseBuilder {
    private static final String DEFAULT_BUILD_DIRECTORY = "./src/test/resources";
    private static final String EXPERIMENT_URI = "http://example.com/exp1";

    private final Dockerizer.Builder dockerizerBuilder;

    private String imageName;
    private String containerName;
    private String systemUri;
    private String hobbitSessionId;

    BaseBuilder(String dockerizerName) {
        dockerizerBuilder = new Dockerizer.Builder(dockerizerName);
    }


    BaseBuilder systemUri(String systemUri) {
        this.systemUri = systemUri;
        return this;
    }

    BaseBuilder hobbitSessionId(String hobbitSessionId) {
        this.hobbitSessionId = hobbitSessionId;
        return this;
    }

    BaseBuilder imageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    BaseBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    BaseBuilder addEnvironmentVariable(String key, String value) {
        dockerizerBuilder.addEnvironmentVariable(key, value);
        return this;
    }

    Dockerizer build() {
        dockerizerBuilder.tempDirectory(DEFAULT_BUILD_DIRECTORY)
                .imageName(imageName)
                .containerName(containerName)
                .dockerFileReader(getDockerFileContent())
                .addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, RABBIT_MQ_HOST_NAME)
                .addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, hobbitSessionId)
                .addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, EXPERIMENT_URI)
                .addEnvironmentVariable(SYSTEM_URI_KEY, systemUri)
                .addNetworks(HOBBIT_NETWORK_NAME)
                .addNetworks(HOBBIT_CORE_NETWORK_NAME);
        Dockerizer dockerizer = dockerizerBuilder.build();
        if (dockerizer == null) {
            throw new IllegalStateException("Unable to dockerize system: ");
        }
        return dockerizer;
    }

    protected abstract Reader getDockerFileContent();
}