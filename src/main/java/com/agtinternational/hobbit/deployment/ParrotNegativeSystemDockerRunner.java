package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.SystemComponent;
import com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark.ParrotNegativeSystemComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roman Katerinenko
 */
public class ParrotNegativeSystemDockerRunner {
    private static final Logger logger = LoggerFactory.getLogger(ParrotNegativeSystemDockerRunner.class);

    public static final int CHECKED_MESSAGES_COUNT_BEFORE_FAIL = 0;

    public static void main(String... args) throws Exception {
        logger.debug("Staring negative system...");
        ParrotNegativeSystemDockerRunner runner = new ParrotNegativeSystemDockerRunner();
        runner.run();
    }

    public void run() throws Exception {
        logger.debug("Creating negative system component...");
        SystemComponent systemComponent = new ParrotNegativeSystemComponent();
        logger.debug("Initializing negative system component...");
        systemComponent.init();
        logger.debug("Running negative system component...");
        systemComponent.run();
        logger.debug("Finished.");
        systemComponent.close();
        logger.debug("Closed.");
    }
}