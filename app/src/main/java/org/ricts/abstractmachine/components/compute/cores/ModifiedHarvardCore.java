package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ModifiedHarvardCore extends CpuCore {

    public ModifiedHarvardCore(ComputeCoreInterface core, MemoryPort dataMemory){
        super(core, dataMemory, dataMemory);
    }

    @Override
    protected CuDataInterface createControlUnit(ComputeCoreInterface core,
                                                ReadPort instructionCache,
                                                MemoryPort dataMemory) {
        return new PipelinedControlUnit(core, instructionCache, dataMemory);
    }
}
