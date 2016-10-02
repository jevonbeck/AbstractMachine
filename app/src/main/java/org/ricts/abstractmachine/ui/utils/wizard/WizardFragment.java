package org.ricts.abstractmachine.ui.utils.wizard;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class WizardFragment extends Fragment {
    public abstract void restorePageData(Bundle bundle);
    public abstract void savePageData(Bundle bundle);

    /** N.B: the first wizard page does NOT need to implement this function.
     * This function is primarily to update the next wizard page when there are
     * dependencies between the current and next wizard pages. */
    public abstract void updatePage(Bundle bundle);

    public interface DataSource {
        Bundle getWizardData();
    }

    private DataSource dataSource;

    protected Bundle getWizardData(){
        return (dataSource != null) ? dataSource.getWizardData() : null;
    }

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
