package org.ricts.abstractmachine.components.compute.core;

import android.content.res.Resources;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeCoreInterface;

/**
 * Created by Jevon on 10/03/2017.
 */

public abstract class UniMemoryComputeCore extends ComputeCore implements UniMemoryComputeCoreInterface {
    protected MemoryPort dataMemory;

    public UniMemoryComputeCore(Resources res) {
        super(res);
    }

    @Override
    public void setDataMemory(MemoryPort memory) {
        dataMemory = memory;
    }
}
