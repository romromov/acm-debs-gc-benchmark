package com.agtinternational.hobbit.deployment;

import com.agtinternational.hobbit.core.KeyValue;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.*;
import org.hobbit.core.rabbit.RabbitMQUtils;

import java.util.Map;


/**
 * @author Roman Katerinenko
 */
public class JenaKeyValue extends KeyValue {
    public static String DEFAULT_EXPERIMENT_URI = "http://jenaKeyValue.com/experimentUri";

    private final String experimentUri;

    public JenaKeyValue(String experimentUri) {
        this.experimentUri = experimentUri;
    }

    public JenaKeyValue() {
        this.experimentUri = DEFAULT_EXPERIMENT_URI;
    }

    public String encodeToString() {
        return RabbitMQUtils.writeModel2String(toModel());
    }

    public byte[] toBytes() {
        return RabbitMQUtils.writeModel(toModel());
    }

    public Model toModel() {
        Model model = ModelFactory.createDefaultModel();
        Resource subject = model.createResource(experimentUri);
        for (Map.Entry<String, Object> entry : getEntries()) {
            Literal literal = model.createTypedLiteral(entry.getValue());
            Property property = model.createProperty(entry.getKey());
            model.add(subject, property, literal);
        }
        return model;
    }

    public static class Builder {
        public JenaKeyValue buildFrom(String string) {
            Model model = RabbitMQUtils.readModel(string);
            return buildFrom(model);
        }

        public JenaKeyValue buildFrom(byte[] bytes) {
            Model model = RabbitMQUtils.readModel(bytes);
            return buildFrom(model);
        }

        public JenaKeyValue buildFrom(Model model) {
            JenaKeyValue keyValue = new JenaKeyValue();
            StmtIterator iterator = model.listStatements(null, null, (RDFNode) null);
            while (iterator.hasNext()) {
                Statement statement = iterator.nextStatement();
                String propertyUri = statement.getPredicate().getURI();
                RDFNode object = statement.getObject();
                if (object.isLiteral()) {
                    Literal literal = object.asLiteral();
                    RDFDatatype datatype = literal.getDatatype();
                    Object value = datatype.parse(literal.getLexicalForm());
                    keyValue.setValue(propertyUri, value);
                }
            }
            return keyValue;
        }
    }
}