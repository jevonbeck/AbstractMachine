package org.ricts.abstractmachine.ui.fragment;

import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableCuFSM;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableDefaultValueSource;
import org.ricts.abstractmachine.components.observable.ObservableFetchCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableReadPort;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.ui.compute.CpuAltCoreView;
import org.ricts.abstractmachine.ui.compute.InspectActionResponder;
import org.ricts.abstractmachine.ui.storage.RamView;
import org.ricts.abstractmachine.ui.storage.RomView;

/**
 * Created by Jevon on 13/08/2016.
 */
public class HarvardAltSystemFragment extends HarvardAltActivityFragment {
    private RomView instructionCacheView;
    private RamView dataMemoryView;
    private CpuAltCoreView cpuView;

    @Override
    protected void initViews(View mainView) {
        super.initViews(mainView);
        instructionCacheView = (RomView) mainView.findViewById(R.id.instructionCache);
        dataMemoryView = (RamView) mainView.findViewById(R.id.dataMemory);

        cpuView = (CpuAltCoreView) mainView.findViewById(R.id.cpuView);
        cpuView.setActionResponder(new InspectActionResponder() {
            @Override
            public void onStepAnimationEnd() {
                notifyStepActionListener(); // let Activity know that animations completed
            }

            @Override
            public void onResetAnimationEnd() {
                notifyResetActionListener();
            }
        });
    }

    @Override
    protected void bindObservablesToViews() {
        ObservableCuFSM fsm = controlUnit.getMainFSM();
        ObservableFetchCore regCore = controlUnit.getRegCore();
        ObservableDefaultValueSource irDefaultValueSource = controlUnit.getIrDefaultValueSource();

        /** Initialise Views **/
        instructionCacheView.setDataSource(instructionCache.getType());
        dataMemoryView.setDataSource((RAM) dataMemory.getType());
        cpuView.initCpu(fsm, regCore, mainCore.getALU(), instructionCacheView, dataMemoryView);

        /** Add observers to observables **/
        instructionCache.addObserver(instructionCacheView);
        dataMemory.addObserver(dataMemoryView);
        fsm.addObserver(cpuView);
        regCore.addObserver(cpuView);
        irDefaultValueSource.addObserver(cpuView);
        mainCore.addObserver(cpuView);
        decoderUnit.addObserver(cpuView);
    }

    @Override
    protected void handleUserVisibility(boolean visible) {
        instructionCacheView.setAnimatePins(visible);
        dataMemoryView.setAnimatePins(visible);
        cpuView.setViewVisibility(visible);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_harvard_alt_system;
    }

    public HarvardAltSystemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mainCore Processing core architecture
     * @param decoderUnit decoder
     * @param instructionCache instruction memory
     * @param dataMemory data memory
     * @param cu Control Unit
     * @return A new instance of fragment HarvardSystemFragment.
     */
    public static HarvardAltSystemFragment newInstance(ObservableComputeAltCore mainCore, ObservableDecoderUnit decoderUnit,
                                                       ObservableReadPort<ROM> instructionCache,
                                                       ObservableMemoryPort dataMemory, ControlUnitAltCore cu) {
        HarvardAltSystemFragment fragment = new HarvardAltSystemFragment();
        fragment.setObservables(mainCore, decoderUnit, instructionCache, dataMemory, cu);
        return fragment;
    }
}
