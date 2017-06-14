package com.agtinternational.hobbit.testutils;

import org.hobbit.core.components.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Katerinenko
 */
public class ComponentsExecutor {
    private final static int AWAIT_TERMINATION_MILLIS = 1;
    private final static int CORE_POOL_SIZE = 100;

    private final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor = new ThreadPoolExecutor(0, CORE_POOL_SIZE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    public void submit(Runnable runnable) {
        executor.submit(runnable);
    }

    public void submit(Component component) {
        executor.submit(() -> {
            try {
                component.init();
                component.run();
            } catch (Throwable e) {
                exceptions.add(e);
            } finally {
                try {
                    component.close();
                } catch (IOException e) {
                    exceptions.add(e);
                }
            }
        });
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();
        while (executor.isTerminated()) {
            executor.awaitTermination(AWAIT_TERMINATION_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    public Collection<Throwable> getExceptions() {
        return exceptions;
    }

    public boolean anyExceptions() {
        return !getExceptions().isEmpty();
    }
}