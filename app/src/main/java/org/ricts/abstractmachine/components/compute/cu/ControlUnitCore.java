package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitState.GenericCUState;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.observables.ObservableCuRegCore;
import org.ricts.abstractmachine.components.observables.ObservableCuFSM;

/**
 * Created by Jevon on 11/03/2017.
 */

public abstract class ControlUnitCore implements ControlUnitInterface {
    protected static final String ACTIVE_STATE = GenericCUState.ACTIVE.name();
    protected static final String FETCH_STATE = GenericCUState.FETCH.name();
    private static final String HALT_STATE = GenericCUState.HALT.name();
    private static final String SLEEP_STATE = GenericCUState.SLEEP.name();

    protected abstract CuRegCore createRegCore(ReadPort instructionCache, int pcWidth, int irWidth);
    protected abstract CuFsmInterface createMainFSM(ControlUnitRegCore regCore, ComputeCoreInterface core);

    protected ObservableCuRegCore regCore;
    protected ObservableCuFSM mainFSM;

    public ControlUnitCore(ComputeCoreInterface core, ReadPort instructionCache) {
        regCore = new ObservableCuRegCore(createRegCore(instructionCache, core.iAddrWidth(), core.instrWidth()));
        mainFSM = new ObservableCuFSM(createMainFSM(regCore, core));
    }

    @Override
    public void reset() {
        setStartExecFrom(0);
    }

    @Override
    public void setNextStateToHalt() {
        mainFSM.setNextState(HALT_STATE);
    }

    @Override
    public void setNextStateToSleep() {
        mainFSM.setNextState(SLEEP_STATE);
    }

    public ObservableCuRegCore getRegCore() {
        return regCore;
    }

    public ObservableCuFSM getMainFSM() {
        return mainFSM;
    }
}
