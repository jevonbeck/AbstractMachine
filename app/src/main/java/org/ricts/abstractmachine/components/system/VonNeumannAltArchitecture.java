package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.core.UniMemoryComputeAltCore;
import org.ricts.abstractmachine.components.compute.core.UniMemoryComputeCore;
import org.ricts.abstractmachine.components.compute.core.VonNeumannAltCore;
import org.ricts.abstractmachine.components.compute.core.VonNeumannCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiplexer;
import org.ricts.abstractmachine.components.observable.ObservableUniMemoryComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableUniMemoryComputeCore;
import org.ricts.abstractmachine.components.storage.RAM;

import java.util.List;

public class VonNeumannAltArchitecture extends SystemAltArchitecture<UniMemoryComputeAltCore> {
    private ObservableMemoryPort mainMemory;
    private ControlUnitAltCore controlUnit;
    private ObservableMultiplexer multiplexer;
    private ObservableMultiMemoryPort multiplexerPorts;

    public VonNeumannAltArchitecture(UniMemoryComputeAltCore core, int memAccessTime){
        super(core);
        mainMemory = new ObservableMemoryPort(new RAM(decoderUnit.instrWidth(), decoderUnit.iAddrWidth(), memAccessTime));

        VonNeumannAltCore vCore = new VonNeumannAltCore((ObservableUniMemoryComputeAltCore) mainCore, mainMemory);
        controlUnit = vCore.getControlUnit();

        multiplexer = vCore.getObservableMultiplexer();
        multiplexerPorts = vCore.getObservableMultiMemoryPort();

        tpu = vCore;
    }

    public void initMemory(List<Integer> data) {
        mainMemory.setData(data);
    }

    public ObservableMemoryPort getMainMemory(){
        return mainMemory;
    }

    public ControlUnitAltCore getControlUnit(){
        return controlUnit;
    }

    public ObservableMultiplexer getMultiplexer() {
        return multiplexer;
    }

    public ObservableMultiMemoryPort getMultiplexerPorts() {
        return multiplexerPorts;
    }

    @Override
    protected ObservableComputeAltCore<UniMemoryComputeAltCore> createObservableComputeCore(UniMemoryComputeAltCore core) {
        return new ObservableUniMemoryComputeAltCore<>(core);
    }
}