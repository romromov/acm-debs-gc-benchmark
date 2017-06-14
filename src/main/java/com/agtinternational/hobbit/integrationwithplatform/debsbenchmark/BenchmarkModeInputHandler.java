package com.agtinternational.hobbit.integrationwithplatform.debsbenchmark;

import com.agt.ferromatikdata.core.GeneratorTask;
import com.agt.ferromatikdata.core.GrowingGenerationTask;
import com.agt.ferromatikdata.core.Machines;
import com.agtinternational.hobbit.core.KeyValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.BENCHMARK_MODE_DEBS_DYNAMIC;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.BENCHMARK_MODE_INPUT_NAME;
import static com.agtinternational.hobbit.integrationwithplatform.debsbenchmark.DebsConstants.DATA_POINT_COUNT_INPUT_NAME;

/**
 * @author Roman Katerinenko
 */
class BenchmarkModeInputHandler {
    private static final Pattern pattern = Pattern.compile(BENCHMARK_MODE_DEBS_DYNAMIC + ":([0-9]+):([0-9]+)");
    private final KeyValue inputParams;
    private final Machines machines;

    BenchmarkModeInputHandler(KeyValue inputParams, Machines machines) {
        this.inputParams = inputParams;
        this.machines = machines;
    }

    GeneratorTask getGeneratorTask() {
        int dataPointCount = inputParams.getIntValueFor(DATA_POINT_COUNT_INPUT_NAME);
        String mode = inputParams.getStringValueFor(BENCHMARK_MODE_INPUT_NAME);
        if (mode != null && mode.startsWith(BENCHMARK_MODE_DEBS_DYNAMIC)) {
            Matcher matcher = pattern.matcher(mode);
            if (!matcher.matches()) {
                throw new IllegalStateException("Wrong input format: " + mode);
            } else {
                int initialMachineCount = Integer.valueOf(matcher.group(1));
                int dataPointsBeforeNewMachine = Integer.valueOf(matcher.group(2));
                return new GrowingGenerationTask(machines,
                        dataPointCount,
                        initialMachineCount,
                        dataPointsBeforeNewMachine);
            }
        } else {
            return GeneratorTask.newGeneratorTask(machines, dataPointCount);
        }
    }
}
