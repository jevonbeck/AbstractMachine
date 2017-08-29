package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeAltCoreInterface;

/**
 * Created by Jevon on 10/03/2017.
 */

public abstract class UniMemoryComputeAltCore extends ComputeAltCore implements UniMemoryComputeAltCoreInterface {
    protected MemoryPort dataMemory;

    public UniMemoryComputeAltCore(DecoderUnit decoder) {
        super(decoder);
    }

    @Override
    public void setDataMemory(MemoryPort memory) {
        dataMemory = memory;
    }
}
