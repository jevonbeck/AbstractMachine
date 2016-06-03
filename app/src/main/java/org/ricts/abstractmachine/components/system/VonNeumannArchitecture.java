package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.cores.VonNeumannCore;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.storage.RAM;

import java.util.List;

public class VonNeumannArchitecture extends SystemArchitecture {
    private ObservableRAM mainMemory;
    private ObservableControlUnit controlUnit;

    public VonNeumannArchitecture(ComputeCore core, int memAccessTime){
        super(core);

        mainMemory = new ObservableRAM(new RAM(core.instrWidth(), core.iAddrWidth(), memAccessTime));

        VonNeumannCore vCore = new VonNeumannCore(mainCore, mainMemory);
        controlUnit = vCore.getControlUnit();

        processorCore = vCore;
    }

    public void initMemory(List<Integer> data) {
        initMemory(data, 0);
    }

    public void initMemory(List<Integer> data, int addrOffset){
        mainMemory.getType().setData(data, addrOffset);
    }

    public ObservableRAM getMainMemory(){
        return mainMemory;
    }

    public ObservableControlUnit getControlUnit(){
        return controlUnit;
    }
}