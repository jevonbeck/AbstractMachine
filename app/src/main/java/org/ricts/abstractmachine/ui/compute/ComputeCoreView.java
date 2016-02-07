package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.device.MultiPinView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 18/12/2015.
 */
public class ComputeCoreView extends DeviceView implements Observer {
    private static final String TAG = "ComputeCoreView";

    public interface HaltResponder {
        void onHaltCompleted();
    }

    public interface MemoryCommandResponder {
        void onMemoryCommandIssued();
    }

    private MainBodyView mainBody;
    private PinsView pins;
    private MemoryCommandResponder memoryCommandResponder;
    private HaltResponder haltResponder;

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
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableComputeCore &&
                o != null && o instanceof ObservableComputeCore.ExecuteParams) {
            ComputeCore mainCore = (ComputeCore) ((ObservableComputeCore) observable).getType();
            ObservableComputeCore.ExecuteParams params = (ObservableComputeCore.ExecuteParams) o;
            int instruction = params.getInstruction();
            ControlUnitInterface controlUnit = params.getControlUnit();
            int pcPreExecute = params.getPcPreExecute();
            int pcPostExecute = controlUnit.getPC();

            final boolean isDataMemInstruction = mainCore.isDataMemoryInstruction(instruction);
            final boolean isHaltInstruction = mainCore.isHaltInstruction(instruction);
            final String pcPostExecuteString = Device.formatNumberInHex(pcPostExecute, mainCore.iAddrWidth());
            final boolean updatePC = pcPreExecute != pcPostExecute;

            mainBody.setInstructionText(mainCore.instrString(instruction));

            pins.setCommandResponder(new PinsView.CommandOnlyResponder() {
                @Override
                public void onCommandCompleted() {
                    if(isHaltInstruction){
                        if(haltResponder != null) {
                            haltResponder.onHaltCompleted();
                        }
                    }
                }
            });

            pins.setExecuteResponder(new PinsView.ExecuteResponder() {
                @Override
                public void onExecuteCompleted() {
                    // first animation [executeInstruction()] ends here!
                    mainBody.updateInstructionView();

                    /** Start one of these animations if applicable (they are mutually exclusive) **/
                    if(isHaltInstruction){
                        pins.sendCommandOnly("setHalt");
                    }
                    else if(isDataMemInstruction){
                        if(memoryCommandResponder != null) {
                            memoryCommandResponder.onMemoryCommandIssued();
                        }
                    }

                    if (updatePC) {
                        pins.updatePC(pcPostExecuteString);
                    }
                }
            });

            // actually begin the animation
            pins.executeInstruction(Device.formatNumberInHex(instruction, mainCore.instrWidth()));
        }
    }

    public void setUpdatePcResponder(PinsView.UpdateResponder updateResponder){
        pins.setUpdateResponder(updateResponder);
    }

    public void setHaltCommandResponder(HaltResponder responder){
        haltResponder = responder;
    }

    public void setMemoryCommandResponder(MemoryCommandResponder responder){
        memoryCommandResponder = responder;
    }

    public static class PinsView extends MultiPinView {
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

        public void setExecuteResponder(ExecuteResponder execResponder) {
            executeResponder = execResponder;
        }

        public void executeInstruction(String instruction){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = "execute";
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = null;

            pin = pinArray[PinNames.DATA.ordinal()];
            pin.data = instruction;
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

        public void updatePC(String pcValue){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = "setPC";
            pin.direction = outDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = startDelay;
            pin.animListener = null;

            pin = pinArray[PinNames.DATA.ordinal()];
            pin.data = pcValue;
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

    private static class MainBodyView extends RelativeLayout {
        private TextView instructionView;
        private String instructionText;

        public MainBodyView(Context context) {
            this(context, null);
        }

        public MainBodyView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MainBodyView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            float scaleFactor = context.getResources().getDisplayMetrics().density;
            /*** setSelectWidth properties ***/
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

        public void updateInstructionView(){
            instructionView.setText(instructionText);
        }

        public void setInstructionText(String text){
            instructionText = text;
        }
    }
}
