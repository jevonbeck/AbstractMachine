package org.ricts.abstractmachine.ui.utils;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 23/01/2016.
 */
public abstract class DelayedUpdateTextView extends OrientableTextView implements Observer {
    private boolean updateImmediately;
    private CharSequence updateText;

    public abstract void update(Observable observable, Object o);

    public DelayedUpdateTextView(Context context) {
        super(context);
    }

    public DelayedUpdateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DelayedUpdateTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        updateImmediately = true;
    }

    public void updateDisplayText() {
        mainTextView.setText(updateText);
    }

    public void setUpdateImmediately(boolean immediately){
        updateImmediately = immediately;
    }

    protected void attemptImmediateTextUpdate(){
        if (updateImmediately) {
            updateDisplayText();
        }
    }

    protected void setUpdateText(CharSequence text){
        updateText = text;
    }
}
