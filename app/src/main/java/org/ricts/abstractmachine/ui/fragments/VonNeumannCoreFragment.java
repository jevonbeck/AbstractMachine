package org.ricts.abstractmachine.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.ui.compute.ComputeCoreView;
import org.ricts.abstractmachine.ui.compute.ControlUnitView;
import org.ricts.abstractmachine.ui.network.MemoryPortMultiplexerView;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;
import org.ricts.abstractmachine.ui.storage.ReadPortView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepActionListener} interface
 * to handle interaction events.
 * Use the {@link VonNeumannCoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VonNeumannCoreFragment extends VonNeumannActivityFragment {
    private ControlUnitView cuView;
    private MemoryPortMultiplexerView muxView;

    private enum MuxInputIds{
        INS_MEM, DATA_MEM
    }

    @Override
    protected void initView(View mainView){
        mainView.setId(R.id.VonNeumannCoreFragment_main_view);

        muxView = (MemoryPortMultiplexerView) mainView.findViewById(R.id.mux);
        muxView.initMux(1, mainMemory.dataWidth(), mainMemory.addressWidth());
        muxView.setOutputSource(mainMemory);

        View [] temp = muxView.getInputs();
        MemoryPortView muxInputs[] =  new MemoryPortView[temp.length];
        for(int x=0; x != muxInputs.length; ++x){
            muxInputs[x] = (MemoryPortView) temp[x];
        }

        MemoryPortView instructionCache = muxInputs[MuxInputIds.INS_MEM.ordinal()];
        MemoryPortView dataMemory = muxInputs[MuxInputIds.DATA_MEM.ordinal()];

        ComputeCoreView coreView = (ComputeCoreView) mainView.findViewById(R.id.core);
        coreView.setComputeCore(mainCore);

        cuView = (ControlUnitView) mainView.findViewById(R.id.control_unit);
        cuView.initCU(coreView, instructionCache, dataMemory);

        coreView.setUpdatePcResponder(new ComputeCoreView.PinsView.UpdateResponder() {
            @Override
            public void onUpdatePcCompleted() {
                cuView.updatePcView();
            }
        });

        instructionCache.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                cuView.updateIrView();
            }

            @Override
            public void onReadStart() {
                cuView.updatePcView();
            }
        });
    }

    @Override
    public int nextActionTransitionTime() {
        return cuView.nextActionDuration();
    }

    @Override
    public void triggerNextAction() {
        if(cuView.isAboutToExecute()){
            muxView.setSelection(MuxInputIds.DATA_MEM.ordinal());
        }
        else {
            muxView.setSelection(MuxInputIds.INS_MEM.ordinal());
        }

        cuView.performNextAction(); // perform action for 'currentState' and go to next state
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
    public static VonNeumannCoreFragment newInstance(ComputeCore core, RAM memData) {
        VonNeumannCoreFragment fragment = new VonNeumannCoreFragment();
        fragment.init(core, memData, R.layout.fragment_von_neumann_core);
        return fragment;
    }
}
