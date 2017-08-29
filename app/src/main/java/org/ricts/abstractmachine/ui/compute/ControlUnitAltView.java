package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.FetchUnit;
import org.ricts.abstractmachine.components.compute.cu.fsm.ControlUnitState;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;
import org.ricts.abstractmachine.components.interfaces.FetchCore;
import org.ricts.abstractmachine.components.observable.ObservableCuFSM;
import org.ricts.abstractmachine.components.observable.ObservableDefaultValueSource;
import org.ricts.abstractmachine.components.observable.ObservableFetchCore;
import org.ricts.abstractmachine.ui.storage.ReadPortView;

import java.util.Observable;
import java.util.Observer;

public class ControlUnitAltView extends RelativeLayout implements Observer{
    private InspectActionResponder actionResponder;

    private TextView pc; // Program Counter
    private TextView ir; // Instruction Register
    private TextView stateView; // Control Unit state

    private String pcText, irText, stateText;
    private boolean updateImmediately, irDefaultValueSourceCalled;

    public ControlUnitAltView(Context context) {
        this(context, null);
    }

    public ControlUnitAltView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlUnitAltView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        float scaleFactor = context.getResources().getDisplayMetrics().density;
        /*** setSelectWidth properties ***/
        setBackgroundColor(context.getResources().getColor(R.color.reg_data_unselected));
        int padding = (int) (10 * scaleFactor);
        setPadding(padding, padding, padding, padding);

        /*** create children ***/
        pc = new TextView(context);
        pc.setId(R.id.ControlUnitView_pc_view);
        pc.setTypeface(Typeface.MONOSPACE);
        pc.setTextColor(context.getResources().getColor(android.R.color.white));
        pc.setBackgroundColor(context.getResources().getColor(R.color.test_color));

        ir = new TextView(context);
        ir.setId(R.id.ControlUnitView_ir_view);
        ir.setTypeface(Typeface.MONOSPACE);
        ir.setTextColor(context.getResources().getColor(android.R.color.white));
        ir.setBackgroundColor(context.getResources().getColor(R.color.test_color));

        TextView pcLabel = new TextView(context);
        pcLabel.setId(R.id.ControlUnitView_pc_label);
        pcLabel.setTypeface(Typeface.MONOSPACE);
        pcLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        pcLabel.setText(context.getResources().getText(R.string.program_counter_label));

        TextView irLabel = new TextView(context);
        irLabel.setId(R.id.ControlUnitView_ir_label);
        irLabel.setTypeface(Typeface.MONOSPACE);
        irLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        irLabel.setText(context.getResources().getText(R.string.instruction_register_label));

        TextView stateLabel = new TextView(context);
        stateLabel.setId(R.id.ControlUnitView_state_label);
        stateLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        stateLabel.setText(context.getResources().getText(R.string.control_unit_state_label));

        stateView = new TextView(context);
        stateView.setId(R.id.ControlUnitView_fsm_view);
        stateView.setTextColor(context.getResources().getColor(android.R.color.white));
        stateView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

        /*** determine children layouts and positions ***/
        int viewWidth = (int) (100 * scaleFactor);

        LayoutParams lpPcLabel = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(pcLabel, lpPcLabel);

        LayoutParams lpPcView = new LayoutParams(
                viewWidth, LayoutParams.WRAP_CONTENT);
        lpPcView.addRule(RelativeLayout.RIGHT_OF, pcLabel.getId());
        lpPcView.addRule(RelativeLayout.ALIGN_TOP, pcLabel.getId());
        addView(pc, lpPcView);

        LayoutParams lpIrLabel = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpIrLabel.addRule(RelativeLayout.BELOW, pcLabel.getId());
        lpIrLabel.addRule(RelativeLayout.ALIGN_LEFT, pcLabel.getId());
        addView(irLabel, lpIrLabel);

        LayoutParams lpIrView = new LayoutParams(
                viewWidth, LayoutParams.WRAP_CONTENT);
        lpIrView.addRule(RelativeLayout.RIGHT_OF, irLabel.getId());
        lpIrView.addRule(RelativeLayout.ALIGN_TOP, irLabel.getId());
        addView(ir, lpIrView);

        LayoutParams lpStateLabel = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpStateLabel.addRule(RelativeLayout.BELOW, irLabel.getId());
        addView(stateLabel, lpStateLabel);

