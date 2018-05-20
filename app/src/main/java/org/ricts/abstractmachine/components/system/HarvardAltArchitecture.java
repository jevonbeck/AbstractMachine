package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.core.HarvardAltCore;
import org.ricts.abstractmachine.components.compute.core.UniMemoryComputeAltCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableReadPort;
import org.ricts.abstractmachine.components.observable.ObservableUniMemoryComputeAltCore;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.components.storage.ROM;

import java.util.List;

public class HarvardAltArchitecture extends SystemAltArchitecture<UniMemoryComputeAltCore> {
    private ObservableReadPort<ROM> instructionCache;
    private ObservableMemoryPort dataRAM;
    private ControlUnitAltCore controlUnit;

    public HarvardAltArchitecture(UniMemoryComputeAltCore core, int iMemAccessTime, int dMemAccessTime) {
        super(core);
        instructionCache = new ObservableReadPort<ROM>(new ROM(decoderUnit.instrWidth(), decoderUnit.iAddrWidth(), iMemAccessTime));
        dataRAM = new ObservableMemoryPort(new RAM(decoderUnit.dataWidth(), decoderUnit.dAddrWidth(), dMemAccessTime));
        core.setDataMemory(dataRAM);

        HarvardAltCore hCore = new HarvardAltCore(mainCore, instructionCache);
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

    public ControlUnitAltCore getControlUnit(){
        return controlUnit;
    }

    @Override
    protected ObservableComputeAltCore<UniMemoryComputeAltCore> createObservableComputeCore(UniMemoryComputeAltCore core) {
        return new ObservableUniMemoryComputeAltCore<>(core);
    }
}