package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;

import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.storage.RAM;
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

    /** Standard Constructors **/
    public MemoryPortView(Context context) {
        this(context, null);
    }

    public MemoryPortView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MemoryPortView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable != null && observable instanceof ObservableRAM &&
                o != null && o instanceof ObservableRAM.WriteParams) {
            ObservableRAM.WriteParams params = (ObservableRAM.WriteParams) o;
            ObservableRAM observedRam = (ObservableRAM) observable;
            RAM ram = observedRam.getType();

            final int address = params.getAddress();
            final int data = params.getData();
            int dataWidth = ram.dataWidth();
            int addressWidth = ram.addressWidth();

            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.ADDRESS.ordinal()];
            pin.dataWidth = addressWidth;
            pin.data = Device.formatNumberInHex(address, addressWidth);
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;

            pin = pinArray[PinNames.DATA.ordinal()];
            pin.dataWidth = dataWidth;
            pin.data = Device.formatNumberInHex(data, dataWidth);
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;
            pin.animListener = null;

            pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = "write";
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
