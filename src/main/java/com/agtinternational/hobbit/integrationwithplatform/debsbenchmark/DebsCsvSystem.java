package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agtinternational.hobbit.core.KeyValue;
import com.agtinternational.hobbit.core.SystemComponent;
import com.agtinternational.hobbit.io.RabbitMqCommunication;

/**
 * @author Roman Katerinenko
 */
public class DebsCsvSystem extends SystemComponent {
    public DebsCsvSystem(KeyValue inputParameters) {
        super(new DebsSystemProtocol(new RabbitMqCommunication.Builder(), inputParameters));
    }
}