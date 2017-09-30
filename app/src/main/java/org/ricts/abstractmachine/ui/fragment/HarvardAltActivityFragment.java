package org.ricts.abstractmachine.ui.fragment;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableReadPort;
import org.ricts.abstractmachine.components.storage.ROM;

public abstract class HarvardAltActivityFragment extends InspectFragment {
    protected ObservableComputeAltCore mainCore;
    protected ObservableReadPort<ROM> instructionCache;
    protected ObservableMemoryPort dataMemory;
    protected ControlUnitAltCore controlUnit;

    private int actionCount = 0;

    public HarvardAltActivityFragment() {
        // Required empty public constructor
    }

    public void setObservables(ObservableComputeAltCore core, ObservableReadPort<ROM> iCache, ObservableMemoryPort dataMem,
                               ControlUnitAltCore controlUnitData){
        mainCore = core;
        instructionCache = iCache;
        dataMemory = dataMem;
        controlUnit = controlUnitData;

        observablesReady = true;
        attemptInit();
    }

    protected void notifyStepActionListener(){
        ++actionCount;

        if((actionCount & 1) == 0) { // actionCount divisible by 2
            mListener.onStepActionCompleted();
        }
    }
}
