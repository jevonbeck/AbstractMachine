package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitAlt;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeAltCoreInterface;
import org.ricts.abstractmachine.components.network.MemoryPortMux;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;
import org.ricts.abstractmachine.components.observable.ObservableMultiplexer;

public class VonNeumannAltCore extends UniMemoryCpuAltCore {
    private ObservableMultiplexer multiplexer;

    public VonNeumannAltCore(UniMemoryComputeAltCoreInterface core, MemoryPort dataMemory){
        super(core, dataMemory);
    }

    @Override
    protected ControlUnitAltCore createControlUnit(CompCore core, ReadPort instructionCache) {
        return new ControlUnitAlt(core, instructionCache, getObservableMultiplexer());
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