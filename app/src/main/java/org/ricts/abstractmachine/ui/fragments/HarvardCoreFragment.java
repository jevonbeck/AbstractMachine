package org.ricts.abstractmachine.ui.fragments;

import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.observables.ObservableROM;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.ui.compute.ComputeCoreView;
import org.ricts.abstractmachine.ui.compute.ControlUnitView;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;
import org.ricts.abstractmachine.ui.storage.ReadPortView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 13/08/2016.
 */
public class HarvardCoreFragment extends HarvardActivityFragment implements Observer {
    private ControlUnitView cuView;
    private ComputeCoreView coreView;

    private ReadPortView instructionCacheView;
    private MemoryPortView dataMemoryView;
    private ObservableRAM dataMemObservable;
    private Object dataMemObservableObject;
    private boolean animatePins;

    @Override
    protected void initViews(View mainView) {
        coreView = (ComputeCoreView) mainView.findViewById(R.id.core);
        dataMemoryView = (MemoryPortView) mainView.findViewById(R.id.dataMemory);
        instructionCacheView = (ReadPortView) mainView.findViewById(R.id.instructionCache);

        coreView.setMemoryCommandResponder(new ComputeCoreView.MemoryCommandResponder() {
            @Override
            public void onMemoryCommandIssued() {
                if(animatePins) {
                    dataMemoryView.update(dataMemObservable, dataMemObservableObject);
                }
            }
        });
        coreView.setActionResponder(new ComputeCoreView.StepActionResponder() {
            @Override
            public void onAnimationEnd() {
                mListener.onStepActionCompleted();
            }
        });

        dataMemoryView.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                coreView.sendDoneCommand();
            }

            @Override
            public void onReadStart() {

            }
        });
        dataMemoryView.setWriteResponder(new MemoryPortView.WriteResponder() {
            @Override
            public void onWriteFinished() {
                coreView.sendDoneCommand();
            }

            @Override
            public void onWriteStart() {

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
    protected void bindObservablesToViews() {
        /** Initialise Views **/
        cuView.initCU(controlUnit.getType(), coreView, instructionCacheView);

        /** Add observers to observables **/
        mainCore.addObserver(coreView);
        controlUnit.addObserver(cuView);
        dataMemory.addObserver(this);
        instructionCache.addObserver(this);
    }

    @Override
    protected void handleUserVisibility(boolean visible) {
        boolean fragmentNotVisible = !visible;

        animatePins = visible;
        cuView.setUpdateImmediately(fragmentNotVisible);
        coreView.setUpdateImmediately(fragmentNotVisible);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_harvard_core;
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableRAM){
            dataMemObservable = (ObservableRAM) observable;
            dataMemObservableObject = o;
        }
        else if(observable instanceof ObservableROM) {
            if(animatePins) {
                instructionCacheView.update(observable, o);
            }
        }
    }

    public HarvardCoreFragment() {
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
     * @return A new instance of fragment HarvardCoreFragment.
     */
    public static HarvardCoreFragment newInstance(ObservableComputeCore mainCore,
                                                    ObservableROM<ROM> instructionCache,
                                                    ObservableRAM dataMemory, ObservableControlUnit cu) {
        HarvardCoreFragment fragment = new HarvardCoreFragment();
        fragment.setObservables(mainCore, instructionCache, dataMemory, cu);
        return fragment;
    }
}
