package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.storage.*;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.cpu.VonNeumannCpu;

public class VonNeumannArchitecture extends SystemArchitecture {
    private RAM mainMemory;

    public VonNeumannArchitecture(ComputeCore core, int memAccessTime){
        super();

        mainMemory = new RAM(core.dataWidth(), core.dAddrWidth(), memAccessTime);
        processorCore = new VonNeumannCpu(core, mainMemory);
    }
}