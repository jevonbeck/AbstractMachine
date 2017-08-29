package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnitAlt;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeAltCoreInterface;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;

public class ModifiedHarvardAltCore extends UniMemoryCpuAltCore {

    public ModifiedHarvardAltCore(UniMemoryComputeAltCoreInterface core, MemoryPort dataMemory){
        super(core, dataMemory);
    }

    @Override
    protected MultiPortSerializer<MemoryPort, MultiMemoryPort> createSerializer(MemoryPort memory, int inputCount) {
        return null; // TODO: implement serializer core
    }

    @Override
    protected ControlUnitAltCore createControlUnit(CompCore core, ReadPort instructionCache) {
        return new PipelinedControlUnitAlt(core, instructionCache);
    }
}