        LayoutParams lpStateView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpStateView.addRule(RelativeLayout.RIGHT_OF, stateLabel.getId());
        lpStateView.addRule(RelativeLayout.ALIGN_TOP, stateLabel.getId());
        lpStateView.addRule(RelativeLayout.ALIGN_RIGHT, ir.getId());
        addView(stateView, lpStateView);
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableCuFSM) {
            boolean isUpdateFromReset = o != null && o instanceof Boolean;

            CuFsmInterface fsm = ((ObservableCuFSM) observable).getType();
            updateStateText(fsm.currentState());

            if( updateImmediately || isUpdateFromReset ||
               !(fsm.isInHaltState() || fsm.isInSleepState()) ){
                updateState();
            }
        }
        else if(observable instanceof ObservableFetchCore) {
            boolean isUpdateFromFetch = o != null && o instanceof ObservableFetchCore.FetchObject;
            boolean isUpdateFromExpectedPC = o != null && o instanceof ObservableFetchCore.ExpectedPcObject;
            boolean isUpdateFromSetRegs = o != null && o instanceof ObservableFetchCore.SetRegsObject;

            boolean isUpdateFromReset = irDefaultValueSourceCalled && isUpdateFromSetRegs;
            if(isUpdateFromReset) {
                irDefaultValueSourceCalled = false; // clear indication that update was from reset
            }

            FetchUnit fetchUnit = ((ObservableFetchCore) observable).getType();
            pcText = fetchUnit.getPCString();
            irText = fetchUnit.getIRString();

            boolean isControlUnitReset = isUpdateFromReset && !fetchUnit.hasTempRegs();
            if(updateImmediately || isControlUnitReset){
                updateIR();
                if(isControlUnitReset){
                    actionResponder.onResetAnimationEnd(); // ControlUnit case
                }
            }

            if(updateImmediately || isUpdateFromReset || isUpdateFromFetch || isUpdateFromExpectedPC){
                if(isUpdateFromFetch){
                    ObservableFetchCore.FetchObject fetchObject = (ObservableFetchCore.FetchObject) o;
                    pcText = fetchObject.getPC();
                }
                updatePC();
            }
        }
        else if(observable instanceof ObservableDefaultValueSource) {
            irDefaultValueSourceCalled = true; // indicate to reg core that update is from reset
        }
    }

    public void initCU(CuFsmInterface fsm, FetchCore fetchCore, DecoderUnitView decoderView,
                       ControlUnitInterfaceView cuInterfaceView, ReadPortView instructionCache){
        /** initialise variables **/
        updateStateText(fsm.currentState());
        updateState();
        pc.setText(fetchCore.getPCString());
        ir.setText(fetchCore.getIRString());

        /** setup callback behaviour **/
        instructionCache.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                updateIR(); // only update ir when read finished
                actionResponder.onStepAnimationEnd();
            }

            @Override
            public void onReadStart() {

            }
        });

        decoderView.setNopResponder(new DecoderUnitView.PinsView.NopResponder() {
            @Override
            public void onFetchNopCompleted() {
                updateIR();
                actionResponder.onResetAnimationEnd();  // PipelinedControlUnit case
            }
        });

        cuInterfaceView.setUpdateResponder(new ControlUnitInterfaceView.UpdateResponder() {
            @Override
            public void onUpdateFetchUnitCompleted() {
                updatePC();
                updateIR();
                actionResponder.onStepAnimationEnd();
            }
        });

        cuInterfaceView.setCommandResponder(new ControlUnitInterfaceView.CommandOnlyResponder() {
            @Override
            public void onCommandCompleted() {
                updateState();
                actionResponder.onStepAnimationEnd();
            }
        });
    }

    public void setUpdateImmediately(boolean immediately){
        updateImmediately = immediately;
    }

    public void setActionResponder(InspectActionResponder responder){
        actionResponder = responder;
    }

    private void updateStateText(String text) {
        int resId;
        switch(Enum.valueOf(ControlUnitState.GenericCUState.class, text)) {
            case FETCH:
                resId = R.string.control_unit_fetch_state;
                break;
            case DECODE:
                resId = R.string.control_unit_decode_state;
                break;
            case EXECUTE:
                resId = R.string.control_unit_execute_state;
                break;
            case ACTIVE:
                resId = R.string.control_unit_active_state;
                break;
            case SLEEP:
                resId = R.string.control_unit_sleep_state;
                break;
            case HALT:
                resId = R.string.control_unit_halt_state;
                break;
            default:
                resId = 0;
        }

        stateText = getResources().getString(resId);
    }

    private void updatePC(){
        pc.setText(pcText);
    }

    private void updateIR(){
        ir.setText(irText);
    }

    private void updateState(){
        stateView.setText(stateText);
    }
}
