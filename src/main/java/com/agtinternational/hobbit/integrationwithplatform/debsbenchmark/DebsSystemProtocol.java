package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agt.ferromatikdata.anomalydetector.AnomalyDetector;
import com.agt.ferromatikdata.anomalydetector.WithinMachineAnomaly;
import com.agt.ferromatikdata.formatting.RdfAnomalyFormatter;
import com.agtinternational.hobbit.core.KeyValue;
import com.agtinternational.hobbit.integrationwithplatform.correctnesscheck.CorrectnessSystemProtocol;
import com.agtinternational.hobbit.io.NetworkCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
class DebsSystemProtocol extends CorrectnessSystemProtocol {
    private static final Logger logger = LoggerFactory.getLogger(DebsSystemProtocol.class);

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private AnomalyDetector anomalyDetector;

    DebsSystemProtocol(NetworkCommunication.Builder communicationBuilder, KeyValue kv) {
        super(communicationBuilder);
        try {
            anomalyDetector = new FerromatikAnomalyDetectorBuilder().buildFrom(kv, this::sendAnomaly);
        } catch (Exception e) {
//            todo fix exception from constructor
            anomalyDetector = null;
            logger.error("Exception", e);
        }
    }

    protected void sendAnomaly(WithinMachineAnomaly anomaly) {
        try {
            RdfAnomalyFormatter f = new RdfAnomalyFormatter(CHARSET);
            f.init();
            String string = f.format(anomaly);
            sendBytes(string.getBytes(CHARSET));
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }

    @Override
    protected void waitForFinishingProcessing() throws InterruptedException {
        // do nothing
    }

    @Override
    protected void handleDelivery(byte[] bytes) {
        anomalyDetector.addNewDataPoint(new String(bytes, CHARSET));
    }
}