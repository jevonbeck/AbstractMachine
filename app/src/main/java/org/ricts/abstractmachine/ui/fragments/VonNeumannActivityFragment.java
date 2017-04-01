package org.ricts.abstractmachine.ui.fragments;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiplexer;

public abstract class VonNeumannActivityFragment extends InspectFragment {
    protected ObservableComputeCore mainCore;
    protected ObservableMemoryPort mainMemory;
    protected ControlUnitCore controlUnit;

    protected ObservableMultiplexer muxSelector;
    protected ObservableMultiMemoryPort muxInputPorts;

    public VonNeumannActivityFragment() {
        // Required empty public constructor
    }

    public void setObservables(ObservableComputeCore core, ObservableMemoryPort memData,
                               ControlUnitCore controlUnitData){
        mainCore = core;
        mainMemory = memData;
        controlUnit = controlUnitData;

        observablesReady = true;
        attemptInit();
    }

    public void setObservables(ObservableComputeCore core, ObservableMemoryPort memData,
                               ControlUnitCore controlUnitData, ObservableMultiplexer muxSelect,
                               ObservableMultiMemoryPort muxPorts){
        muxSelector = muxSelect;
        muxInputPorts = muxPorts;
        setObservables(core, memData, controlUnitData);
    }
}
