package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeCore;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;

public class ModifiedHarvardCore extends UniMemoryCpuCore {

    public ModifiedHarvardCore(UniMemoryComputeCore core, MemoryPort dataMemory){
        super(core, dataMemory);
    }

    @Override
    protected MultiPortSerializer<MemoryPort, MultiMemoryPort> createSerializer(MemoryPort memory, int inputCount) {
        return null; // TODO: implement serializer core
    }

    @Override
    protected ControlUnitCore createControlUnit(ComputeCore core, ReadPort instructionCache) {
        return new PipelinedControlUnit(core, instructionCache);
    }
}
