package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class HarvardCore extends CpuCore {

    public HarvardCore(ComputeCore core, ReadPort instructionCache){
        initCpuCore(core, instructionCache);
    }

    @Override
    protected ControlUnitCore createControlUnit(ComputeCore core,
                                                ReadPort instructionCache) {
        return new PipelinedControlUnit(core, instructionCache);
    }
}
