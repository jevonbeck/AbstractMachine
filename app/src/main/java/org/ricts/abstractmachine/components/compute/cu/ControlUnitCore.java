package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.observables.ObservableCuRegCore;

/**
 * Created by Jevon on 11/03/2017.
 */

public abstract class ControlUnitCore implements CuDataInterface {
    protected abstract CuRegCore createRegCore(ReadPort instructionCache, int pcWidth, int irWidth);

    protected ObservableCuRegCore regCore;

    public ControlUnitCore(ReadPort instructionCache, int pcWidth, int irWidth) {
        regCore = new ObservableCuRegCore(createRegCore(instructionCache, pcWidth, irWidth));
    }

    @Override
    public void reset() {
        setStartExecFrom(0);
    }

    public ObservableCuRegCore getRegCore() {
        return regCore;
    }
}
