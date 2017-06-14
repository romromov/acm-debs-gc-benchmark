package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.FORMAT_RDF;

/**
 * @author Roman Katerinenko
 */
public class AnalyticsBenchmarkDeployment {
    public static void main(String[] args) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        Dockerizer dockerizer = new AnalyticsBenchmarkBuilder()
                .benchmarkOutputFormat(FORMAT_RDF)
                .hobbitSessionId(CommonConstants.HOBBIT_SESSION_ID)
                .systemUri(CommonConstants.SYSTEM_URI)
                .build();
        dockerizer.removeImagesAndContainers();
        dockerizer.buildImage();
        dockerizer.createContainer();
    }
}