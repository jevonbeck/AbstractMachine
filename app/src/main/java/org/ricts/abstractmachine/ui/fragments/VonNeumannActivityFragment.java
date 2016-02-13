package org.ricts.abstractmachine.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepActionListener} interface
 * to handle interaction events.
 */
public abstract class VonNeumannActivityFragment extends Fragment {
    protected ObservableComputeCore mainCore;
    protected ObservableRAM mainMemory;
    protected ObservableControlUnit controlUnit;

    private StepActionListener mListener;

    private int layoutId;

    public VonNeumannActivityFragment() {
        // Required empty public constructor
    }

    protected abstract void initView(View mainView);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(layoutId, container, false);
        initView(rootView);
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onStepActionCompleted();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StepActionListener) {
            mListener = (StepActionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StepActionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface StepActionListener {
        void onStepActionCompleted();
    }

    public void init(ObservableComputeCore core, ObservableRAM memData,
                     ObservableControlUnit fsmData, int id){
        mainCore = core;
        mainMemory = memData;
        controlUnit = fsmData;
        layoutId = id;
    }
}
