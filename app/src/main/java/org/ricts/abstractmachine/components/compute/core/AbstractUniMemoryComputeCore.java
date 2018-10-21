package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeCore;

/**
 * Created by Jevon on 10/03/2017.
 */

public abstract class AbstractUniMemoryComputeCore extends AbstractComputeCore implements UniMemoryComputeCore {
    protected MemoryPort dataMemory;

    public AbstractUniMemoryComputeCore(DecoderUnit decoder) {
        super(decoder);
    }

    @Override
    public void setDataMemory(MemoryPort memory) {
        dataMemory = memory;
    }
}
