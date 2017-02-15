package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.cores.VonNeumannCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiplexer;
import org.ricts.abstractmachine.components.storage.RAM;

import java.util.List;

public class VonNeumannArchitecture extends SystemArchitecture {
    private ObservableMemoryPort mainMemory;
    private ObservableControlUnit controlUnit;
    private ObservableMultiplexer multiplexer;
    private ObservableMultiMemoryPort multiplexerPorts;

    public VonNeumannArchitecture(ComputeCore core, int memAccessTime){
        super(core);
        mainMemory = new ObservableMemoryPort(new RAM(core.instrWidth(), core.iAddrWidth(), memAccessTime));

        VonNeumannCore vCore = new VonNeumannCore(mainCore, mainMemory);
        controlUnit = vCore.getControlUnit();

        multiplexer = vCore.getObservableMultiplexer();
        multiplexerPorts = vCore.getObservableMultiMemoryPort();

        tpu = vCore;
    }

    public void initMemory(List<Integer> data) {
        initMemory(data, 0);
    }

    public void initMemory(List<Integer> data, int addrOffset){
        ((RAM) mainMemory.getType()).setData(data, addrOffset);
    }

    public ObservableMemoryPort getMainMemory(){
        return mainMemory;
    }

    public ObservableControlUnit getControlUnit(){
        return controlUnit;
    }

    public ObservableMultiplexer getMultiplexer() {
        return multiplexer;
    }

    public ObservableMultiMemoryPort getMultiplexerPorts() {
        return multiplexerPorts;
    }
}