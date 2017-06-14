package com.agtinternational.hobbit.deployment;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;

/**
 * @author Roman Katerinenko
 */
class DebsCsvSystemBuilder extends BaseDebsBuilder {
    DebsCsvSystemBuilder() {
        super("DebsCsvSystemDockerizer");
        imageName("git.project-hobbit.eu:4567/rkaterinenko/debssystemcsv");
        containerName("cont_name_debssystemcsv");
        runnerClass(DebsCsvSystemDockerRunner.class);
    }

    @Override
    BaseBuilder parameters(String parameters) {
        addEnvironmentVariable(SYSTEM_PARAMETERS_MODEL_KEY, parameters);
        return this;
    }
}