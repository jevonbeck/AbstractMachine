package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.MultiPinView;

/**
 * Created by jevon.beckles on 19/08/2017.
 */

public class ControlUnitInterfaceView extends MultiPinView {
    private UpdateResponder updateResponder;
    private CommandOnlyResponder cmdResponder;

    private String setNextString;

    public interface UpdateResponder {
        void onUpdateFetchUnitCompleted();
    }

    public interface CommandOnlyResponder {
        void onCommandCompleted();
    }

    protected enum PinNames{
        COMMAND, INSTRUCTION, PROG_COUNT
    }

    public ControlUnitInterfaceView(Context context) {
        this(context, null);
    }

    public ControlUnitInterfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlUnitInterfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /*** create pin child data ***/
        // initialise pin names (memoryPins data)
        DevicePin[] pinData = new DevicePin[PinNames.values().length];
        DevicePin pin = new DevicePin();
        pin.name = context.getResources().getString(R.string.pin_name_command);
        pinData[PinNames.COMMAND.ordinal()] = pin;

        pin = new DevicePin();
        pin.name = context.getResources().getString(R.string.pin_name_pc_val);
        pinData[PinNames.PROG_COUNT.ordinal()] = pin;

        pin = new DevicePin();
        pin.name = context.getResources().getString(R.string.pin_name_ir_val);
        pinData[PinNames.INSTRUCTION.ordinal()] = pin;

        /*** bind pin child to its data ***/
        setPinData(pinData);

        /*** Setup other vars ***/
        setStartDelay(500);
        setNextString = context.getResources().getString(R.string.pin_data_set_next);
    }

    public void setUpdateResponder(UpdateResponder responder){
        updateResponder = responder;
    }

    public void updateFetchUnit(String pcValue, String irValue, boolean cuIsPipelined){
        // Setup correct data in pin UI
        DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
        pin.data = setNextString;
        pin.direction = outDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
        pin.animationDelay = startDelay;
        pin.animListener = null;

        pin = pinArray[PinNames.INSTRUCTION.ordinal()];
        pin.data = irValue;
        pin.direction = outDirection;
        pin.action = cuIsPipelined ? DevicePin.PinAction.MOVING : DevicePin.PinAction.STATIONARY;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
        pin.animationDelay = startDelay;
        pin.animListener = null;

        pin = pinArray[PinNames.PROG_COUNT.ordinal()];
        pin.data = pcValue;
        pin.direction = outDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
        pin.animationDelay = startDelay;
        pin.animListener = new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation animation) {
                if(updateResponder != null){
                    updateResponder.onUpdateFetchUnitCompleted();
                }
            }

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        updateView(); // Animate pin UI
    }

    public void setCommandResponder(CommandOnlyResponder responder){
        cmdResponder = responder;
    }

    public void sendCommandOnly(String command){
        // Setup correct data in pin UI
        DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
        pin.data = command;
        pin.direction = outDirection;
        pin.action = DevicePin.PinAction.MOVING;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
        pin.animationDelay = startDelay;
        pin.animListener = new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation animation) {
                if(cmdResponder != null){
                    cmdResponder.onCommandCompleted();
                }
            }

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        pin = pinArray[PinNames.PROG_COUNT.ordinal()];
        pin.action = DevicePin.PinAction.STATIONARY;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
        pin.animListener = null;

        pin = pinArray[PinNames.INSTRUCTION.ordinal()];
        pin.action = DevicePin.PinAction.STATIONARY;
        pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
        pin.animListener = null;

        updateView(); // Animate pin UI
    }
}