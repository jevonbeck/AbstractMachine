package org.ricts.abstractmachine.ui.network;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableReadPort;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.device.RelativePosition;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 07/06/2015.
 */
public class MemoryPortMultiplexerView extends MultiplexerView implements Observer{
    private static final int DEFAULT_READ_DELAY_MULTIPLE = 1;
    private static final int INPUT_PIN_DELAY_MULTIPLE = 3;

    private boolean updateImmediately, showAnimation;
    private Observable memoryPortObservable;
    private Object currentParams;
    private int activePort;

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
        showAnimation = true;
    }

    @Override
    protected View createPinView(Context context, RelativePosition pinPosition) {
        return new MemoryPortView(context, DeviceView.getDefaultAttributeSet(context, pinPosition));
    }

    @Override
    protected void initOutputPinView(View pinView) {
        MemoryPortView memoryPortPins =  (MemoryPortView) pinView;

        // implementations which use a MultiPinView subclass will probably want to do this
        memoryPortPins.setStartDelayByMultiple(DEFAULT_READ_DELAY_MULTIPLE);
    }

    @Override
    protected void initInputPinView(View pinView) {
        MemoryPortView memoryPortPins =  (MemoryPortView) pinView;
        memoryPortPins.setReadDelayByMultiple(INPUT_PIN_DELAY_MULTIPLE);
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableMultiMemoryPort && o != null) {
            ObservableMultiMemoryPort.MemoryPortParams params =
                    (ObservableMultiMemoryPort.MemoryPortParams) o;
            activePort = params.getPortId();

            int address = params.getAddress();
            if(params.hasData()) {
                int data = params.getData();
                currentParams = new ObservableMemoryPort.WriteParams(address, data);
            }
            else {
                currentParams = new ObservableReadPort.ReadParams(address);
            }

            if (updateImmediately) {
                animatePins();
            }
        }
        else {
            super.update(observable, o);
        }
    }

    public void setUpdateImmediately(boolean immediately){
        updateImmediately = immediately;
    }

    public void showPinAnimations(boolean show){
        showAnimation = show;
    }

    public void animatePins(){
        if(showAnimation) {
            if (currentParams != null && currentParams instanceof ObservableReadPort.ReadParams) {
                animateSelectPin(); // initiate select pin animation
            }

            MemoryPortView activePin = (MemoryPortView) getInputs()[activePort];
            if(activePort == getSelection()) {
                ((MemoryPortView) getOutput()).update(
                        memoryPortObservable, currentParams); // initiate output pin animation
                activePin.update(memoryPortObservable, currentParams); // initiate selected input pin animation
            }
            else {
                // initiate active input pin animation (will return immediately with bad value)
                activePin.setReadDelayByMultiple(DEFAULT_READ_DELAY_MULTIPLE);
                activePin.update(memoryPortObservable, currentParams);
                activePin.setReadDelayByMultiple(INPUT_PIN_DELAY_MULTIPLE);
            }
        }
    }

    public void setTargetMemoryPort(MemoryPort port) {
        memoryPortObservable = new ObservableMemoryPort(port);
    }
}
