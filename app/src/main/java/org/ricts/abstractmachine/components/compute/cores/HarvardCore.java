package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;

public class HarvardCore extends CpuCore {
    private ObservableControlUnit cu; // Control Unit

    public HarvardCore(ComputeCoreInterface core, ReadPort instructionCache, MemoryPort dataMemory){
        cu = new ObservableControlUnit(createControlUnit(core, instructionCache, dataMemory));
    }

    @Override
    protected CuDataInterface createControlUnit(ComputeCoreInterface core,
                                                ReadPort instructionCache,
                                                MemoryPort dataMemory) {
        return new PipelinedControlUnit(core, instructionCache, dataMemory);
    }

    @Override
    public ObservableControlUnit getControlUnit() {
        return cu;
    }

}
