package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;

import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.observables.ObservableROM;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.MultiPinView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 21/08/2015.
 */
public class ReadPortView extends MultiPinView implements Observer {
    public interface ReadResponder{
        void onReadFinished();
        void onReadStart();
    }

    protected int readDelay;

    private ReadResponder readResponder;

    protected enum PinNames{
        COMMAND, ADDRESS, DATA
    }

    /** Standard Constructors **/
    public ReadPortView(Context context) {
        this(context, null);
    }

    public ReadPortView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadPortView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /*** create pin child data ***/
        // initialise pin names (memoryPins data)
        DevicePin[] pinData = new DevicePin[PinNames.values().length];
        DevicePin pin = new DevicePin();
        pin.name = "command";
        pin.dataWidth = 2;
        pinData[PinNames.COMMAND.ordinal()] = pin;

        pin = new DevicePin();
        pin.name = "address";
        pinData[PinNames.ADDRESS.ordinal()] = pin;

        pin = new DevicePin();
        pin.name = "data";
        pinData[PinNames.DATA.ordinal()] = pin;

        /*** bind pin child to its data ***/
        setPinData(pinData);

        /*** Setup other vars ***/
        setReadAnimationDelay(1);
        startDelay = 0;
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof ObservableROM.ReadParams) {
            ObservableROM observedRom = (ObservableROM) observable;
            ROM rom = (ROM) observedRom.getType();

            final int address = ((ObservableROM.ReadParams) o).getAddress();
            final int readData = rom.read(address);
            int dataWidth = rom.dataWidth();
            int addressWidth = rom.addressWidth();

            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = "read";
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;
            pin.animListener = null;

            pin = pinArray[PinNames.ADDRESS.ordinal()];
            pin.dataWidth = addressWidth;
            pin.data = Device.formatNumberInHex(address, addressWidth);
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;

            pin = pinArray[PinNames.DATA.ordinal()];
            pin.dataWidth = dataWidth;
            pin.data = Device.formatNumberInHex(readData, dataWidth);
            pin.direction = outDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay + readDelay;
            pin.animListener = new Animation.AnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation){
                    if(readResponder != null){
                        readResponder.onReadFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {

                }

                @Override
                public void onAnimationStart(Animation arg0) {
                    if(readResponder != null){
                        readResponder.onReadStart();
                    }
                }
            };

            updateView(); // Animate pin UI
        }
    }

    public void setReadAnimationDelay(int delayMultiple){
        readDelay = getDelay(delayMultiple);
    }

    public void setReadResponder(ReadResponder responder){
        readResponder = responder;
    }
}
