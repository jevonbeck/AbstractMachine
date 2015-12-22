package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.devices.Device;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.device.MultiPinView;

/**
 * Created by Jevon on 18/12/2015.
 */
public class ComputeCoreView extends DeviceView implements ComputeCoreInterface {
    private static final String TAG = "ComputeCoreView";

    private ComputeCore mainCore;

    private MainBodyView mainBody;
    private PinsView pins;

    public ComputeCoreView(Context context) {
        this(context, null);
    }

    public ComputeCoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComputeCoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mainBody = (MainBodyView) mainView;
        pins = (PinsView) pinView;
    }

    @Override
    protected View createPinView(Context context, int pinPosition) {
        return new PinsView(context, getDefaultAttributeSet(context, pinPosition));
    }

    @Override
    protected View createMainView(Context context, int pinPosition) {
        return new MainBodyView(context);
    }

    @Override
    protected LayoutParams createMainViewLayoutParams() {
        return new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public void executeInstruction(final int instruction, final MemoryPort dataMemory,
                                   final ControlUnitInterface cu) {
        final boolean isDataMemInstruction = mainCore.isDataMemoryInstruction(instruction);
        final boolean isHaltInstruction = mainCore.isHaltInstruction(instruction);

        // N.B: In general, data memory instructions should NOT update PC,
        // so no harm reading PC again if data memory instruction is not executed
        final int pcPreExecute = cu.getPC();
        if( !(isDataMemInstruction || isHaltInstruction) ){ // don't execute instruction yet, since this animates UI
            mainCore.executeInstruction(instruction, dataMemory, cu);
        }
        final int pcPostExecute = cu.getPC();

        pins.setCommandResponder(new PinsView.CommandOnlyResponder() {
            @Override
            public void onCommandCompleted() {
                // perform halt OR data memory action
                mainCore.executeInstruction(instruction, dataMemory, cu);
            }
        });

        pins.setExecuteResponder(new PinsView.ExecuteResponder() {
            @Override
            public void onExecuteCompleted() {
                // first animation [executeInstruction()] ends here!
                mainBody.setText(mainCore.instrString(instruction));

                /** Start one of these animations if applicable (they are mutually exclusive) **/
                if(isHaltInstruction){
                    pins.sendCommandOnly("setHalt");
                }
                else if(isDataMemInstruction){
                    pins.sendCommandOnly("getMem");
                }

                if (pcPreExecute != pcPostExecute) {
                    pins.updatePC(pcPostExecute);
                }
            }
        });

        // actually begin the animation
        pins.executeInstruction(instruction);
    }

    @Override
    public int instrExecTime(int instruction, MemoryPort dataMemory) {
        return mainCore.instrExecTime(instruction, dataMemory);
    }

    @Override
    public int nopInstruction() {
        return mainCore.nopInstruction();
    }

    @Override
    public int dAddrWidth() {
        return mainCore.dAddrWidth();
    }

    @Override
    public int instrWidth() {
        return mainCore.instrWidth();
    }

    @Override
    public int iAddrWidth() {
        return mainCore.iAddrWidth();
    }

    @Override
    public int dataWidth() {
        return mainCore.dataWidth();
    }

    public void setComputeCore(ComputeCore core){
        mainCore = core;
        pins.initParams(mainCore.instrWidth(), mainCore.iAddrWidth());
    }

    public void setUpdatePcResponder(PinsView.UpdateResponder responder){
        pins.setUpdateResponder(responder);
    }

    public static class PinsView extends MultiPinView {
        private int instructionWidth, instructionAddrWidth;
        private UpdateResponder updateResponder;
        private ExecuteResponder executeResponder;
        private CommandOnlyResponder cmdResponder;

        private int startDelay;

        public interface ExecuteResponder {
            void onExecuteCompleted();
        }

        public interface UpdateResponder {
            void onUpdatePcCompleted();
        }

        public interface CommandOnlyResponder {
            void onCommandCompleted();
        }

        protected enum PinNames{
            COMMAND, DATA
        }

        public PinsView(Context context) {
            this(context, null);
        }

        public PinsView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public PinsView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            /*** create pin child data ***/
            // initialise pin names (memoryPins data)
            DevicePin[] pinData = new DevicePin[PinNames.values().length];
            DevicePin pin = new DevicePin();
            pin.name = "command";
            pin.dataWidth = 2;
            pinData[PinNames.COMMAND.ordinal()] = pin;

            pin = new DevicePin();
            pin.name = "data";
            pinData[PinNames.DATA.ordinal()] = pin;

            /*** bind pin child to its data ***/
            setPinData(pinData);

            /*** Setup other vars ***/
            startDelay = (int) (0.25 * getDelay(1));
        }

        public void initParams(int iWidth, int iAddrWidth){
            instructionWidth = iWidth;
            instructionAddrWidth = iAddrWidth;

            pinArray[PinNames.COMMAND.ordinal()].dataWidth = instructionWidth;
            pinArray[PinNames.DATA.ordinal()].dataWidth = instructionWidth;
        }

        public void setExecuteResponder(ExecuteResponder execResponder) {
            executeResponder = execResponder;
        }

        public void executeInstruction(int instruction){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = "execute";
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = null;

            pin = pinArray[PinNames.DATA.ordinal()];
            pin.data = Device.formatNumberInHex(instruction, instructionWidth);
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(executeResponder != null){
                        executeResponder.onExecuteCompleted();
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

        public void setUpdateResponder(UpdateResponder responder){
            updateResponder = responder;
        }

        public void updatePC(int pcValue){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = "setPC";
            pin.direction = outDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;
            pin.animListener = null;

            pin = pinArray[PinNames.DATA.ordinal()];
            pin.data = Device.formatNumberInHex(pcValue, instructionAddrWidth);
            pin.direction = outDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;
            pin.animListener = new Animation.AnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(updateResponder != null){
                        updateResponder.onUpdatePcCompleted();
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

            pin = pinArray[PinNames.DATA.ordinal()];
            pin.action = DevicePin.PinAction.STATIONARY;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = null;

            updateView(); // Animate pin UI
        }
    }

    private static class MainBodyView extends RelativeLayout{
        private TextView instructionView;

        public MainBodyView(Context context) {
            this(context, null);
        }

        public MainBodyView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MainBodyView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            float scaleFactor = context.getResources().getDisplayMetrics().density;
            /*** init properties ***/
            setBackgroundColor(context.getResources().getColor(R.color.reg_data_unselected));
            int padding = (int) (10 * scaleFactor);
            setPadding(padding, padding, padding, padding);

            /*** create children ***/
            TextView instructionLabel = new TextView(context);
            instructionLabel.setId(R.id.ComputeCoreView_instruction_label);
            instructionLabel.setTextColor(context.getResources().getColor(android.R.color.white));
            instructionLabel.setText(context.getResources().getText(R.string.decoded_instruction_label));

            instructionView = new TextView(context);
            instructionView.setTextColor(context.getResources().getColor(android.R.color.white));
            instructionView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

            /*** determine children layouts and positions ***/
            LayoutParams lpInstructionLabel = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            addView(instructionLabel, lpInstructionLabel);

            LayoutParams lpInstructionView = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lpInstructionView.addRule(RelativeLayout.RIGHT_OF, instructionLabel.getId());
            lpInstructionView.addRule(RelativeLayout.ALIGN_TOP, instructionLabel.getId());
            addView(instructionView, lpInstructionView);
        }

        public void setText(CharSequence text){
            instructionView.setText(text);
        }
    }
}
