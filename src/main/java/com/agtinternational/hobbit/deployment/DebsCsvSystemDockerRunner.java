package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.SystemComponent;
import com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsCsvSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;

/**
 * @author Roman Katerinenko
 */
public class DebsCsvSystemDockerRunner {
    private static final Logger logger = LoggerFactory.getLogger(DebsCsvSystemDockerRunner.class);

    public static void main(String... args) throws Exception {
        logger.debug("Staring system...");
        DebsCsvSystemDockerRunner dr = new DebsCsvSystemDockerRunner();
        dr.run();
    }

    public void run() throws Exception {
        logger.debug("Creating system component...");
        String encodedModel = System.getenv().get(SYSTEM_PARAMETERS_MODEL_KEY);
        logger.debug("Params:{}", encodedModel);
        JenaKeyValue params = new JenaKeyValue.Builder().buildFrom(encodedModel);
        SystemComponent sc = new DebsCsvSystem(params);
        logger.debug("Initializing system component...");
        sc.init();
        logger.debug("Running system component...");
        sc.run();
        logger.debug("Finished.");
        sc.close();
        logger.debug("Closed.");
    }
}