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
    private TextView muxSelectView;

    private enum MuxInputIds{
        INS_MEM, DATA_MEM;

        public String getOrdinalText(){
            return "0x" + ordinal();
        }
    }

    @Override
    protected void initView(View mainView){
        mainView.setId(R.id.VonNeumannCoreFragment_main_view);

        muxSelectView = (TextView) mainView.findViewById(R.id.muxSelect);
        muxSelectView.setText(MuxInputIds.INS_MEM.getOrdinalText());

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

        cuView = (ControlUnitView) mainView.findViewById(R.id.control_unit);
        cuView.initCU(controlUnit.getType(), coreView, instructionCache);

        /** Add observers to observables **/
        mainCore.addObserver(coreView);
        mainMemory.addObserver(this);
        controlUnit.addObserver(this);
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
            if(controlUnit.isAboutToFetch()){
                muxView.setUpdateImmediately(true);
                muxView.setSelection(MuxInputIds.INS_MEM.ordinal());
            }
            else{
                muxView.setUpdateImmediately(false);
                muxView.setSelection(MuxInputIds.DATA_MEM.ordinal());
            }
            muxView.update(observable, o);
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
        fragment.init(core, memData, fsmData, R.layout.fragment_von_neumann_core);
        return fragment;
    }
}
