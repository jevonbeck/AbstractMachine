package org.ricts.abstractmachine.ui.fragments;

import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;

public abstract class VonNeumannActivityFragment extends InspectFragment {
    protected ObservableComputeCore mainCore;
    protected ObservableRAM mainMemory;
    protected ObservableControlUnit controlUnit;

    public VonNeumannActivityFragment() {
        // Required empty public constructor
    }

    public void setObservables(ObservableComputeCore core, ObservableRAM memData,
                               ObservableControlUnit controlUnitData){
        mainCore = core;
        mainMemory = memData;
        controlUnit = controlUnitData;

        observablesReady = true;
        attemptInit();
    }
}
