package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;

import org.ricts.abstractmachine.components.devices.Device;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.ui.device.DevicePin;

/**
 * Created by Jevon on 21/08/2015.
 */
public class MemoryPortView extends ReadPortView implements MemoryPort {
    public interface WriteResponder{
        void onWriteFinished();
        void onWriteStart();
    }

    private WriteResponder writeResponder;
    private MemoryPort ram;

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
    public void write(final int address, final int data) {
        // Setup correct data in pin UI
        DevicePin pin = pinArray[PinNames.ADDRESS.ordinal()];
        pin.data = Device.formatNumberInHex(address, addressWidth);
        pin.direction = inDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
        pin.animationDelay = startDelay;

        pin = pinArray[PinNames.DATA.ordinal()];
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

        ram.write(address, data);
        updateView(); // Animate pin UI
    }

    public void setSource(MemoryPort readSource){
        ram = readSource;
        rom = ram;
    }

    public void setWriteResponder(WriteResponder responder){
        writeResponder = responder;
    }
}
