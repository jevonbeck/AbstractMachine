package org.ricts.abstractmachine.ui.utils.wizard;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class WizardFragment extends Fragment {
    public abstract void restorePageData(Bundle bundle);
    public abstract void savePageData(Bundle bundle);

    public WizardFragment() {
        // Required empty public constructor
    }
}
