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

    public interface UpdateResponder {
        void onUpdatePcCompleted();
        void onUpdateIrCompleted();
    }

    public interface MemoryCommandResponder {
        void onMemoryCommandIssued();
    }

    public interface StepActionResponder {
        void onAnimationEnd();
    }

    private MainBodyView mainBody;
    private PinsView pins;

    private MemoryCommandResponder memoryCommandResponder;
    private HaltResponder haltResponder;
    private StepActionResponder stepResponder;

    private boolean updateImmediately;
    private String haltString, sleepString, doneString;

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

        haltString = context.getResources().getString(R.string.pin_data_halt);
        doneString = context.getResources().getString(R.string.pin_data_done);
        sleepString = context.getResources().getString(R.string.pin_data_sleep);
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
        if(o instanceof ObservableComputeCore.ExecuteParams) {
            final ComputeCore mainCore = (ComputeCore) ((ObservableComputeCore) observable).getType();
            ObservableComputeCore.ExecuteParams params = (ObservableComputeCore.ExecuteParams) o;
            int instruction = params.getInstruction();

            mainBody.setInstructionText(mainCore.instrString(instruction));

            if(updateImmediately){
                mainBody.updateInstructionView();
            }
            else{
                int pcPreExecute = params.getPcPreExecute();
                int pcPostExecute = params.getPcPostExecute();

                final boolean cuIsPipelined = params.getCuIsPipelined();
                final boolean isDataMemInstruction = mainCore.isDataMemoryInstruction(instruction);
                final boolean isHaltInstruction = mainCore.isHaltInstruction(instruction);
                final boolean isSleepInstruction = mainCore.isSleepInstruction(instruction);
                final String pcPostExecuteString = mainCore.instrAddrValueString(pcPostExecute);
                final boolean updatePC = pcPreExecute != pcPostExecute;

                pins.setCommandResponder(new PinsView.CommandOnlyResponder() {
                    @Override
                    public void onCommandCompleted() {
                        if(isHaltInstruction || isSleepInstruction){
                            if(haltResponder != null) {
                                haltResponder.onHaltCompleted();
                            }
                        }
                        else{
                            stepResponder.onAnimationEnd();
                        }
                    }
                });

                pins.setExecuteResponder(new PinsView.ExecuteResponder() {
                    @Override
                    public void onExecuteCompleted() {
                        // first animation [executeInstruction()] ends here!
                        mainBody.updateInstructionView();

                        /** Start one of these animations if applicable (they are mutually exclusive) **/
                        if (isHaltInstruction) {
                            pins.sendCommandOnly(haltString);
                        }
                        else if (isSleepInstruction) {
                            pins.sendCommandOnly(sleepString);
                        } else if (isDataMemInstruction) {
                            if (memoryCommandResponder != null) {
                                memoryCommandResponder.onMemoryCommandIssued();
                            }
                        } else if (updatePC) {
                            pins.updatePC(pcPostExecuteString,
                                    mainCore.instrValueString(mainCore.getNopInstruction()),
                                    cuIsPipelined);
                        } else {
                            sendDoneCommand();
                        }
                    }
                });

                // actually begin the animation
                pins.executeInstruction(mainCore.instrAddrValueString(pcPreExecute),
                        mainCore.instrValueString(instruction));
            }
        }
        else if(o instanceof ObservableComputeCore.GetNopParams) {
            if(!updateImmediately){
                ComputeCore mainCore = (ComputeCore) ((ObservableComputeCore) observable).getType();
                ObservableComputeCore.GetNopParams params = (ObservableComputeCore.GetNopParams) o;
                int nopInstruction = params.getNopInstruction();

                pins.fetchNop(mainCore.instrValueString(nopInstruction));
            }
        }
        else if(o instanceof Boolean) { // update is from a reset
            mainBody.setInstructionText(null);
            mainBody.updateInstructionView();
        }
    }

    public void sendDoneCommand(){
        pins.sendCommandOnly(doneString);
    }

    public void setUpdateResponder(UpdateResponder updateResponder){
        pins.setUpdateResponder(updateResponder);
    }

    public void setHaltCommandResponder(HaltResponder responder){
        haltResponder = responder;
    }

    public void setMemoryCommandResponder(MemoryCommandResponder responder){
        memoryCommandResponder = responder;
    }

    public void setUpdateImmediately(boolean immediately){
        updateImmediately = immediately;
    }

    public void setActionResponder(StepActionResponder responder){
        stepResponder = responder;
    }

    public static class PinsView extends MultiPinView {
        private UpdateResponder updateResponder;
        private ExecuteResponder executeResponder;
        private CommandOnlyResponder cmdResponder;

        private String executeString, setNextString, getNopString;

        public interface ExecuteResponder {
            void onExecuteCompleted();
        }

        public interface CommandOnlyResponder {
            void onCommandCompleted();
        }

        protected enum PinNames{
            COMMAND, INSTRUCTION, PROG_COUNT
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
            executeString = context.getResources().getString(R.string.pin_data_execute);
            setNextString = context.getResources().getString(R.string.pin_data_set_next);
            getNopString = context.getResources().getString(R.string.pin_data_get_nop);
        }

        public void setExecuteResponder(ExecuteResponder execResponder) {
            executeResponder = execResponder;
        }

        public void executeInstruction(String programCounter, String instruction){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = executeString;
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = null;

            pin = pinArray[PinNames.PROG_COUNT.ordinal()];
            pin.data = programCounter;
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = null;

            pin = pinArray[PinNames.INSTRUCTION.ordinal()];
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

        public void updatePC(String pcValue, String irValue, boolean cuIsPipelined){
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

        public void fetchNop(String nopValue){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = getNopString;
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = null;

            pin = pinArray[PinNames.INSTRUCTION.ordinal()];
            pin.data = nopValue;
            pin.direction = outDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.DELAY;
            pin.animationDelay = -1;
            pin.animListener = new Animation.AnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(updateResponder != null){
                        updateResponder.onUpdateIrCompleted();
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
            pin.animListener = null;

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
