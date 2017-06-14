package com.agtinternational.hobbit.integrationwithplatform.parrotbenchmark;

import com.agtinternational.hobbit.core.AbstractCommunicationProtocol;
import com.agtinternational.hobbit.core.SystemComponent;
import com.agtinternational.hobbit.io.RabbitMqCommunication;

/**
 * System component that fails intentionally
 *
 * @author Roman Katerinenko
 */
public class ParrotNegativeSystemComponent extends SystemComponent {
    public ParrotNegativeSystemComponent() {
        super(newSystemCommunicationProtocol());
    }

    private static AbstractCommunicationProtocol newSystemCommunicationProtocol() {
        RabbitMqCommunication.Builder communicationBuilder = new RabbitMqCommunication.Builder();
        return new DistortedRepeatProtocol(communicationBuilder);
    }
}