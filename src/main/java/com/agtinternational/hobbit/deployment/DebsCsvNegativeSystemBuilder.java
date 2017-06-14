package com.agtinternational.hobbit.deployment;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;

/**
 * @author Roman Katerinenko
 */
class DebsCsvNegativeSystemBuilder extends BaseDebsBuilder {
    DebsCsvNegativeSystemBuilder() {
        super("DebsCsvNegativeSystemDockerizer");
        imageName("git.project-hobbit.eu:4567/rkaterinenko/debssystemcsvneg");
        containerName("cont_name_debssystemcsvneg");
        runnerClass(DebsCsvNegativeSystemDockerRunner.class);
    }

    DebsCsvNegativeSystemBuilder parameters(String parameters) {
        addEnvironmentVariable(SYSTEM_PARAMETERS_MODEL_KEY, parameters);
        return this;
    }
}