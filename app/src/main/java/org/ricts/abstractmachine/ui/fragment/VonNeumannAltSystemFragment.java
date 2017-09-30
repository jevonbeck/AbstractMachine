package org.ricts.abstractmachine.ui.fragment;

import android.support.v4.app.Fragment;
import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeCore;
import org.ricts.abstractmachine.components.observable.ObservableCuFSM;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableDefaultValueSource;
import org.ricts.abstractmachine.components.observable.ObservableFetchCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.ui.compute.CpuAltCoreView;
import org.ricts.abstractmachine.ui.compute.CpuCoreView;
import org.ricts.abstractmachine.ui.compute.InspectActionResponder;
import org.ricts.abstractmachine.ui.storage.RamView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InspectActionListener} interface
 * to handle interaction events.
 * Use the {@link VonNeumannAltSystemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VonNeumannAltSystemFragment extends VonNeumannAltActivityFragment {
    private RamView memory;
    private CpuAltCoreView cpu;

    @Override
    protected void initViews(View mainView){
        memory = (RamView) mainView.findViewById(R.id.memory);

        cpu = (CpuAltCoreView) mainView.findViewById(R.id.cpuView);
        cpu.setActionResponder(new InspectActionResponder() {
            @Override
            public void onStepAnimationEnd() {
                mListener.onStepActionCompleted(); // let Activity know that animations completed
            }

            @Override
            public void onResetAnimationEnd() {
                mListener.onResetCompleted();
            }
        });
    }

    @Override
    protected void bindObservablesToViews(){
        ObservableCuFSM fsm = controlUnit.getMainFSM();
        ObservableFetchCore regCore = controlUnit.getRegCore();
        ObservableDefaultValueSource irDefaultValueSource = controlUnit.getIrDefaultValueSource();

        /** Initialise Views **/
        memory.setDataSource((RAM) mainMemory.getType());
        cpu.initCpu(fsm, regCore, mainCore.getALU(), memory, memory);

        /** Add observers to observables **/
        mainMemory.addObserver(memory);
        fsm.addObserver(cpu);
        regCore.addObserver(cpu);
        irDefaultValueSource.addObserver(cpu);
        mainCore.addObserver(cpu);
        decoderUnit.addObserver(cpu);
    }

    @Override
    protected void handleUserVisibility(boolean visible) {
        memory.setAnimatePins(visible);
        cpu.setUpdateIrImmediately(!visible);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_von_neumann_alt_system;
    }

    public VonNeumannAltSystemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param core Processing core architecture
     * @param memData System memory
     * @param cu Control Unit
     * @return A new instance of fragment VonNeumannSystemFragment.
     */
    public static VonNeumannAltSystemFragment newInstance(ObservableComputeAltCore core, ObservableDecoderUnit decoderUnit,
                                                          ObservableMemoryPort memData, ControlUnitAltCore cu) {
        VonNeumannAltSystemFragment fragment = new VonNeumannAltSystemFragment();
        fragment.setObservables(core, decoderUnit, memData, cu);
        return fragment;
    }
}
