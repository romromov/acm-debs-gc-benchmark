package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agt.ferromatikdata.core.GeneratorTask;
import com.agt.ferromatikdata.core.Machines;
import com.agt.ferromatikdata.dataframe.DataGenerator;
import com.agt.ferromatikdata.formatting.CsvFormatter;
import com.agt.ferromatikdata.formatting.OutputFormatter;
import com.agt.ferromatikdata.formatting.RdfFormatter;
import com.agtinternational.hobbit.core.BenchmarkTask;
import com.agtinternational.hobbit.core.Communication;
import com.agtinternational.hobbit.core.KeyValue;
import com.agtinternational.hobbit.deployment.JenaKeyValue;
import com.agtinternational.hobbit.integrationwithplatform.correctnesscheck.CorrectnessBenchmarkProtocol;
import com.agtinternational.hobbit.io.RabbitMqCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_COUNT_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.ANOMALY_MATCH_SUCCESS;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.AVERAGE_LATENCY_NANOS_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.DATA_POINT_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.DEBS_KPI_OUTPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.FORMAT_RDF;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.INTERVAL_NANOS_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.SEED_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.THROUGHPUT_BYTES_PER_SEC_OUTPUT_NAME;
import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public class DebsTask extends CorrectnessBenchmarkProtocol implements BenchmarkTask {
    private static final Logger logger = LoggerFactory.getLogger(DebsTask.class);

    public static final int LATENCY_UNDEFINED = -1;
    public static final double THROUGHPUT_UNDEFINED = -1.;
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String MESSAGE_IF_NO_LAST = "<empty>";

    private final KeyValue params;
    private final OutputSender outputSender = new OutputSender();
    private final int dataPointCount;
    private final int inputIntervalNanos;

    private com.agt.ferromatikdata.dataframe.DataGenerator dataGenerator;
    private DebsDataChecker dataChecker;
    private boolean exceptionWasThrown = false;
    private String exceptionMessage;
    private long bytesSent;
    private long startDataGenerationMillis;
    private OutputFormatter outputFormatter;
    private OutputFormatter csvFormatter;
    private Machines machines;

    public DebsTask(KeyValue params) {
        super(new RabbitMqCommunication.Builder());
        this.params = params;
        this.dataChecker = new DebsDataChecker(params, new DebsAnomalyDetectorBuilder());
        dataPointCount = params.getIntValueFor(DATA_POINT_COUNT_INPUT_NAME);
        inputIntervalNanos = params.getIntValueFor(INTERVAL_NANOS_INPUT_NAME);
        machines = new DebsMachinesInputHandler(params).getMachines();
    }

    @Override
    protected void initProtocol() throws Exception {
        outputFormatter = newOutputFormatter();
        csvFormatter = newCsvFormatter();
        dataGenerator = new com.agt.ferromatikdata.dataframe.DataGenerator.Builder()
                .generatorTask(newGenerationTask())
                .generationDelayNanos(inputIntervalNanos)
                .initialDelayMillis(0)
                .dataPointsListener(newDataPointsListener())
                .seed(params.getIntValueFor(SEED_INPUT_NAME))
                .charset(CHARSET)
                .gapBetweenMeasurements(Duration.ofSeconds(1))
                .startingDateTime(getDateTime())
                .build();
    }

    private GeneratorTask newGenerationTask() {
        return new BenchmarkModeInputHandler(params, machines).getGeneratorTask();
    }

    private DataGenerator.DataPointsListener newDataPointsListener() throws Exception {
        return dataPointSource -> {
            try {
                String outputDataPoint = dataPointSource.formatUsing(outputFormatter);
                String csvDataPoint = dataPointSource.formatUsing(csvFormatter);
                outputSender.send(outputDataPoint);
                bytesSent += outputDataPoint.getBytes(CHARSET).length;
                dataChecker.addDataPoint(new InstantAndText(Instant.now(), csvDataPoint));
            } catch (Exception e) {
                logger.error("Exception", e);
            }
        };
    }

    private static OutputFormatter newCsvFormatter() throws Exception {
        OutputFormatter csvFormatter = new CsvFormatter(CHARSET);
        csvFormatter.init();
        return csvFormatter;
    }

    private OutputFormatter newOutputFormatter() throws Exception {
        int formatInt = params.getIntValueFor(DebsConstants.FORMAT_INPUT_NAME);
        OutputFormatter formatter = (formatInt == FORMAT_RDF) ? new RdfFormatter(CHARSET) : new CsvFormatter(CHARSET);
        formatter.init();
        return formatter;
    }

    private static LocalDateTime getDateTime() {
        int year = 2017;
        int month = 1;
        int dayOfMonth = 1;
        int hour = 1;
        int minute = 0;
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (Exception e) {
            exceptionWasThrown = true;
            exceptionMessage = e.getMessage();
            logger.error("Exception", e);
        }
    }

    @Override
    protected void startDataGeneration() throws Exception {
        startDataGenerationMillis = System.currentTimeMillis();
        dataGenerator.run();
    }

    @Override
    protected Communication.Consumer getInputConsumer() {
        return bytes -> {
            String anomalyFromSystem = new String(bytes, CHARSET);
            dataChecker.addActual(new InstantAndText(Instant.now(), anomalyFromSystem));
        };
    }

    @Override
    public boolean isSuccessful() {
        return dataChecker.isCorrect();
    }

    @Override
    public KeyValue getResult() {
        JenaKeyValue jenaKeyValue = new JenaKeyValue();
        String resultMessage = composeResultMessage();
        jenaKeyValue.setValue(DEBS_KPI_OUTPUT_NAME, resultMessage);
        jenaKeyValue.setValue(ANOMALY_MATCH_OUTPUT_NAME, composeAnomalyMatchResult());
        jenaKeyValue.setValue(ANOMALY_MATCH_COUNT_OUTPUT_NAME, getCheckedAnomaliesCount());
        jenaKeyValue.setValue(AVERAGE_LATENCY_NANOS_OUTPUT_NAME, getAverageLatencyNanos());
        jenaKeyValue.setValue(THROUGHPUT_BYTES_PER_SEC_OUTPUT_NAME, computeThroughputBytesPerSecond());
        logger.debug("DebsResultMessage: " + resultMessage);
        return jenaKeyValue;
    }

    private double computeThroughputBytesPerSecond() {
        double runTimeSec = (System.currentTimeMillis() - startDataGenerationMillis) / 1000.;
        if (runTimeSec == 0) {
            return THROUGHPUT_UNDEFINED;
        } else {
            return (double) bytesSent / runTimeSec;
        }
    }

    private int getAverageLatencyNanos() {
        Duration averageLatency = dataChecker.getAverageLatency();
        return (int) (averageLatency == null ? LATENCY_UNDEFINED : averageLatency.toNanos());
    }

    private String composeResultMessage() {
        return format("Executed on %d data points,%n " +
                        "interval %d nanos,%n " +
                        "checked %d anomalies,%n " +
                        "result: %s",
                dataPointCount,
                inputIntervalNanos,
                getCheckedAnomaliesCount(),
                composeAnomalyMatchResult());
    }

    private int getCheckedAnomaliesCount() {
        return dataChecker.getCheckedCount();
    }

    private String composeAnomalyMatchResult() {
        if (exceptionWasThrown) {
            return "Exception: " + exceptionMessage;
        } else {
            if (isSuccessful()) {
                return ANOMALY_MATCH_SUCCESS;
            } else {
                return format("Expected[%s]%nActual[%s]", getNotMatchedGold(), getNotMatchedLast());
            }
        }
    }

    private String getNotMatchedGold() {
        Comparable lastGold = dataChecker.getNotMatchedGold();
        return lastGold == null ? MESSAGE_IF_NO_LAST : lastGold.toString();
    }

    private String getNotMatchedLast() {
        Comparable lastActual = dataChecker.getNotMatchedActual();
        return lastActual == null ? MESSAGE_IF_NO_LAST : lastActual.toString();
    }
}