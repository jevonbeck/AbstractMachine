package org.ricts.abstractmachine.ui.fragments;

import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.observables.ObservableROM;
import org.ricts.abstractmachine.components.storage.ROM;

public abstract class HarvardActivityFragment extends InspectFragment {
    protected ObservableComputeCore mainCore;
    protected ObservableROM<ROM> instructionCache;
    protected ObservableRAM dataMemory;
    protected ObservableControlUnit controlUnit;

    private int actionCount = 0;

    public HarvardActivityFragment() {
        // Required empty public constructor
    }

    public void setObservables(ObservableComputeCore core, ObservableROM<ROM> iCache, ObservableRAM dataMem,
                               ObservableControlUnit controlUnitData){
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
