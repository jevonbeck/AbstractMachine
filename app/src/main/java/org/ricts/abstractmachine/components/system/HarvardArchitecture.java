package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.cores.HarvardCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.observables.ObservableROM;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.components.storage.ROM;

import java.util.List;

public class HarvardArchitecture extends SystemArchitecture {
    private ObservableROM<ROM> instructionCache;
    private ObservableRAM dataRAM;
    private ObservableControlUnit controlUnit;

    public HarvardArchitecture(ComputeCore core, int iMemAccessTime, int dMemAccessTime) {
        super(core);

        instructionCache = new  ObservableROM<ROM>(new ROM(core.instrWidth(), core.iAddrWidth(), iMemAccessTime));
        dataRAM = new ObservableRAM(new RAM(core.dataWidth(), core.dAddrWidth(), dMemAccessTime));

        HarvardCore hCore = new HarvardCore(mainCore, instructionCache, dataRAM);
        controlUnit = hCore.getControlUnit();

        tpu = hCore;
    }

    public void initInstructionCache(List<Integer> data, int addrOffset){
        instructionCache.getType().setData(data, addrOffset);
    }

    public void initDataMemory(List<Integer> data, int addrOffset){
        dataRAM.getType().setData(data, addrOffset);
    }

    public void initInstructionCache(List<Integer> data){
        initInstructionCache(data, 0);
    }

    public void initDataMemory(List<Integer> data){
        initDataMemory(data, 0);
    }

    public ObservableRAM getDataMemory(){
        return dataRAM;
    }

    public ObservableROM<ROM> getInstructionCache(){
        return instructionCache;
    }

    public ObservableControlUnit getControlUnit(){
        return controlUnit;
    }
}