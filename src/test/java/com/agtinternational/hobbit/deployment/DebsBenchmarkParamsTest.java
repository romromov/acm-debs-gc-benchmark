package com.agtinternational.hobbit.deployment;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.Assert;
import org.junit.Test;

import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.DATA_POINT_COUNT_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.EXPECTED_DATA_POINTS_COUNT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_DEFAULT;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.PROBABILITY_THRESHOLD_INPUT_NAME;

/**
 * @author Roman Katerinenko
 */
public class DebsBenchmarkParamsTest {
    @Test
    public void checkInputFromPlatformController() {
        byte[] bytesEncoding = newInputFromPlatformController();
        JenaKeyValue keyValue = new JenaKeyValue.Builder().buildFrom(bytesEncoding);
        Assert.assertTrue(EXPECTED_DATA_POINTS_COUNT == keyValue.getIntValueFor(DATA_POINT_COUNT_INPUT_NAME));
        double v = keyValue.getDoubleValueFor(PROBABILITY_THRESHOLD_INPUT_NAME);
        Assert.assertTrue(Double.compare(PROBABILITY_THRESHOLD_DEFAULT, v) == 0);
    }

    private byte[] newInputFromPlatformController() {
        Model model = ModelFactory.createDefaultModel();
        Resource subject = model.getResource("http://w3id.org/hobbit/experiments#New");
        model.add(subject, model.getProperty(DATA_POINT_COUNT_INPUT_NAME), model.createTypedLiteral(EXPECTED_DATA_POINTS_COUNT));
        model.add(subject, model.getProperty(PROBABILITY_THRESHOLD_INPUT_NAME), model.createTypedLiteral(PROBABILITY_THRESHOLD_DEFAULT));
        model.add(subject, model.getProperty("http://w3id.org/hobbit/vocab#involvesSystemInstance"), model.createTypedLiteral("http://example.com/system1"));
        model.add(subject, model.getProperty("http://w3id.org/hobbit/vocab#involvesBenchmark"), model.createTypedLiteral("http://example.com/benchmark1"));
        model.add(subject, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource("http://w3id.org/hobbit/vocab#Experiment"));
        return RabbitMQUtils.writeModel(model);
    }
}