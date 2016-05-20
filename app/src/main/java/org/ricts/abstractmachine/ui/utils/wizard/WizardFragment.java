package org.ricts.abstractmachine.ui.utils.wizard;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class WizardFragment extends Fragment {
    public abstract void restorePageData(Bundle bundle);
    public abstract void savePageData(Bundle bundle);

    public interface DataSource {
        Bundle getWizardData();
    }

    protected DataSource dataSource;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DataSource) {
            dataSource = (DataSource) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DataSource");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dataSource = null;
    }
}
