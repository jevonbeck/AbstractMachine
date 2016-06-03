package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.cores.HarvardCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableROM;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.components.storage.ROM;

import java.util.List;

public class HarvardArchitecture extends SystemArchitecture {
    private ObservableROM<ROM> instructionCache;
    private ObservableRAM dataRAM;
    private ObservableControlUnit cu1, cu2;

    public HarvardArchitecture(ComputeCore core, int iMemAccessTime, int dMemAccessTime) {
        super(core);

        instructionCache = new  ObservableROM<ROM>(new ROM(core.instrWidth(), core.iAddrWidth(), iMemAccessTime));
        dataRAM = new ObservableRAM(new RAM(core.dataWidth(), core.dAddrWidth(), dMemAccessTime));

        HarvardCore hCore = new HarvardCore(mainCore, instructionCache, dataRAM);
        cu1 = hCore.getCu1();
        cu2 = hCore.getCu2();

        processorCore = hCore;
    }

    public void initInstructionCache(List<Integer> data, int addrOffset){
        instructionCache.getType().setData(data, addrOffset);
    }

    public void initDataRAM(List<Integer> data, int addrOffset){
        dataRAM.getType().setData(data, addrOffset);
    }

    public void initInstructionCache(List<Integer> data){
        initInstructionCache(data, 0);
    }

    public void initDataRAM(List<Integer> data){
        initDataRAM(data, 0);
    }

    public ObservableRAM getDataRAM(){
        return dataRAM;
    }

    public ObservableROM<ROM> getInstructionCache(){
        return instructionCache;
    }

    public ObservableControlUnit getCu1(){
        return cu1;
    }

    public ObservableControlUnit getCu2(){
        return cu2;
    }
}