package com.agtinternational.hobbit.testutils.commandreactions;

import com.agtinternational.hobbit.io.CommandSender;
import org.hobbit.core.Commands;
import org.junit.Assert;

/**
 * @author Roman Katerinenko
 */
public class StartSystemWhenItReady implements CommandReaction {
    @Override
    public void accept(Byte command, byte[] data) {
        if (command == Commands.SYSTEM_READY_SIGNAL) {
            try {
                new CommandSender(Commands.TASK_GENERATION_FINISHED).send();
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}