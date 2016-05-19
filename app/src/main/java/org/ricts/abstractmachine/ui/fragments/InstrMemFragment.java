package org.ricts.abstractmachine.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.utils.wizard.WizardFragment;

/**
 * Created by Jevon on 18/05/2016.
 */
public class InstrMemFragment extends WizardFragment {
    @Override
    public void restorePageData(Bundle bundle) {

    }

    @Override
    public void savePageData(Bundle bundle) {

    }

    public InstrMemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cpu_basics, container, false);

        return rootView;
    }
}
