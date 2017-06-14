package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agtinternational.hobbit.core.KeyValue;
import com.agtinternational.hobbit.core.SystemComponent;
import com.agtinternational.hobbit.io.RabbitMqCommunication;

/**
 * @author Roman Katerinenko
 */
public class DebsCsvSystemNegative extends SystemComponent {
    public DebsCsvSystemNegative(KeyValue inputParameters) {
        super(new DebsSystemNegativeProtocol(new RabbitMqCommunication.Builder(), inputParameters));
    }
}