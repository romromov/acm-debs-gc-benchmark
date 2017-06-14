package com.agtinternational.hobbit.testutils.commandreactions;

import java.util.function.BiConsumer;

/**
 * @author Roman Katerinenko
 */
public interface CommandReaction extends BiConsumer<Byte, byte[]> {
}