package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.storage.*;
import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.compute.cpu.HarvardCpu;

public class HarvardArchitecture extends SystemArchitecture {
    private ROM instructionCache;
    private RAM dataRAM;

    public HarvardArchitecture(ComputeCore core, int iMemAccessTime, int dMemAccessTime) {
        super();

        instructionCache = new ROM(core.instrWidth(), core.iAddrWidth(), iMemAccessTime);
        dataRAM = new RAM(core.dataWidth(), core.dAddrWidth(), dMemAccessTime);
        processorCore = new HarvardCpu(core, instructionCache, dataRAM);
    }

    public ROM instructionMemory() {
        return instructionCache;
    }

    public RAM dataMemory() {
        return dataRAM;
    }
}