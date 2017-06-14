package com.agtinternational.hobbit.deployment.docker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Roman Katerinenko
 */
public class DockerizerTest {
    private static final String TEST_IMAGE_NAME = "testimagename";
    private static final String CONTAINER_NAME = "testcontainername";
    private static final String ENV1_KEY = "key1";
    private static final String ENV1_VALUE = "value1";
    private static final String ENV2_KEY = "key2";
    private static final String ENV2_VALUE = "value2";
    private static final String NETWORK1 = "testNet1";
    private static final String NETWORK2 = "testNet2";
    private static final String CONTAINER_PORT = "7645";
    private static final String HOST_PORT = "7646";
    private static final PortBinding portBinding = PortBinding.of("0.0.0.0", HOST_PORT);

    @Test
    public void checkBuildImage() throws InterruptedException, DockerException, DockerCertificateException, IOException {
        Dockerizer dockerizer = new Dockerizer.Builder("TestDockerizer")
                .dockerFileReader(newDockerFileReader())
                .tempDirectory("src/test/resources")
                .imageName(TEST_IMAGE_NAME)
                .containerName(CONTAINER_NAME)
                .addEnvironmentVariable(ENV1_KEY, ENV1_VALUE)
                .addEnvironmentVariable(ENV2_KEY, ENV2_VALUE)
                .addNetworks(NETWORK1, NETWORK2)
                .addPortBindings(CONTAINER_PORT, portBinding)
                .build();
        dockerizer.removeImagesAndContainers();
        dockerizer.buildImage();
        dockerizer.createContainer();
        assertTrue(isImageExist(TEST_IMAGE_NAME));
        assertNull(dockerizer.anyExceptions());
        assertTrue(isEnvironmentVariableExist(CONTAINER_NAME, ENV1_KEY, ENV1_VALUE));
        assertTrue(isEnvironmentVariableExist(CONTAINER_NAME, ENV2_KEY, ENV2_VALUE));
        assertTrue(isConnectedToNetworks(CONTAINER_NAME, NETWORK1, NETWORK2));
        assertTrue(checkPortBindings(CONTAINER_NAME, HOST_PORT, portBinding));
        dockerizer.removeImagesAndContainers();
    }

    private static Reader newDockerFileReader() {
        return new StringReader("FROM busybox");
    }

    private static boolean checkPortBindings(String containerIdOrName, String containerPort, PortBinding... hostPorts) {
        // todo implement
        return true;
    }

    private static boolean isConnectedToNetworks(String containerIdOrName, String... targetNetworks) throws DockerCertificateException, DockerException, InterruptedException {
        if (targetNetworks == null || targetNetworks.length == 0) {
            return true;
        }
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            ContainerInfo containerInfo = dockerClient.inspectContainer(containerIdOrName);
            ImmutableMap<String, AttachedNetwork> networks = containerInfo.networkSettings().networks();
            if (networks == null) {
                return false;
            }
            for (String targetNetwork : targetNetworks) {
                ImmutableSet<String> containerNets = networks.keySet();
                if (!containerNets.stream().anyMatch(net -> net.equals(targetNetwork))) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isEnvironmentVariableExist(String containerIdOrName, String key, String value) throws DockerCertificateException, DockerException, InterruptedException {
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            ContainerInfo containerInfo = dockerClient.inspectContainer(containerIdOrName);
            ImmutableList<String> env = containerInfo.config().env();
            return env != null && env.stream().anyMatch(e -> Dockerizer.toEnvironmentEntry(key, value).equals(e));
        }
    }

    private static boolean isImageExist(String targetImageName) throws DockerException, InterruptedException, DockerCertificateException {
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            for (Image image : dockerClient.listImages(DockerClient.ListImagesParam.allImages())) {
                ImmutableList<String> repoTags = image.repoTags();
                if (repoTags != null) {
                    boolean nameMatch = repoTags.stream().anyMatch(name -> name != null && name.contains(targetImageName));
                    if (nameMatch) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}