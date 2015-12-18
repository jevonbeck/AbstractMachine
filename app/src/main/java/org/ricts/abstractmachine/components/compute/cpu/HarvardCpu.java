package org.ricts.abstractmachine.components.compute.cpu;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.cores.HarvardCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class HarvardCpu extends HarvardCore {
    public HarvardCpu(ComputeCore core, ReadPort instructionCache, MemoryPort dataMemory){
        super(core, instructionCache, dataMemory);
    }
}