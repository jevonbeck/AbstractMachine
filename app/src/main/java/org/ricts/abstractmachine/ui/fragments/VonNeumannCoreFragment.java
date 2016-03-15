package org.ricts.abstractmachine.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.ui.compute.ComputeCoreView;
import org.ricts.abstractmachine.ui.compute.ControlUnitView;
import org.ricts.abstractmachine.ui.network.MemoryPortMultiplexerView;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;
import org.ricts.abstractmachine.ui.storage.ReadPortView;

import java.util.Observable;
import java.util.Observer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepActionListener} interface
 * to handle interaction events.
 * Use the {@link VonNeumannCoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VonNeumannCoreFragment extends VonNeumannActivityFragment implements Observer{
    private MemoryPortMultiplexerView muxView;
    private ControlUnitView cuView;
    private ComputeCoreView coreView;
    private TextView muxSelectView;

    private boolean updateMuxView;

    private enum MuxInputIds{
        INS_MEM, DATA_MEM;

        public String getOrdinalText(){
            return "0x" + ordinal();
        }
    }

    @Override
    protected void initViews(View mainView){
        muxSelectView = (TextView) mainView.findViewById(R.id.muxSelect);
        muxSelectView.setText(MuxInputIds.INS_MEM.getOrdinalText());

        muxView = (MemoryPortMultiplexerView) mainView.findViewById(R.id.mux);
        muxView.setSelectWidth(1);

        MemoryPortView dataMemory = (MemoryPortView) (muxView.getInputs())[MuxInputIds.DATA_MEM.ordinal()];
        dataMemory.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                mListener.onStepActionCompleted();
            }

            @Override
            public void onReadStart() {

            }
        });
        dataMemory = (MemoryPortView) muxView.getOutput();
        dataMemory.setWriteResponder(new MemoryPortView.WriteResponder() {
            @Override
            public void onWriteFinished() {
                mListener.onStepActionCompleted();
            }

            @Override
            public void onWriteStart() {

            }
        });

        coreView = (ComputeCoreView) mainView.findViewById(R.id.core);
        coreView.setMemoryCommandResponder(new ComputeCoreView.MemoryCommandResponder() {
            @Override
            public void onMemoryCommandIssued() {
                muxView.animatePins();
            }
        });
        coreView.setActionResponder(new ComputeCoreView.StepActionResponder() {
            @Override
            public void onAnimationEnd() {
                mListener.onStepActionCompleted();
            }
        });

        cuView = (ControlUnitView) mainView.findViewById(R.id.control_unit);
        cuView.setActionResponder(new ControlUnitView.StepActionResponder() {
            @Override
            public void onAnimationEnd() {
                mListener.onStepActionCompleted();
            }
        });
    }

    @Override
    protected void bindObservablesToViews(){
        /** Initialise Views **/
        MemoryPortView instructionCache = (MemoryPortView) (muxView.getInputs())[MuxInputIds.INS_MEM.ordinal()];
        cuView.initCU(controlUnit.getType(), coreView, instructionCache);

        /** Add observers to observables **/
        mainCore.addObserver(coreView);
        mainMemory.addObserver(this);
        controlUnit.addObserver(this);
    }

    @Override
    protected void handleUserVisibility(boolean visible) {
        boolean fragmentNotVisible = !visible;

        updateMuxView = visible;
        cuView.setUpdateImmediately(fragmentNotVisible);
        coreView.setUpdateImmediately(fragmentNotVisible);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_von_neumann_core;
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableControlUnit){
            ControlUnit cu = ((ObservableControlUnit) observable).getType();

            if(cu.isAboutToExecute()){
                muxSelectView.setText(MuxInputIds.INS_MEM.getOrdinalText());
            }
            else{
                muxSelectView.setText(MuxInputIds.DATA_MEM.getOrdinalText());
            }

            cuView.update(observable, o);
        }
        else if(observable instanceof ObservableRAM){
            if(updateMuxView) {
                if (controlUnit.isAboutToFetch()) {
                    muxView.setUpdateImmediately(true);
                    muxView.setSelection(MuxInputIds.INS_MEM.ordinal());
                } else {
                    muxView.setUpdateImmediately(false);
                    muxView.setSelection(MuxInputIds.DATA_MEM.ordinal());
                }

                muxView.update(observable, o);
            }
        }
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
        fragment.setObservables(core, memData, fsmData);
        return fragment;
    }
}
