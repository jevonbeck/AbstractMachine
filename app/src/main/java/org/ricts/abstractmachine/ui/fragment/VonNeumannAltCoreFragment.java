package org.ricts.abstractmachine.ui.fragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.core.UniMemoryCpuCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableCuFSM;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableDefaultValueSource;
import org.ricts.abstractmachine.components.observable.ObservableFetchCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiplexer;
import org.ricts.abstractmachine.ui.compute.ComputeAltCoreView;
import org.ricts.abstractmachine.ui.compute.ControlUnitAltView;
import org.ricts.abstractmachine.ui.compute.ControlUnitInterfaceView;
import org.ricts.abstractmachine.ui.compute.DecoderUnitView;
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
 * Use the {@link VonNeumannAltCoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VonNeumannAltCoreFragment extends VonNeumannAltActivityFragment {
    private static final int DATA_MEM_ID = UniMemoryCpuCore.SerializerInputId.DATA_MEM.ordinal();
    private static final int INS_MEM_ID = UniMemoryCpuCore.SerializerInputId.INSTRUCTION_MEM.ordinal();

    private MemoryPortMultiplexerView muxView;
    private ControlUnitAltView cuView;
    private ComputeAltCoreView coreView;
    private DecoderUnitView decoderView;
    private ControlUnitInterfaceView cuInterfaceView;
    private TextView muxSelectView;

    @Override
    protected void initViews(View mainView){
        muxSelectView = (TextView) mainView.findViewById(R.id.muxSelect);
        muxSelectView.setText(muxSelector.getSelectionText());

        muxView = (MemoryPortMultiplexerView) mainView.findViewById(R.id.mux);
        muxView.setSelectWidth(1);
        muxView.setTargetMemoryPort(mainMemory.getType());

        cuView = (ControlUnitAltView) mainView.findViewById(R.id.control_unit);
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

        decoderView = (DecoderUnitView) mainView.findViewById(R.id.decoder);
        decoderView.setActionResponder(new DecoderUnitView.StepActionResponder() {
            @Override
            public void onAnimationEnd() {
                mListener.onStepActionCompleted();
            }
        });

        cuInterfaceView = (ControlUnitInterfaceView) mainView.findViewById(R.id.cuInterface);
        cuInterfaceView.setUpdateResponder(new ControlUnitInterfaceView.UpdateResponder() {
            @Override
            public void onUpdateFetchUnitCompleted() {
                mListener.onStepActionCompleted();
            }
        });
        cuInterfaceView.setCommandResponder(new ControlUnitInterfaceView.CommandOnlyResponder() {
            @Override
            public void onCommandCompleted() {
                mListener.onStepActionCompleted();
            }
        });

        coreView = (ComputeAltCoreView) mainView.findViewById(R.id.core);
        coreView.setControlUnitCommandInterface(cuInterfaceView);
        coreView.setMemoryCommandResponder(new ComputeAltCoreView.MemoryCommandResponder() {
            @Override
            public void onMemoryCommandIssued() {
                muxView.animatePins();
            }
        });

        MemoryPortView dataMemory = (MemoryPortView) (muxView.getInputs())[DATA_MEM_ID];
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
    }

    @Override
    protected void bindObservablesToViews(){
        ObservableCuFSM fsm = controlUnit.getMainFSM();
        ObservableFetchCore regCore = controlUnit.getRegCore();
        ObservableDefaultValueSource irDefaultValueSource = controlUnit.getIrDefaultValueSource();

        /** Initialise Views **/
        MemoryPortView instructionCache = (MemoryPortView) (muxView.getInputs())[INS_MEM_ID];
        cuView.initCU(fsm, regCore, decoderView, cuInterfaceView, instructionCache);

        /** Add observers to observables **/
        mainCore.addObserver(coreView);
        decoderUnit.addObserver(decoderView);
        fsm.addObserver(cuView);
        regCore.addObserver(cuView);
        irDefaultValueSource.addObserver(cuView);
        muxSelector.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                ObservableMultiplexer mux = (ObservableMultiplexer) observable;

                muxView.setUpdateImmediately(mux.getSelection() == INS_MEM_ID);
                muxSelectView.setText(mux.getSelectionText());
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
        decoderView.setUpdateImmediately(fragmentNotVisible);
        coreView.setUpdateImmediately(fragmentNotVisible);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_von_neumann_alt_core;
    }

    public VonNeumannAltCoreFragment() {
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
    public static VonNeumannAltCoreFragment newInstance(ObservableComputeAltCore core, ObservableDecoderUnit decoderUnit,
                                                        ObservableMemoryPort memData, ControlUnitAltCore controlUnit,
                                                        ObservableMultiplexer muxSelect, ObservableMultiMemoryPort muxPorts) {
        VonNeumannAltCoreFragment fragment = new VonNeumannAltCoreFragment();
        fragment.setObservables(core, decoderUnit, memData, controlUnit, muxSelect, muxPorts);
        return fragment;
    }
}
