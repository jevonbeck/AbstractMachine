package org.ricts.abstractmachine.ui.network;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 07/06/2015.
 */
public class MemoryPortMultiplexerView extends MultiplexerView implements Observer{
    private boolean updateImmediately;
    private Observable currentObservable;
    private Object currentObject;

    /** Standard Constructors **/
    public MemoryPortMultiplexerView(Context context) {
        this(context, null);
    }

    public MemoryPortMultiplexerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MemoryPortMultiplexerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        updateImmediately = true;
    }

    @Override
    protected View createPinView(Context context, int pinPosition) {
        return new MemoryPortView(context, DeviceView.getDefaultAttributeSet(context, pinPosition));
    }

    @Override
    protected void initOutputPinView(View pinView) {
        MemoryPortView memoryPortPins =  (MemoryPortView) pinView;

        // implementations which use a MultiPinView subclass will probably want to do this
        memoryPortPins.setStartDelay(1);
    }

    @Override
    protected void initInputPinView(View pinView) {
        MemoryPortView memoryPortPins =  (MemoryPortView) pinView;
        memoryPortPins.setReadAnimationDelay(3);
    }

    @Override
    public void update(Observable observable, Object o) {
        currentObservable = observable;
        currentObject = o;
        attemptPinAnimations();
    }

    public void setUpdateImmediately(boolean immediately){
        updateImmediately = immediately;
    }

    public void animatePins(){
        animateSelectPin(); // initiate select pin animation
        ((MemoryPortView) outputPins).update(
                currentObservable, currentObject); // initiate output pin animation
        ((MemoryPortView) inputPins[currentSel]).update(
                currentObservable, currentObject); // initiate selected input pin animation
    }

    private void attemptPinAnimations(){
        if (updateImmediately) {
            animatePins();
        }
    }
}
