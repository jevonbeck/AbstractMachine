package org.ricts.abstractmachine.ui.fragment;

import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeCore;
import org.ricts.abstractmachine.components.observable.ObservableCuFSM;
import org.ricts.abstractmachine.components.observable.ObservableCuRegCore;
import org.ricts.abstractmachine.components.observable.ObservableDefaultValueSource;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableReadPort;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.ui.compute.CpuCoreView;
import org.ricts.abstractmachine.ui.compute.InspectActionResponder;
import org.ricts.abstractmachine.ui.storage.RamView;
import org.ricts.abstractmachine.ui.storage.RomView;

/**
 * Created by Jevon on 13/08/2016.
 */
public class HarvardSystemFragment extends HarvardActivityFragment {
    private RomView instructionCacheView;
    private RamView dataMemoryView;
    private CpuCoreView cpuView;

    @Override
    protected void initViews(View mainView) {
        instructionCacheView = (RomView) mainView.findViewById(R.id.instructionCache);
        dataMemoryView = (RamView) mainView.findViewById(R.id.dataMemory);

        cpuView = (CpuCoreView) mainView.findViewById(R.id.cpuView);
        cpuView.setActionResponder(new InspectActionResponder() {
            @Override
            public void onStepAnimationEnd() {
                notifyStepActionListener(); // let Activity know that animations completed
            }

            @Override
            public void onResetAnimationEnd() {
                mListener.onResetCompleted();
            }
        });
    }

    @Override
    protected void bindObservablesToViews() {
        ObservableCuFSM fsm = controlUnit.getMainFSM();
        ObservableCuRegCore regCore = controlUnit.getRegCore();
        ObservableDefaultValueSource irDefaultValueSource = controlUnit.getIrDefaultValueSource();

        /** Initialise Views **/
        instructionCacheView.setDataSource(instructionCache.getType());
        dataMemoryView.setDataSource((RAM) dataMemory.getType());
        cpuView.initCpu(fsm, regCore, instructionCacheView, dataMemoryView);

        /** Add observers to observables **/
        instructionCache.addObserver(instructionCacheView);
        dataMemory.addObserver(dataMemoryView);
        fsm.addObserver(cpuView);
        regCore.addObserver(cpuView);
        irDefaultValueSource.addObserver(cpuView);
        mainCore.addObserver(cpuView);
    }

    @Override
    protected void handleUserVisibility(boolean visible) {
        instructionCacheView.setAnimatePins(visible);
        dataMemoryView.setAnimatePins(visible);
        cpuView.setUpdateIrImmediately(!visible);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_harvard_system;
    }

    public HarvardSystemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mainCore Processing core architecture
     * @param instructionCache instruction memory
     * @param dataMemory data memory
     * @param cu Control Unit
     * @return A new instance of fragment HarvardSystemFragment.
     */
    public static HarvardSystemFragment newInstance(ObservableComputeCore mainCore,
                                                    ObservableReadPort<ROM> instructionCache,
                                                    ObservableMemoryPort dataMemory, ControlUnitCore cu) {
        HarvardSystemFragment fragment = new HarvardSystemFragment();
        fragment.setObservables(mainCore, instructionCache, dataMemory, cu);
        return fragment;
    }
}
