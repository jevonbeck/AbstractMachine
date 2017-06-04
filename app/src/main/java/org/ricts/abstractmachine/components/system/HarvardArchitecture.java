package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.core.HarvardCore;
import org.ricts.abstractmachine.components.compute.core.UniMemoryComputeCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableReadPort;
import org.ricts.abstractmachine.components.observable.ObservableUniMemoryComputeCore;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.components.storage.ROM;

import java.util.List;

public class HarvardArchitecture extends SystemArchitecture<UniMemoryComputeCore> {
    private ObservableReadPort<ROM> instructionCache;
    private ObservableMemoryPort dataRAM;
    private ControlUnitCore controlUnit;

    public HarvardArchitecture(UniMemoryComputeCore core, int iMemAccessTime, int dMemAccessTime) {
        super(core);

        instructionCache = new ObservableReadPort<ROM>(new ROM(core.instrWidth(), core.iAddrWidth(), iMemAccessTime));
        dataRAM = new ObservableMemoryPort(new RAM(core.dataWidth(), core.dAddrWidth(), dMemAccessTime));
        core.setDataMemory(dataRAM);

        HarvardCore hCore = new HarvardCore(mainCore, instructionCache);
        controlUnit = hCore.getControlUnit();

        tpu = hCore;
    }

    public void initInstructionCache(List<Integer> data){
        instructionCache.setData(data);
    }

    public void initDataMemory(List<Integer> data){
        dataRAM.setData(data);
    }

    public ObservableMemoryPort getDataMemory(){
        return dataRAM;
    }

    public ObservableReadPort<ROM> getInstructionCache(){
        return instructionCache;
    }

    public ControlUnitCore getControlUnit(){
        return controlUnit;
    }

    @Override
    protected ObservableComputeCore<UniMemoryComputeCore> createObservableComputeCore(UniMemoryComputeCore core) {
        return new ObservableUniMemoryComputeCore<>(core);
    }
}