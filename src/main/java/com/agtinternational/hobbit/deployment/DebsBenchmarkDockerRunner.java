package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.BenchmarkTask;
import com.agtinternational.hobbit.core.TaskBasedBenchmarkController;
import com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants;
import com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsTask;
import org.hobbit.core.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hobbit.core.Constants.BENCHMARK_PARAMETERS_MODEL_KEY;

/**
 * @author Roman Katerinenko
 */
public class DebsBenchmarkDockerRunner {
    private static final Logger logger = LoggerFactory.getLogger(DebsBenchmarkDockerRunner.class);

    public static void main(String... args) throws Exception {
        new DebsBenchmarkDockerRunner().run();
    }

    private void run() throws Exception {
        logger.debug("Creating benchmark controller...");
        Component bc = createDebsBenchmarkController();
        logger.debug("Initializing core controller...");
        bc.init();
        logger.debug("Running benchmark controller...");
        bc.run();
        logger.debug("Finished.");
        bc.close();
        logger.debug("Closed.");
    }

    private Component createDebsBenchmarkController() {
        String encodedModel = System.getenv().get(BENCHMARK_PARAMETERS_MODEL_KEY);
        JenaKeyValue inputParameters = new JenaKeyValue.Builder().buildFrom(encodedModel);
        BenchmarkTask task = new DebsTask(inputParameters);
        int timeout = inputParameters.getIntValueFor(DebsConstants.TIMEOUT_MINUTES_INPUT_NAME);
        return new TaskBasedBenchmarkController(timeout, task);
    }
}