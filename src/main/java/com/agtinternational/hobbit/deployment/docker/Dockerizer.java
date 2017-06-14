package com.agtinternational.hobbit.deployment.docker;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public class Dockerizer implements Runnable {
    private static final Charset charset = Charset.forName("UTF-8");

    private final String name;
    private final Logger logger;
    private final Reader dockerFileReader;
    private final String tempDirectory;
    private final String imageName;
    private final String containerName;
    private final Map<String, List<PortBinding>> portBindings;
    private final Collection<String> environmentVariables;
    private final Collection<String> networks;

    private String imageId;
    private String tempDockerFileName;
    private Exception exception;
    private String containerId;

    protected Dockerizer(Builder builder) {
        name = builder.name;
        logger = LoggerFactory.getLogger(name);
        dockerFileReader = builder.dockerFileReader;
        tempDirectory = builder.tempDirectory;
        imageName = builder.imageName;
        containerName = builder.containerName;
        portBindings = builder.portBindings;
        environmentVariables = builder.environmentVariables;
        networks = builder.networks;
    }

    @Override
    public void run() {
        try {
            removeImagesAndContainers();
            buildImage();
            createContainer();
            startContainer();
            attachToContainerAndReadLogs();
        } catch (DockerCertificateException | InterruptedException | IOException | DockerException e) {
            logger.error("Exception", e);
            exception = e;
        } finally {
            try {
                removeImagesAndContainers();
            } catch (Exception e) {
                logger.debug("Exception", e);
            }
        }
    }

    public void removeAllSameNamedContainers() throws DockerException, InterruptedException, DockerCertificateException {
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            removeAllSameNamedContainers(dockerClient);
        }
    }

    public void removeAllSameNamedImages() throws DockerException, InterruptedException, DockerCertificateException {
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            removeAllSameNamedImages(dockerClient);
        }
    }

    public void buildImage() throws InterruptedException, DockerException, IOException, DockerCertificateException {
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            buildImage(dockerClient);
        }
    }

    public void createContainer() throws DockerException, InterruptedException, IOException, DockerCertificateException {
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            createContainer(dockerClient);
        }
    }

    public void startContainer() throws DockerCertificateException, DockerException, InterruptedException {
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            dockerClient.startContainer(containerId);
        }
    }

    /**
     * Blocks until the container is terminated
     */
    public void attachToContainerAndReadLogs() {
        LogStream logStream = null;
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            final String logs;
            logStream = dockerClient.attachContainer(containerId,
                    DockerClient.AttachParameter.LOGS, DockerClient.AttachParameter.STDOUT,
                    DockerClient.AttachParameter.STDERR, DockerClient.AttachParameter.STREAM);
            logs = logStream.readFully();
            logger.debug(logs);
        } catch (Exception e) {
            logger.debug("No logs are available because:", e);
        } finally {
            if (logStream != null) {
                logStream.close();
            }
        }
    }

    public void waitForContainerFinish() throws DockerException, InterruptedException, DockerCertificateException {
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build()) {
            dockerClient.waitContainer(containerId);
        }
    }

    public void removeImagesAndContainers() throws InterruptedException, DockerException, DockerCertificateException {
        removeAllSameNamedContainers();
        removeAllSameNamedImages();
    }

    private void createContainer(DockerClient dockerClient) throws DockerException, InterruptedException {
        boolean removeContainerWhenItExits = false;
        HostConfig hostConfig = HostConfig.builder()
                .autoRemove(removeContainerWhenItExits)
                .portBindings(portBindings)
                .build();
        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(getExposedPorts())
                .image(imageName)
                .env(getEnvironmentVariables())
                .build();
        ContainerCreation creation = dockerClient.createContainer(containerConfig, containerName);
        containerId = creation.id();
        if (containerId == null) {
            IllegalStateException exception = new IllegalStateException(format("Unable to create container %s", containerName));
            logger.error("Exception", exception);
            throw exception;
        }
        connectContainerToNetworks(dockerClient);
    }

    private void connectContainerToNetworks(DockerClient dockerClient) throws DockerException, InterruptedException {
        for (String network : networks) {
            String networkId = createDockerNetworkIfNeeded(dockerClient, network);
            dockerClient.connectToNetwork(containerId, networkId);
        }
    }

    public static String createDockerNetworkIfNeeded(DockerClient dockerClient, String networkName) throws
            DockerException, InterruptedException {
        for (Network network : dockerClient.listNetworks()) {
            if (network.name() != null && network.name().equals(networkName)) {
                return network.id();
            }
        }
        NetworkConfig networkConfig = NetworkConfig.builder()
                .name(networkName)
                .build();
        return dockerClient.createNetwork(networkConfig).id();
    }

    private Set<String> getExposedPorts() {
        return portBindings.keySet();
    }

    private String[] getEnvironmentVariables() {
        return environmentVariables.toArray(new String[environmentVariables.size()]);
    }

    private void buildImage(DockerClient dockerClient) throws
            InterruptedException, DockerException, IOException, IllegalStateException {
        createTempDockerFile();
        fillDockerFile();
        Path path = Paths.get(tempDirectory);
        imageId = dockerClient.build(path, imageName, tempDockerFileName, message -> {
        });
        removeTempDockerFile();
        if (imageId == null) {
            IllegalStateException exception = new IllegalStateException(format("Unable to create image %s", imageName));
            logger.error("Exception", exception);
            throw exception;
        }
    }

    private void removeAllSameNamedContainers(DockerClient dockerClient) throws
            DockerException, InterruptedException {
        dockerClient
                .listContainers(DockerClient.ListContainersParam.allContainers())
                .stream()
                .flatMap(container -> container.names().stream())
                .filter(name -> name.equals(dockerizeContainerName(containerName)))
                .forEach(name -> {
                    try {
                        dockerClient.removeContainer(name, DockerClient.RemoveContainerParam.forceKill());
                    } catch (Exception e) {
                        logger.error("Exception", e);
                    }
                });
    }

    private static String dockerizeContainerName(String intendedContainerName) {
        return format("/%s", intendedContainerName);
    }

    private void removeAllSameNamedImages(DockerClient dockerClient) throws DockerException, InterruptedException {
        for (Image image : dockerClient.listImages(DockerClient.ListImagesParam.allImages())) {
            ImmutableList<String> repoTags = image.repoTags();
            if (repoTags != null) {
                boolean nameMatch = repoTags.stream().anyMatch(name -> name != null && name.contains(imageName));
                if (nameMatch) {
                    boolean force = true;
                    boolean dontDeleteUntaggedParents = false;
                    dockerClient.removeImage(image.id(), force, dontDeleteUntaggedParents);
                }
            }
        }
    }

    private void createTempDockerFile() throws IOException {
        File file = File.createTempFile("dockerFile", "temp", new File(tempDirectory));
        tempDockerFileName = file.getName();
    }

    private void fillDockerFile() throws IOException {
        Path path = Paths.get(tempDirectory, tempDockerFileName);
        try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE)) {
            outputStream.write(readAllFromDockerFileReader());
        }
    }

    private byte[] readAllFromDockerFileReader() throws IOException {
        try (BufferedReader reader = new BufferedReader(dockerFileReader)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.write(line.getBytes(charset));
                buffer.write(format("%n").getBytes(charset));
            }
            return buffer.toByteArray();
        }
    }

    private void removeTempDockerFile() throws IOException {
        Path path = Paths.get(tempDirectory, tempDockerFileName);
        Files.deleteIfExists(path);
    }

    public Exception anyExceptions() {
        return exception;
    }

    static String toEnvironmentEntry(String key, String value) {
        return format("%s=%s", key, value);
    }

    public static class Builder {
        private final String name;
        private final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        private final Collection<String> environmentVariables = new HashSet<>();
        private final Collection<String> networks = new HashSet<>();

        private Reader dockerFileReader;
        private String tempDirectory;
        private String imageName;
        private String containerName;

        public Builder(String name) {
            this.name = name;
        }

        public Builder addNetworks(String... nets) {
            if (nets != null) {
                Stream.of(nets).forEach(networks::add);
            }
            return this;
        }

        public Builder addPortBindings(String containerPort, PortBinding... hostPorts) {
            List<PortBinding> hostPortsList = new ArrayList<>();
            hostPortsList.addAll(Arrays.asList(hostPorts));
            portBindings.put(String.valueOf(containerPort), hostPortsList);
            return this;
        }

        public Builder addEnvironmentVariable(String key, String value) {
            environmentVariables.add(toEnvironmentEntry(key, value));
            return this;
        }

        public Builder containerName(String containerName) {
            this.containerName = containerName;
            return this;
        }

        public Builder imageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public Builder tempDirectory(String tempDirectory) {
            this.tempDirectory = tempDirectory;
            return this;
        }

        public Builder dockerFileReader(Reader dockerFileReader) {
            this.dockerFileReader = dockerFileReader;
            return this;
        }

        public Dockerizer build() {
            if (name == null || imageName == null || containerName == null) {
                return null;
            }
            return new Dockerizer(this);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "name='" + name + '\'' +
                    ", environmentVariables=" + environmentVariables +
                    ", networks=" + networks +
                    ", tempDirectory='" + tempDirectory + '\'' +
                    ", imageName='" + imageName + '\'' +
                    ", containerName='" + containerName + '\'' +
                    '}';
        }
    }
}