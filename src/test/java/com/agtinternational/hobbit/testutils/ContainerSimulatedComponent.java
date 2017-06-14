package com.agtinternational.hobbit.testutils;

import com.agtinternational.hobbit.io.CommandSender;
import org.hobbit.core.components.Component;

import java.io.IOException;

/**
 * This component simulates signals that would be produced if delegate run as a docker container
 *
 * @author Roman Katerinenko
 */
public class ContainerSimulatedComponent implements Component {
    private final Component delegate;
    private final String containerName;

    public ContainerSimulatedComponent(Component component, String containerName) {
        this.delegate = component;
        this.containerName = containerName;
    }

    @Override
    public void run() throws Exception {
        delegate.run();
    }

    @Override
    public void init() throws Exception {
        delegate.init();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        byte noErrors = 0;
        try {
            CommandSender.sendContainerTerminatedCommand(containerName, noErrors);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}