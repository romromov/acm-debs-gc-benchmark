package com.agtinternational.hobbit;

import org.hobbit.core.Constants;

/**
 * @author Roman Katerinenko
 */
public class Utils {
    private Utils() {
    }

    public static String toPlatformQueueName(String queueName) {
        return queueName + "." + System.getenv().get(Constants.HOBBIT_SESSION_ID_KEY);
    }
}