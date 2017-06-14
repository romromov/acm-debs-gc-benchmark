package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;

/**
 * @author Roman Katerinenko
 */
class AnalyticsBenchmarkBuilder extends BaseDebsBenchmarkBuilder {
    public AnalyticsBenchmarkBuilder() {
        super("AnalyticsBenchmarkDockerizer");
    }

    @Override
    Dockerizer build() {
        imageName("git.project-hobbit.eu:4567/rkaterinenko/analyticsbenchmark");
        containerName("cont_name_analyticsbenchmark");
        return super.build();
    }
}