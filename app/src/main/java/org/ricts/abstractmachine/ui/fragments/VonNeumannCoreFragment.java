package org.ricts.abstractmachine.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiplexer;
import org.ricts.abstractmachine.ui.compute.ComputeCoreView;
import org.ricts.abstractmachine.ui.compute.ControlUnitView;
import org.ricts.abstractmachine.ui.compute.InspectActionResponder;
import org.ricts.abstractmachine.ui.network.MemoryPortMultiplexerView;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;
import org.ricts.abstractmachine.ui.storage.ReadPortView;

import java.util.Observable;
import java.util.Observer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InspectActionListener} interface
 * to handle interaction events.
 * Use the {@link VonNeumannCoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VonNeumannCoreFragment extends VonNeumannActivityFragment {
    private MemoryPortMultiplexerView muxView;
    private ControlUnitView cuView;
    private ComputeCoreView coreView;
    private TextView muxSelectView;

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
        muxView.setTargetMemoryPort(mainMemory.getType());

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

        MemoryPortView dataMemory = (MemoryPortView) (muxView.getInputs())[MuxInputIds.DATA_MEM.ordinal()];
        dataMemory.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                coreView.sendDoneCommand();
            }

            @Override
            public void onReadStart() {

            }
        });
        dataMemory = (MemoryPortView) muxView.getOutput();
        dataMemory.setWriteResponder(new MemoryPortView.WriteResponder() {
            @Override
            public void onWriteFinished() {
                coreView.sendDoneCommand();
            }

            @Override
            public void onWriteStart() {

            }
        });

        cuView = (ControlUnitView) mainView.findViewById(R.id.control_unit);
        cuView.setActionResponder(new InspectActionResponder() {
            @Override
            public void onStepAnimationEnd() {
                mListener.onStepActionCompleted();
            }

            @Override
            public void onResetAnimationEnd() {
                mListener.onResetCompleted();
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
        controlUnit.addObserver(cuView);
        controlUnit.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                ControlUnit cu = (ControlUnit) ((ObservableControlUnit) observable).getType();
                muxView.setUpdateImmediately(cu.isInFetchState());

                if(cu.isInExecuteState() || (o != null &&  o instanceof Boolean)){
                    muxSelectView.setText(MuxInputIds.INS_MEM.getOrdinalText());
                }
                else{
                    muxSelectView.setText(MuxInputIds.DATA_MEM.getOrdinalText());
                }
            }
        });
        muxSelector.addObserver(muxView);
        muxInputPorts.addObserver(muxView);
    }

    @Override
    protected void handleUserVisibility(boolean visible) {
        boolean fragmentNotVisible = !visible;

        muxView.showPinAnimations(visible);
        cuView.setUpdateImmediately(fragmentNotVisible);
        coreView.setUpdateImmediately(fragmentNotVisible);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_von_neumann_core;
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
    public static VonNeumannCoreFragment newInstance(ObservableComputeCore core, ObservableMemoryPort memData,
                                                     ObservableControlUnit fsmData, ObservableMultiplexer muxSelect,
                                                     ObservableMultiMemoryPort muxPorts) {
        VonNeumannCoreFragment fragment = new VonNeumannCoreFragment();
        fragment.setObservables(core, memData, fsmData, muxSelect, muxPorts);
        return fragment;
    }
}
