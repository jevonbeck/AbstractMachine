package org.ricts.abstractmachine.ui.fragment;

import android.content.Intent;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.activity.InstrMemoryAltDialogActivity;

/**
 * Created by Jevon on 02/07/2016.
 */
public class InstrMemAltFragment extends MemAltFragment {
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
        return new Intent(getContext(), InstrMemoryAltDialogActivity.class);
    }
}
