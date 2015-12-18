package org.ricts.abstractmachine.components.compute.cpu;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.cores.VonNeumannCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;

public class VonNeumannCpu extends VonNeumannCore {
    public VonNeumannCpu(ComputeCore core, MemoryPort mainMemory){
        super(core, mainMemory);
    }
}