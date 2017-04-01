package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.compute.cores.UniMemoryComputeCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeCoreInterface;

/**
 * Created by Jevon on 10/03/2017.
 */

public class ObservableUniMemoryComputeCore<T extends UniMemoryComputeCore>
        extends ObservableComputeCore<T> implements UniMemoryComputeCoreInterface {

    public ObservableUniMemoryComputeCore(T core) {
        super(core);
    }

    @Override
    public void setDataMemory(MemoryPort memory) {
        observable_data.setDataMemory(memory);
    }
}
