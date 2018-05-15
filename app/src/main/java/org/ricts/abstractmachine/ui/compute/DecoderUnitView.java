package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.device.MultiPinView;
import org.ricts.abstractmachine.ui.device.RelativePosition;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 19/08/2017.
 */
public class DecoderUnitView extends DeviceView implements Observer {
    private static final String TAG = "DecoderUnitView";

    public interface StepActionResponder {
        void onAnimationEnd();
    }

    private MainBodyView mainBody;
    private PinsView pins;

    private boolean updateImmediately;
    private StepActionResponder stepResponder;

    public DecoderUnitView(Context context) {
        this(context, null);
    }

    public DecoderUnitView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DecoderUnitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mainBody = (MainBodyView) mainView;
        pins = (PinsView) pinView;
    }

    @Override
    protected View createPinView(Context context, RelativePosition pinPosition) {
        return new PinsView(context, getDefaultAttributeSet(context, pinPosition));
    }

    @Override
    protected View createMainView(Context context, RelativePosition pinPosition) {
        return new MainBodyView(context);
    }

    @Override
    protected LayoutParams createMainViewLayoutParams() {
        return new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof ObservableDecoderUnit.DecodeParams) {
            DecoderUnit decoderUnit = ((ObservableDecoderUnit) observable).getType();
            ObservableDecoderUnit.DecodeParams decodeParams = (ObservableDecoderUnit.DecodeParams) o;

            mainBody.setInstructionText(decodeParams.getInstructionString());

            if(updateImmediately){
                mainBody.updateInstructionView();
            }
            else{
                ObservableDecoderUnit.DecodeParams params = (ObservableDecoderUnit.DecodeParams) o;
                int instruction = params.getInstruction();
                int programCounter = params.getProgramCounter();

                // actually begin the animation
                pins.decodeInstruction(decoderUnit.instrAddrValueString(programCounter),
                        decoderUnit.instrValueString(instruction));
            }
        }
        else if(o instanceof ObservableDecoderUnit.GetNopParams) {
            if(!updateImmediately){
                DecoderUnit decoderUnit = ((ObservableDecoderUnit) observable).getType();
                pins.fetchNop(decoderUnit.instrValueString(decoderUnit.getNopInstruction()));
            }
        }
        else if(o instanceof ObservableDecoderUnit.InvalidateParams) {
            if(updateImmediately) {
                clearMainBodyText();
            }
        }
        else if(o instanceof Boolean) { // update is from a reset
            clearMainBodyText();
        }
    }

    public void setNopResponder(PinsView.NopResponder nopResponder){
        pins.setNopResponder(nopResponder);
    }

    public void setUpdateImmediately(boolean immediately){
        updateImmediately = immediately;
    }

    public void setActionResponder(StepActionResponder responder){
        stepResponder = responder;

        pins.setDecodeResponder(new PinsView.DecodeResponder() {
            @Override
            public void onDecodeCompleted() {
                mainBody.updateInstructionView();
                stepResponder.onAnimationEnd();
            }
        });

        pins.setInvalidateResponder(new PinsView.InvalidateResponder() {
            @Override
            public void onInvalidateCompleted() {
                clearMainBodyText();
                stepResponder.onAnimationEnd();
            }
        });
    }

    public void sendInvalidateCommand() {
        if(!updateImmediately) {
            pins.animateInvalidate();
        }
        else {
            stepResponder.onAnimationEnd();
        }
    }

    private void clearMainBodyText() {
        mainBody.setInstructionText(null);
        mainBody.updateInstructionView();
    }

    public static class PinsView extends MultiPinView {
        private NopResponder nopResponder;
        private DecodeResponder decodeResponder;
        private InvalidateResponder invalidateResponder;

        private String decodeString, getNopString, invalidateString;

        public interface DecodeResponder {
            void onDecodeCompleted();
        }

        public interface NopResponder {
            void onFetchNopCompleted();
        }

        public interface InvalidateResponder {
            void onInvalidateCompleted();
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
            decodeString = context.getResources().getString(R.string.pin_data_decode);
            getNopString = context.getResources().getString(R.string.pin_data_get_nop);
            invalidateString = context.getResources().getString(R.string.pin_data_invalidate);
        }

        public void setDecodeResponder(DecodeResponder decResponder) {
            decodeResponder = decResponder;
        }

        public void decodeInstruction(String programCounter, String instruction){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = decodeString;
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
                    if(decodeResponder != null){
                        decodeResponder.onDecodeCompleted();
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

        public void setNopResponder(NopResponder responder){
            nopResponder = responder;
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
                    if(nopResponder != null){
                        nopResponder.onFetchNopCompleted();
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

        public void setInvalidateResponder(InvalidateResponder responder) {
            invalidateResponder = responder;
        }

        public void animateInvalidate(){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.COMMAND.ordinal()];
            pin.data = invalidateString;
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = new Animation.AnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(invalidateResponder != null){
                        invalidateResponder.onInvalidateCompleted();
                    }
                }

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };

            pin = pinArray[PinNames.INSTRUCTION.ordinal()];
            pin.action = DevicePin.PinAction.STATIONARY;
            pin.animListener = null;

            pin = pinArray[PinNames.PROG_COUNT.ordinal()];
            pin.action = DevicePin.PinAction.STATIONARY;
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
            int padding = (int) (6 * scaleFactor);
            setPadding(padding, padding, padding, padding);

            /*** create children ***/
            TextView instructionLabel = new TextView(context);
            instructionLabel.setId(R.id.DecoderUnitView_instruction_label);
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
            lpInstructionView.addRule(RelativeLayout.BELOW, instructionLabel.getId());
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
