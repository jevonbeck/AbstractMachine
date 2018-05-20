package org.ricts.abstractmachine.components.observable;

import org.ricts.abstractmachine.components.compute.core.UniMemoryComputeAltCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeAltCoreInterface;

/**
 * Created by Jevon on 10/03/2017.
 */

public class ObservableUniMemoryComputeAltCore<T extends UniMemoryComputeAltCore>
        extends ObservableComputeAltCore<T> implements UniMemoryComputeAltCoreInterface {

    public ObservableUniMemoryComputeAltCore(T core) {
        super(core);
    }

    @Override
    public void setDataMemory(MemoryPort memory) {
        observable_data.setDataMemory(memory);
    }
}
