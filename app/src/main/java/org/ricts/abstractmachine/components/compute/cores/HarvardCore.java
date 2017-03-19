package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class HarvardCore extends CpuCore {

    public HarvardCore(ComputeCoreInterface core, ReadPort instructionCache){
        createObservableControlUnit(core, instructionCache);
    }

    @Override
    protected ControlUnitCore createControlUnit(ComputeCoreInterface core,
                                                ReadPort instructionCache) {
        return new PipelinedControlUnit(core, instructionCache);
    }
}
