package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agt.ferromatikdata.anomalydetector.ClusterRandomizer;
import com.agt.ferromatikdata.core.Machines;
import com.agtinternational.hobbit.core.KeyValue;

import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.MACHINE_COUNT_INPUT_NAME;

/**
 * @author Roman Katerinenko
 */
class DebsMachinesInputHandler {
    private final int machineCount;

    DebsMachinesInputHandler(KeyValue inputParams) {
        machineCount = inputParams.getIntValueFor(MACHINE_COUNT_INPUT_NAME);
    }

    Machines getMachines() {
        return Machines.newNMachinesDefault(ClusterRandomizer.newDefault(), machineCount, 0);
    }
}