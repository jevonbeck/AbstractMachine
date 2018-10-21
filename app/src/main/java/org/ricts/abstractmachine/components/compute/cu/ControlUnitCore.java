package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.cu.fsm.ControlUnitState.GenericCUState;
import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.interfaces.FetchCore;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.observable.ObservableCuFSM;
import org.ricts.abstractmachine.components.observable.ObservableDefaultValueSource;
import org.ricts.abstractmachine.components.observable.ObservableFetchCore;

/**
 * Created by Jevon on 11/03/2017.
 */

public abstract class ControlUnitCore implements ControlUnitInterface {
    protected static final String ACTIVE_STATE = GenericCUState.ACTIVE.name();
    protected static final String FETCH_STATE = GenericCUState.FETCH.name();
    private static final String HALT_STATE = GenericCUState.HALT.name();
    private static final String SLEEP_STATE = GenericCUState.SLEEP.name();

    protected abstract FetchUnit createRegCore(ReadPort instructionCache, int pcWidth, int irWidth);
    protected abstract CuFsmInterface createMainFSM(FetchCore regCore, ComputeCore core);
    protected abstract DefaultValueSource createDefaultValueSource();
    protected abstract void resetInternal();

    protected ObservableFetchCore regCore;
    protected DecoderUnit decoderUnit;
    protected ObservableCuFSM mainFSM;
    private ObservableDefaultValueSource irDefaultValueSource;

    public ControlUnitCore(ComputeCore core, ReadPort instructionCache) {
        decoderUnit = core.getDecoderUnit();
        regCore = new ObservableFetchCore(createRegCore(instructionCache, decoderUnit.iAddrWidth(), decoderUnit.instrWidth()));
        mainFSM = new ObservableCuFSM(createMainFSM(regCore, core));
        irDefaultValueSource = new ObservableDefaultValueSource(createDefaultValueSource());
    }

    @Override
    public void reset() {
        setStartExecFrom(0);
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        regCore.setPcAndIr(currentPC, irDefaultValueSource.defaultValue());
        mainFSM.reset();
        resetInternal();
    }

    @Override
    public void setNextStateToHalt() {
        mainFSM.setNextState(HALT_STATE);
    }

    @Override
    public void setNextStateToSleep() {
        mainFSM.setNextState(SLEEP_STATE);
    }

    @Override
    public int fsmStageCount() {
        return mainFSM.parallelStageCount();
    }

    public ObservableFetchCore getRegCore() {
        return regCore;
    }

    public ObservableCuFSM getMainFSM() {
        return mainFSM;
    }

    public ObservableDefaultValueSource getIrDefaultValueSource() {
        return irDefaultValueSource;
    }
}
