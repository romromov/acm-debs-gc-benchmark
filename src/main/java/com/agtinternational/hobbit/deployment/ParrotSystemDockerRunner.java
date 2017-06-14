package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.SystemComponent;
import com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark.ParrotSystemComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roman Katerinenko
 */
public class ParrotSystemDockerRunner {
    private static final Logger logger = LoggerFactory.getLogger(ParrotSystemDockerRunner.class);

    public static void main(String... args) throws Exception {
        logger.debug("Staring system...");
        ParrotSystemDockerRunner parrotSystemDockerRunner = new ParrotSystemDockerRunner();
        parrotSystemDockerRunner.run();
    }

    public void run() throws Exception {
        logger.debug("Creating system component...");
        SystemComponent systemComponent = new ParrotSystemComponent();
        logger.debug("Initializing system component...");
        systemComponent.init();
        logger.debug("Running system component...");
        systemComponent.run();
        logger.debug("Finished.");
        systemComponent.close();
        logger.debug("Closed.");
    }
}