package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeCoreInterface;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;

public class ModifiedHarvardCore extends UniMemoryCpuCore {

    public ModifiedHarvardCore(UniMemoryComputeCoreInterface core, MemoryPort dataMemory){
        super(core, dataMemory);
    }

    @Override
    protected MultiPortSerializer<MemoryPort, MultiMemoryPort> createSerializer(MemoryPort memory, int inputCount) {
        return null; // TODO: implement serializer core
    }

    @Override
    protected ControlUnitCore createControlUnit(ComputeCoreInterface core, ReadPort instructionCache) {
        return new PipelinedControlUnit(core, instructionCache);
    }
}
