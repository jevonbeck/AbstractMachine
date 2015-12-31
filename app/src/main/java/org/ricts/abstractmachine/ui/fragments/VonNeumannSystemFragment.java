package org.ricts.abstractmachine.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.storage.RAM;
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
    private CpuCoreView cpu;

    @Override
    protected void initView(View mainView){
        mainView.setId(R.id.VonNeumannSystemFragment_main_view);

        RamView memory = (RamView) mainView.findViewById(R.id.memory);
        memory.setDataSource(mainMemory);

        cpu = (CpuCoreView) mainView.findViewById(R.id.cpuView);
        cpu.initCpu(mainCore, memory);
    }

    @Override
    public int nextActionTransitionTime() {
        return cpu.nextActionTransitionTime();
    }

    @Override
    public void triggerNextAction() {
        cpu.triggerNextAction(); // perform action for 'currentState' and go to next state
    }

    @Override
    public void setStartExecFrom(int currentPC){
        cpu.setStartExecFrom(currentPC);
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
    public static VonNeumannSystemFragment newInstance(ComputeCore core, RAM memData) {
        VonNeumannSystemFragment fragment = new VonNeumannSystemFragment();
        fragment.init(core, memData, R.layout.fragment_von_neumann_system);
        return fragment;
    }
}
