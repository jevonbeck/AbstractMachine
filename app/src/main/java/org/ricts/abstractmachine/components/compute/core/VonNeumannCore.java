package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeCore;
import org.ricts.abstractmachine.components.network.MemoryPortMux;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;
import org.ricts.abstractmachine.components.observable.ObservableMultiplexer;

public class VonNeumannCore extends UniMemoryCpuCore {
    private ObservableMultiplexer multiplexer;

    public VonNeumannCore(UniMemoryComputeCore core, MemoryPort dataMemory){
        super(core, dataMemory);
    }

    @Override
    protected ControlUnitCore createControlUnit(ComputeCore core, ReadPort instructionCache) {
        return new ControlUnit(core, instructionCache, getObservableMultiplexer());
    }

    @Override
    protected MultiPortSerializer<MemoryPort, MultiMemoryPort> createSerializer(MemoryPort memory, int inputCount) {
        return new MemoryPortMux(memory, inputCount);
    }

    public ObservableMultiplexer getObservableMultiplexer() {
        if(multiplexer == null){
            multiplexer = new ObservableMultiplexer((Multiplexer) serializer);
        }

        return multiplexer;
    }
}