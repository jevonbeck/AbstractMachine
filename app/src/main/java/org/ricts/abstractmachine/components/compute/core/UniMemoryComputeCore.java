package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;

/**
 * Created by Jevon on 10/03/2017.
 */

public abstract class UniMemoryComputeCore extends ComputeCore {
    protected MemoryPort dataMemory;

    public void setDataMemory(MemoryPort memory) {
        dataMemory = memory;
    }
}
