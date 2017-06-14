package com.agtinternational.hobbit.core;

import com.agtinternational.hobbit.deployment.JenaKeyValue;
import com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TERMINATION_TYPE_NORMAL;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.TERMINATION_TYPE_OUTPUT_NAME;

/**
 * @author Roman Katerinenko
 */
public class TaskBasedBenchmarkController extends AbstractBenchmarkController {
    private static final Logger logger = LoggerFactory.getLogger(TaskBasedBenchmarkController.class);

    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final String TERMINATION_MESSAGE = "~~Termination Message~~";

    private final Collection<BenchmarkTask> tasks;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private final int timeoutMin;

    private BenchmarkTask task;
    private JenaKeyValue resultKeyValue;
    private CountDownLatch taskFinishedBarrier;

    public TaskBasedBenchmarkController(int timeoutMin, BenchmarkTask... tasks) {
        this.timeoutMin = timeoutMin;
        this.tasks = new ArrayList<>(Arrays.asList(tasks));
    }

    public TaskBasedBenchmarkController(BenchmarkTask... tasks) {
        this(DebsConstants.NO_TIMEOUT, tasks);
    }

    @Override
    public void init() throws Exception {
        logger.debug("Initializing...");
        super.init();
        Map<String, String> environment = System.getenv();
        if (!environment.containsKey(Constants.SYSTEM_URI_KEY)) {
            throw new IllegalStateException("System URI must not be null");
        }
        String hobbitSessionId = getHobbitSessionId();
        if (hobbitSessionId.equals(Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS) ||
                hobbitSessionId.equals(Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS)) {
            throw new IllegalStateException("Wrong hobbit session id. It must not be equal to HOBBIT_SESSION_ID_FOR_BROADCASTS or HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS");
        }
        logger.debug("Initialized successfully");
    }

    @Override
    protected void executeBenchmark() throws Exception {
        logger.debug("Start executing...");
        resultKeyValue = new JenaKeyValue(experimentUri);
        taskFinishedBarrier = new CountDownLatch(1);
        if (!tasks.isEmpty()) {
            task = tasks.iterator().next();
            executor.execute(new TaskRunner(task));
            setupTimer();
            taskFinishedBarrier.await();
            resultKeyValue.add(task.getResult());
            resultKeyValue.setValue(TERMINATION_TYPE_OUTPUT_NAME, TERMINATION_TYPE_NORMAL);
        }
        sendResult();
        logger.debug("Stopping executor...");
        stopExecutor();
        logger.debug("Finished.");
    }

    private void sendResult() {
        try {
            byte[] bytes = resultKeyValue.toBytes();
            sendToCmdQueue(Commands.BENCHMARK_FINISHED_SIGNAL, bytes);
        } catch (Exception e) {
            logger.error("Unable to send result to benchmark", e);
        }
    }

    private void setupTimer() {
        if (timeoutMin != DebsConstants.NO_TIMEOUT) {
            logger.debug("setting timer:" + timeoutMin);
            timer.schedule(new Terminator(), timeoutMin, TimeUnit.MINUTES);
        }
    }

    private void stopExecutor() throws InterruptedException {
        executor.shutdown();
        while (!executor.isTerminated()) {
            executor.awaitTermination(10, TimeUnit.MILLISECONDS);
        }
    }

    private class TaskRunner implements Runnable {
        private final BenchmarkTask task;

        TaskRunner(BenchmarkTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.run();
            } catch (Throwable e) {
                logger.error("Task {} threw an exception ", task.toString());
                logger.error("", e);
            }
            taskFinishedBarrier.countDown();
        }
    }

    private class Terminator implements Runnable {
        @Override
        public void run() {
            resultKeyValue.add(task.getResult());
            String text = String.format("Aborted after %d min.", timeoutMin);
            resultKeyValue.setValue(TERMINATION_TYPE_OUTPUT_NAME, text);
            sendResult();
            logger.debug(text);
            System.exit(0);
        }
    }
}