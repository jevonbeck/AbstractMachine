package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.device.MultiPinView;
import org.ricts.abstractmachine.ui.device.RelativePosition;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 18/12/2015.
 */
public class ComputeAltCoreView extends DeviceView implements Observer {
    private static final String TAG = "ComputeAltCoreView";

    public interface MemoryCommandResponder {
        void onMemoryCommandIssued();
    }

    private MainBodyView mainBody;
    private PinsView pins;

    private boolean updateImmediately;
    private ControlUnitInterfaceView cuCommandInterface;
    private MemoryCommandResponder memoryCommandResponder;
    private String haltString, sleepString, doneString;

    public ComputeAltCoreView(Context context) {
        this(context, null);
    }

    public ComputeAltCoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComputeAltCoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mainBody = (MainBodyView) mainView;
        pins = (PinsView) pinView;

        haltString = context.getResources().getString(R.string.pin_data_halt);
        doneString = context.getResources().getString(R.string.pin_data_done);
        sleepString = context.getResources().getString(R.string.pin_data_sleep);
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
        if(o instanceof ObservableComputeAltCore.ExecuteParams) {
            final CompCore mainCore = (CompCore) ((ObservableComputeAltCore) observable).getType();

            mainBody.setStatusRegText(mainCore.getALU().statusString());

            if(updateImmediately){
                mainBody.updateStatusView();
            }
            else{
                int pcPostExecute = mainCore.getProgramCounterValue();
                final boolean updatePC = mainCore.controlUnitUpdated();

                DecoderUnit decoderUnit = mainCore.getDecoderUnit();
                final boolean cuIsPipelined = decoderUnit.hasTempStorage();
                final boolean isDataMemInstruction = decoderUnit.isDataMemoryInstruction();
                final boolean isHaltInstruction = decoderUnit.isHaltInstruction();
                final boolean isSleepInstruction = decoderUnit.isSleepInstruction();
                final String pcPostExecuteString = decoderUnit.instrAddrValueString(pcPostExecute);
                final String nopInstructionString = decoderUnit.instrValueString(decoderUnit.getNopInstruction());

                pins.setExecuteResponder(new PinsView.ExecuteResponder() {
                    @Override
                    public void onExecuteCompleted() {
                        // first animation [executeInstruction()] ends here!
                        mainBody.updateStatusView();

                        /** Execute one of these if applicable (they are mutually exclusive) **/
                        if (isDataMemInstruction) {
                            memoryCommandResponder.onMemoryCommandIssued();
                        } else if (isHaltInstruction) {
                            cuCommandInterface.sendCommandOnly(haltString);
                        } else if (isSleepInstruction) {
                            cuCommandInterface.sendCommandOnly(sleepString);
                        } else if (updatePC) {
                            cuCommandInterface.updateFetchUnit(pcPostExecuteString, nopInstructionString, cuIsPipelined);
                        } else {
                            sendDoneCommand();
                        }
                    }
                });

                // actually begin the animation
                pins.executeInstruction(decoderUnit.getMneumonic(), decoderUnit.getOperandsString());
            }
        }
        else if(o instanceof Boolean) { // update is from a reset
            mainBody.setStatusRegText(null);
            mainBody.updateStatusView();
        }
    }

    public void setControlUnitCommandInterface(ControlUnitInterfaceView pinsView){
        cuCommandInterface = pinsView;
    }

    public void setMemoryCommandResponder(MemoryCommandResponder responder){
        memoryCommandResponder = responder;
    }

    public void sendDoneCommand() {
        cuCommandInterface.sendCommandOnly(doneString);
    }

    public void setUpdateImmediately(boolean immediately){
        updateImmediately = immediately;
    }

    public static class PinsView extends MultiPinView {
        private ExecuteResponder executeResponder;

        public interface ExecuteResponder {
            void onExecuteCompleted();
        }

        protected enum PinNames{
            MNEUMONIC, OPERANDS
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
            pin.name = context.getResources().getString(R.string.pin_name_mneumonic);
            pinData[PinNames.MNEUMONIC.ordinal()] = pin;

            pin = new DevicePin();
            pin.name = context.getResources().getString(R.string.pin_name_operands);
            pinData[PinNames.OPERANDS.ordinal()] = pin;

            /*** bind pin child to its data ***/
            setPinData(pinData);

            /*** Setup other vars ***/
            setStartDelay(500);
        }

        public void setExecuteResponder(ExecuteResponder execResponder) {
            executeResponder = execResponder;
        }

        public void executeInstruction(String mneumonic, String operands){
            // Setup correct data in pin UI
            DevicePin pin = pinArray[PinNames.MNEUMONIC.ordinal()];
            pin.data = mneumonic;
            pin.direction = inDirection;
            pin.action = DevicePin.PinAction.MOVING;
            pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
            pin.animListener = null;

            pin = pinArray[PinNames.OPERANDS.ordinal()];
            pin.data = operands;
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
    }

    private static class MainBodyView extends RelativeLayout {
        private TextView statusRegView;
        private String statusRegText;

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
            TextView statusRegLabel = new TextView(context);
            statusRegLabel.setId(R.id.ComputeAltCoreView_status_label);
            statusRegLabel.setTextColor(context.getResources().getColor(android.R.color.white));
            statusRegLabel.setText(context.getResources().getText(R.string.status_reg_label));

            statusRegView = new TextView(context);
            statusRegView.setTextColor(context.getResources().getColor(android.R.color.white));
            statusRegView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

            /*** determine children layouts and positions ***/
            LayoutParams lpStatusLabel = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            addView(statusRegLabel, lpStatusLabel);

            LayoutParams lpStatusView = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lpStatusView.addRule(RelativeLayout.RIGHT_OF, statusRegLabel.getId());
            lpStatusView.addRule(RelativeLayout.ALIGN_TOP, statusRegLabel.getId());
            addView(statusRegView, lpStatusView);
        }

        public void updateStatusView(){
            statusRegView.setText(statusRegText);
        }

        public void setStatusRegText(String text){
            statusRegText = text;
        }
    }
}
