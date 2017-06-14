package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.BenchmarkTask;
import com.agtinternational.hobbit.core.TaskBasedBenchmarkController;
import com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark.ParrotTask;
import org.hobbit.core.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hobbit.core.Constants.BENCHMARK_PARAMETERS_MODEL_KEY;

/**
 * Public because it is called from command line
 *
 * @author Roman Katerinenko
 */
public class ParrotBenchmarkDockerRunner {
    private static final Logger logger = LoggerFactory.getLogger(ParrotBenchmarkDockerRunner.class);

    public static void main(String... args) throws Exception {
        new ParrotBenchmarkDockerRunner().run();
    }

    private void run() throws Exception {
        logger.debug("Creating benchmark controller...");
        Component benchmarkController = createParrotBenchmarkController();
        logger.debug("Initializing core controller...");
        benchmarkController.init();
        logger.debug("Running benchmark controller...");
        benchmarkController.run();
        logger.debug("Finished.");
        benchmarkController.close();
        logger.debug("Closed.");
    }

    private Component createParrotBenchmarkController() {
        String encodedModel = System.getenv().get(BENCHMARK_PARAMETERS_MODEL_KEY);
        JenaKeyValue inputParameters = new JenaKeyValue.Builder().buildFrom(encodedModel);
        BenchmarkTask task = new ParrotTask(inputParameters);
        return new TaskBasedBenchmarkController(task);
    }
}