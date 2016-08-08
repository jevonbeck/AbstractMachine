package org.ricts.abstractmachine.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.ui.compute.CpuCoreView;
import org.ricts.abstractmachine.ui.storage.RamView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepActionListener} interface
 * to handle interaction events.
 * Use the {@link VonNeumannSystemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VonNeumannSystemFragment extends VonNeumannActivityFragment {
    private RamView memory;
    private CpuCoreView cpu;

    @Override
    protected void initViews(View mainView){
        memory = (RamView) mainView.findViewById(R.id.memory);

        cpu = (CpuCoreView) mainView.findViewById(R.id.cpuView);
        cpu.setActionResponder(new CpuCoreView.StepActionResponder() {
            @Override
            public void onAnimationEnd() {
                mListener.onStepActionCompleted(); // let Activity know that animations completed
            }
        });
    }

    @Override
    protected void bindObservablesToViews(){
        /** Initialise Views **/
        memory.setDataSource(mainMemory.getType());
        cpu.initCpu(controlUnit.getType(), memory, memory);

        /** Add observers to observables **/
        mainMemory.addObserver(memory);
        controlUnit.addObserver(cpu);
        mainCore.addObserver(cpu);
    }

    @Override
    protected void handleUserVisibility(boolean visible) {
        memory.setAnimatePins(visible);
        cpu.setUpdateIrImmediately(!visible);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_von_neumann_system;
    }

    public VonNeumannSystemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param core Processing core architecture
     * @param memData System memory
     * @return A new instance of fragment VonNeumannSystemFragment.
     */
    public static VonNeumannSystemFragment newInstance(ObservableComputeCore core, ObservableRAM memData,
                                                       ObservableControlUnit cu) {
        VonNeumannSystemFragment fragment = new VonNeumannSystemFragment();
        fragment.setObservables(core, memData, cu);
        return fragment;
    }
}
