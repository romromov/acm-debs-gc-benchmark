package com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark;

import com.agtinternational.hobbit.core.AbstractCommunicationProtocol;
import com.agtinternational.hobbit.core.SystemComponent;
import com.agtinternational.hobbit.io.RabbitMqCommunication;

/**
 * @author Roman Katerinenko
 */
public class ParrotSystemComponent extends SystemComponent {
    public ParrotSystemComponent() {
        super(newSystemCommunicationProtocol());
    }

    private static AbstractCommunicationProtocol newSystemCommunicationProtocol() {
        RabbitMqCommunication.Builder communicationBuilder = new RabbitMqCommunication.Builder();
        return new RepeatProtocol(communicationBuilder);
    }
}