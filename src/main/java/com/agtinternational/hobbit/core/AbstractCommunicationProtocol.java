package com.agtinternational.hobbit.core;

import com.agtinternational.hobbit.io.NetworkCommunication;

/**
 * @author Roman Katerinenko
 */
public abstract class AbstractCommunicationProtocol {
    private final NetworkCommunication.Builder communicationBuilder;

    protected AbstractCommunicationProtocol(NetworkCommunication.Builder communicationBuilder) {
        this.communicationBuilder = communicationBuilder;
    }

    public abstract void execute() throws Exception;

    protected NetworkCommunication.Builder getCommunicationBuilder() {
        return communicationBuilder;
    }
}