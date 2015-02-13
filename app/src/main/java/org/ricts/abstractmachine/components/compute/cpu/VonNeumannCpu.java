package org.ricts.abstractmachine.components.compute.cpu;

import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.compute.CpuCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;

public class VonNeumannCpu extends CpuCore {
    public VonNeumannCpu(ComputeCore core, MemoryPort mainMemory){
        super(core, mainMemory, mainMemory);
    }
}