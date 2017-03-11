package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;

public class HarvardCore extends CpuCore {

    public HarvardCore(ComputeCoreInterface core, ReadPort instructionCache){
        createObservableControlUnit(core, instructionCache);
    }

    @Override
    protected CuDataInterface createControlUnit(ComputeCoreInterface core,
                                                ReadPort instructionCache) {
        return new PipelinedControlUnit(core, instructionCache);
    }
}
