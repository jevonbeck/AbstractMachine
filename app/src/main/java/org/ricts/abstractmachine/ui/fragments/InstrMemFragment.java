package org.ricts.abstractmachine.ui.fragments;

import android.content.Intent;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.activities.InstrMemoryDialogActivity;

/**
 * Created by Jevon on 02/07/2016.
 */
public class InstrMemFragment extends MemFragment {
    @Override
    protected int titleStringResource() {
        return R.string.program_memory_title;
    }

    @Override
    protected MemoryType memoryType() {
        return MemoryType.INSTRUCTION;
    }

    @Override
    protected Intent getDialogActivityIntent() {
        return new Intent(getContext(), InstrMemoryDialogActivity.class);
    }
}
