package org.ricts.abstractmachine.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InspectActionListener} interface
 * to handle interaction events.
 */
public abstract class InspectFragment extends Fragment {
    protected InspectActionListener mListener;

    protected boolean viewsReady = false, observablesReady = false;

    public InspectFragment() {
        // Required empty public constructor
    }

    protected abstract void initViews(View mainView);
    protected abstract void bindObservablesToViews();
    protected abstract void handleUserVisibility(boolean visible);
    protected abstract int getLayoutId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(getLayoutId(), container, false);
        initViews(rootView);

        viewsReady = true;
        attemptInit();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InspectActionListener) {
            mListener = (InspectActionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InspectActionListener");
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
    public interface InspectActionListener {
        void onStepActionCompleted();
        void onResetCompleted();
    }

    public void setUserVisibility(boolean visibility){
        handleUserVisibility(visibility);
    }

    protected void attemptInit(){
        if(viewsReady && observablesReady){
            bindObservablesToViews();
        }
    }
}
