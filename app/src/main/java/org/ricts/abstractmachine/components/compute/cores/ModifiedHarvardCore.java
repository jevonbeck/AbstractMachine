package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MemoryPortMuxCore;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;

public class ModifiedHarvardCore extends UniMemoryCpuCore {

    public ModifiedHarvardCore(ComputeCoreInterface core, MemoryPort dataMemory){
        super(core, dataMemory);
    }

    @Override
    protected MultiPortSerializer<MemoryPort, MemoryPortMuxCore> createSerializer(MemoryPort memory, int inputCount) {
        return null; // TODO: implement serializer core
    }

    @Override
    protected CuDataInterface createControlUnit(ComputeCoreInterface core,
                                                ReadPort instructionCache,
                                                MemoryPort dataMemory) {
        return new PipelinedControlUnit(core, instructionCache, dataMemory);
    }
}
