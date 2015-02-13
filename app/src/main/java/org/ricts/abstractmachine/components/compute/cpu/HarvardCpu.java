package org.ricts.abstractmachine.components.compute.cpu;

import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.compute.PipelinedCpuCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class HarvardCpu extends PipelinedCpuCore{
    public HarvardCpu(ComputeCore core, ReadPort instructionCache, MemoryPort dataMemory){
        super(core, instructionCache, dataMemory);
    }
}