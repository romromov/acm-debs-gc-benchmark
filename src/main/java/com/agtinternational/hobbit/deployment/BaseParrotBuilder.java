package com.agtinternational.hobbit.deployment;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author Roman Katerinenko
 */
abstract class BaseParrotBuilder extends BaseBuilder {
    private Class runnerClass;

    BaseParrotBuilder(String dockerizerName) {
        super(dockerizerName);
    }

    BaseBuilder runnerClass(Class runnerClass) {
        this.runnerClass = runnerClass;
        return this;
    }

    protected Reader getDockerFileContent() {
        String content = String.format(
                "FROM java\n" +
                        "RUN mkdir -p /usr/src/debs\n" +
                        "WORKDIR /usr/src/debs\n" +
                        "ADD ./sml-benchmark-1.0-SNAPSHOT.jar /usr/src/debs\n" +
                        "CMD [\"java\", \"-cp\", \"sml-benchmark-1.0-SNAPSHOT.jar\", \"%s\"]",
                runnerClass.getCanonicalName());
        return new StringReader(content);
    }
}