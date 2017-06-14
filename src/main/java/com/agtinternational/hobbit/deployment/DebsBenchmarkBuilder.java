package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.deployment.docker.Dockerizer;

/**
 * @author Roman Katerinenko
 */
class DebsBenchmarkBuilder extends BaseDebsBenchmarkBuilder {
    public DebsBenchmarkBuilder() {
        super("DebsBenchmarkDockerizer");
    }

    @Override
    Dockerizer build() {
        imageName("git.project-hobbit.eu:4567/rkaterinenko/debsbenchmark");
        containerName("cont_name_debsbenchmark");
        return super.build();
    }
}