package org.ricts.abstractmachine.ui.fragment;

import android.content.Intent;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.activity.DataMemoryDialogActivity;

/**
 * Created by Jevon on 02/07/2016.
 */
public class DataMemFragment extends MemFragment {
    @Override
    protected int titleStringResource() {
        return R.string.data_memory_title;
    }

    @Override
    protected MemoryType memoryType() {
        return MemoryType.DATA;
    }

    @Override
    protected Intent getDialogActivityIntent() {
        return new Intent(getContext(), DataMemoryDialogActivity.class);
    }
}
