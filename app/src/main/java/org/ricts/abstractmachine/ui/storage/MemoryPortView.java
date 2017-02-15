package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.observables.ObservableMemoryPort;
import org.ricts.abstractmachine.ui.device.DevicePin;

import java.util.Observable;

/**
 * Created by Jevon on 21/08/2015.
 */
public class MemoryPortView extends ReadPortView {
    public interface WriteResponder{
        void onWriteFinished();
        void onWriteStart();
    }

    private WriteResponder writeResponder;
    private String writeString;

    /** Standard Constructors **/
    public MemoryPortView(Context context) {
        this(context, null);
    }

    public MemoryPortView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MemoryPortView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        writeString = context.getResources().getString(R.string.pin_data_write);
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof ObservableMemoryPort.WriteParams) {
            ObservableMemoryPort observedMemoryPort = (ObservableMemoryPort) observable;

            ObservableMemoryPort.WriteParams params = (ObservableMemoryPort.WriteParams) o;
            int address = params.getAddress();
            int data = params.getData();

            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.ADDRESS.ordinal()];
            pin.data = observedMemoryPort.addressString(address);
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;

            pin = pinArray[PinNames.DATA.ordinal()];
            pin.data = observedMemoryPort.dataString(data);
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;
            pin.animListener = null;

            pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = writeString;
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;

            pin.animListener = new Animation.AnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(writeResponder != null){
                        writeResponder.onWriteFinished(); // - RamView should update rom data UI
                    }
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {

                }

                @Override
                public void onAnimationStart(Animation arg0) {
                    if(writeResponder != null){
                        writeResponder.onWriteStart();
                    }
                }
            };

            updateView(); // Animate pin UI
        }
        else {
            super.update(observable, o);
        }
    }

    public void setWriteResponder(WriteResponder responder){
        writeResponder = responder;
    }
}
