package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark.ParrotTask;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Katerinenko
 */
public class ParrotBenchmarkParamsTest {
    private static final String EXPERIMENT_URI = "http://w3id.org/hobbit/vocab#Experiment";
    private static final String STRING_PROPERTY_URI = "http://example.com/stringParam";
    private static final String EXPECTED_STRING_VALUE = "paramValue";
    private static final int EXPECTED_INT_VALUE = 88;

    @Test
    public void checkOutputFromPlatformController() {
        JenaKeyValue jenaKeyValue = new JenaKeyValue(EXPERIMENT_URI);
        jenaKeyValue.setValue(STRING_PROPERTY_URI, EXPECTED_STRING_VALUE);
        byte[] bytesEncoding = jenaKeyValue.toBytes();
        checkPlatformControllerCanParse(bytesEncoding);
    }

    private static void checkPlatformControllerCanParse(byte[] keyValueEncoded) {
        Model model = RabbitMQUtils.readModel(keyValueEncoded);
        StmtIterator iterator = model.listStatements(model.getResource(EXPERIMENT_URI), null, (RDFNode) null);
        Assert.assertTrue(iterator.hasNext());
    }

    @Test
    public void checkInputFromPlatformController() {
        byte[] bytesEncoding = newInputFromPlatformController();
        JenaKeyValue keyValue = new JenaKeyValue.Builder().buildFrom(bytesEncoding);
        int actualValue = keyValue.getIntValueFor(ParrotTask.MESSAGE_COUNT_INPUT_NAME);
        Assert.assertTrue(EXPECTED_INT_VALUE == actualValue);
    }

    private byte[] newInputFromPlatformController() {
        Model model = ModelFactory.createDefaultModel();
        Resource subject = model.getResource("http://w3id.org/hobbit/experiments#New");
        model.add(subject, model.getProperty(ParrotTask.MESSAGE_COUNT_INPUT_NAME), model.createTypedLiteral(EXPECTED_INT_VALUE));
        model.add(subject, model.getProperty("http://w3id.org/hobbit/vocab#involvesSystemInstance"), model.createTypedLiteral("http://example.com/system1"));
        model.add(subject, model.getProperty("http://w3id.org/hobbit/vocab#involvesBenchmark"), model.createTypedLiteral("http://example.com/benchmark1"));
        model.add(subject, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource("http://w3id.org/hobbit/vocab#Experiment"));
        return RabbitMQUtils.writeModel(model);
    }

}