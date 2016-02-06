package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.util.AttributeSet;

import org.ricts.abstractmachine.components.observables.ObservableFSM;
import org.ricts.abstractmachine.ui.utils.DelayedUpdateTextView;

import java.util.Observable;

/**
 * Created by Jevon on 03/01/2016.
 */
public class FSMView extends DelayedUpdateTextView {

    public FSMView(Context context) {
        this(context, null);
    }

    public FSMView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FSMView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mainTextView.setTextColor(context.getResources().getColor(android.R.color.white));
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableFSM) {
            setUpdateText( ((ObservableFSM) observable).currentState().getName() );
            attemptImmediateTextUpdate();
        }
    }
}
