package org.ricts.abstractmachine.ui.fragment;

import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableReadPort;
import org.ricts.abstractmachine.components.storage.ROM;

public abstract class HarvardAltActivityFragment extends InspectFragment {
    protected ObservableComputeAltCore mainCore;
    protected ObservableDecoderUnit decoderUnit;
    protected ObservableReadPort<ROM> instructionCache;
    protected ObservableMemoryPort dataMemory;
    protected ControlUnitAltCore controlUnit;

    protected TextView tempDisplayView;
    private int actionCount = 0;
    private int stageCount;

    public HarvardAltActivityFragment() {
        // Required empty public constructor
    }

    public void setObservables(ObservableComputeAltCore core, ObservableDecoderUnit decoder,
                               ObservableReadPort<ROM> iCache, ObservableMemoryPort dataMem,
                               ControlUnitAltCore controlUnitData){
        mainCore = core;
        decoderUnit = decoder;
        instructionCache = iCache;
        dataMemory = dataMem;
        controlUnit = controlUnitData;
        stageCount = controlUnitData.fsmStageCount();

        observablesReady = true;
        attemptInit();
    }

    @Override
    protected void initViews(View mainView) {
        tempDisplayView = (TextView) mainView.findViewById(R.id.remainderText);
        tempDisplayView.setText("Remainder = 0");
    }

    protected void notifyStepActionListener(){
        ++actionCount;

        int remainder = actionCount % stageCount;
        if(remainder == 0) {
            mListener.onStepActionCompleted();
        }
        tempDisplayView.setText("Remainder = " + remainder);
    }

    protected void notifyResetActionListener() {
        actionCount = 0;
        mListener.onResetCompleted();
        tempDisplayView.setText("Remainder = 0");
    }
}
