package com.agtinternational.hobbit.deployment;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author Roman Katerinenko
 */
public abstract class BaseDebsBuilder extends BaseBuilder {
    private Class runnerClass;

    public BaseDebsBuilder(String dockerizerName) {
        super(dockerizerName);
    }

    BaseBuilder runnerClass(Class runnerClass) {
        this.runnerClass = runnerClass;
        return this;
    }

    abstract BaseBuilder parameters(String parameters);

    protected Reader getDockerFileContent() {
        String content = String.format(
                "FROM java\n" +
                        "RUN mkdir -p /usr/src/debs\n" +
                        "WORKDIR /usr/src/debs\n" +
                        "ADD ./sml-benchmark-1.0-SNAPSHOT.jar /usr/src/debs\n" +
                        "ADD ./original-wm-data-gen-1.0-SNAPSHOT.jar /usr/src/debs\n" +
                        "CMD [\"java\", \"-Xmx250g\", \"-cp\", \"sml-benchmark-1.0-SNAPSHOT.jar:original-wm-data-gen-1.0-SNAPSHOT.jar\", \"%s\"]",
                runnerClass.getCanonicalName());
        return new StringReader(content);
    }
}