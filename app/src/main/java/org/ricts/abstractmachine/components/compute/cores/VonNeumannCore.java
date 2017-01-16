package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPortMuxCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;
import org.ricts.abstractmachine.components.network.MemoryPortMux;

public class VonNeumannCore extends UniMemoryCpuCore {

    public VonNeumannCore(ComputeCoreInterface core, MemoryPort dataMemory){
        super(core, dataMemory);
    }

    @Override
    protected CuDataInterface createControlUnit(ComputeCoreInterface core,
                                                ReadPort instructionCache,
                                                MemoryPort dataMemory) {
        return new ControlUnit(core, instructionCache, dataMemory);
    }

    @Override
    protected MultiPortSerializer<MemoryPort, MemoryPortMuxCore> createSerializer(MemoryPort memory, int inputCount) {
        return new MemoryPortMux(memory, inputCount);
    }
}