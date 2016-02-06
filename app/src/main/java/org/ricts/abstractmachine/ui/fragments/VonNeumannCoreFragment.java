package org.ricts.abstractmachine.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.ui.compute.ComputeCoreView;
import org.ricts.abstractmachine.ui.compute.ControlUnitView;
import org.ricts.abstractmachine.ui.network.MemoryPortMultiplexerView;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepActionListener} interface
 * to handle interaction events.
 * Use the {@link VonNeumannCoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VonNeumannCoreFragment extends VonNeumannActivityFragment {
    private MemoryPortMultiplexerView muxView;
    private TextView muxSelectView;

    private enum MuxInputIds{
        INS_MEM, DATA_MEM
    }

    @Override
    protected void initView(View mainView){
        mainView.setId(R.id.VonNeumannCoreFragment_main_view);

        muxSelectView = (TextView) mainView.findViewById(R.id.muxSelect);
        muxSelectView.setText("0x0");

        muxView = (MemoryPortMultiplexerView) mainView.findViewById(R.id.mux);
        muxView.setSelectWidth(1);

        View [] temp = muxView.getInputs();
        MemoryPortView muxInputs[] =  new MemoryPortView[temp.length];
        for(int x=0; x != muxInputs.length; ++x){
            muxInputs[x] = (MemoryPortView) temp[x];
        }

        MemoryPortView instructionCache = muxInputs[MuxInputIds.INS_MEM.ordinal()];
        //MemoryPortView dataMemory = muxInputs[MuxInputIds.DATA_MEM.ordinal()];

        ComputeCoreView coreView = (ComputeCoreView) mainView.findViewById(R.id.core);
        coreView.setMemoryCommandResponder(new ComputeCoreView.MemoryCommandResponder() {
            @Override
            public void onMemoryCommandIssued() {
                muxView.animatePins();
            }
        });

        ControlUnitView cuView = (ControlUnitView) mainView.findViewById(R.id.control_unit);
        cuView.initCU(controlUnit.getType(), coreView, instructionCache);

        /** Add observers to observables **/
        mainCore.addObserver(coreView);
        mainMemory.addObserver(muxView);
        controlUnit.addObserver(cuView);
    }

    @Override
    public int nextActionTransitionTime() {
        return controlUnit.nextActionDuration();
    }

    @Override
    public void triggerNextAction() {
        if(controlUnit.isAboutToExecute()){
            muxView.setUpdateImmediately(false);
            muxView.setSelection(MuxInputIds.DATA_MEM.ordinal());
            muxSelectView.setText("0x1");
        }
        else {
            muxView.setUpdateImmediately(true);
            muxView.setSelection(MuxInputIds.INS_MEM.ordinal());
            muxSelectView.setText("0x0");
        }

        controlUnit.performNextAction(); // perform action for 'currentState' and go to next state
    }

    public VonNeumannCoreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param core Processing core architecture
     * @param memData System memory
     * @return A new instance of fragment VonNeumannCoreFragment.
     */
    public static VonNeumannCoreFragment newInstance(ObservableComputeCore core, ObservableRAM memData,
                                                     ObservableControlUnit fsmData) {
        VonNeumannCoreFragment fragment = new VonNeumannCoreFragment();
        fragment.init(core, memData, fsmData, R.layout.fragment_von_neumann_core);
        return fragment;
    }
}
