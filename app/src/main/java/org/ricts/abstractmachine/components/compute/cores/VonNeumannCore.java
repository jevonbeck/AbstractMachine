package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeCoreInterface;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;
import org.ricts.abstractmachine.components.network.MemoryPortMux;
import org.ricts.abstractmachine.components.observables.ObservableMultiplexer;

public class VonNeumannCore extends UniMemoryCpuCore {
    private ObservableMultiplexer multiplexer;

    public VonNeumannCore(UniMemoryComputeCoreInterface core, MemoryPort dataMemory){
        super(core, dataMemory);
    }

    @Override
    protected ControlUnitCore createControlUnit(ComputeCoreInterface core, ReadPort instructionCache) {
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