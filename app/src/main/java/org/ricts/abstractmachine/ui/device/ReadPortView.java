package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.Device;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.ui.CustomDimenRecyclerView;

/**
 * Created by Jevon on 21/08/2015.
 */
public class ReadPortView extends MultiPinView implements ReadPort {
    public interface ReadResponder{
        void onReadFinished();
        void onReadStart();
    }

    protected int dataWidth, addressWidth;
    protected int readDelay, startDelay;

    private ReadResponder readResponder;
    protected ReadPort rom;

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
        // initialise pin names (pinView data)
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
    public int read(final int address) {
        final int readData = rom.read(address);

        // Setup correct data in pin UI
        DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
        pin.data = "read";
        pin.direction = inDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
        pin.animationDelay = startDelay;
        pin.animListener = null;

        pin = pinArray[PinNames.ADDRESS.ordinal()];
        pin.data = Device.formatNumberInHex(address, addressWidth);
        pin.direction = inDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
        pin.animationDelay = startDelay;

        pin = pinArray[PinNames.DATA.ordinal()];
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

        return readData; // return actual data to underlying requester
    }

    @Override
    public int accessTime() {
        return rom.accessTime();
    }

    public void initParams(int dWidth, int aWidth){
        dataWidth = dWidth;
        addressWidth = aWidth;

        // update pin data (pinView data)
        pinArray[PinNames.ADDRESS.ordinal()].dataWidth = addressWidth;
        pinArray[PinNames.DATA.ordinal()].dataWidth = dataWidth;
    }

    public void setStartDelay(int delayMultiple){
        startDelay = getDelay(delayMultiple);
    }

    public void setReadAnimationDelay(int delayMultiple){
        readDelay = getDelay(delayMultiple);
    }

    public void setReadResponder(ReadResponder responder){
        readResponder = responder;
    }

    public void setSource(ReadPort readSource){
        rom = readSource;
    }

    private int getDelay(int multiple){
        return multiple * getContext().getResources().getInteger(R.integer.pin_sig_trans_time);
    }
}
