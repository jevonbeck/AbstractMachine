package org.ricts.abstractmachine.ui.fragment;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiplexer;

public abstract class VonNeumannAltActivityFragment extends InspectFragment {
    protected ObservableComputeAltCore mainCore;
    protected ObservableMemoryPort mainMemory;
    protected ControlUnitAltCore controlUnit;

    protected ObservableMultiplexer muxSelector;
    protected ObservableMultiMemoryPort muxInputPorts;

    public VonNeumannAltActivityFragment() {
        // Required empty public constructor
    }

    public void setObservables(ObservableComputeAltCore core, ObservableMemoryPort memData,
                               ControlUnitAltCore controlUnitData){
        mainCore = core;
        mainMemory = memData;
        controlUnit = controlUnitData;

        observablesReady = true;
        attemptInit();
    }

    public void setObservables(ObservableComputeAltCore core, ObservableMemoryPort memData,
                               ControlUnitAltCore controlUnitData, ObservableMultiplexer muxSelect,
                               ObservableMultiMemoryPort muxPorts){
        muxSelector = muxSelect;
        muxInputPorts = muxPorts;
        setObservables(core, memData, controlUnitData);
    }
}
