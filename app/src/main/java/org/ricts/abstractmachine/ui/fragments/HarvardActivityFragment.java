package org.ricts.abstractmachine.ui.fragments;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableReadPort;
import org.ricts.abstractmachine.components.storage.ROM;

public abstract class HarvardActivityFragment extends InspectFragment {
    protected ObservableComputeCore mainCore;
    protected ObservableReadPort<ROM> instructionCache;
    protected ObservableMemoryPort dataMemory;
    protected ControlUnitCore controlUnit;

    private int actionCount = 0;

    public HarvardActivityFragment() {
        // Required empty public constructor
    }

    public void setObservables(ObservableComputeCore core, ObservableReadPort<ROM> iCache, ObservableMemoryPort dataMem,
                               ControlUnitCore controlUnitData){
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
