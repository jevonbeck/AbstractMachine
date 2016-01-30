package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.util.AttributeSet;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.ui.utils.DelayedUpdateTextView;

import java.util.Observable;

/**
 * Created by Jevon on 23/01/2016.
 */
public class InstructionView extends DelayedUpdateTextView {
    public InstructionView(Context context) {
        super(context);
    }

    public InstructionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InstructionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mainTextView.setTextColor(context.getResources().getColor(android.R.color.white));
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableComputeCore &&
                o != null && o instanceof ObservableComputeCore.ExecuteParams){
            ComputeCore core = (ComputeCore) ((ObservableComputeCore) observable).getType();
            int instruction = ((ObservableComputeCore.ExecuteParams) o).getInstruction();

            setUpdateText(core.instrString(instruction));
            attemptImmediateTextUpdate();
        }
    }
}
