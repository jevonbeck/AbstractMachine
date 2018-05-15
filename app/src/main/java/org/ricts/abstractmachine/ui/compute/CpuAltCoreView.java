package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.FetchUnit;
import org.ricts.abstractmachine.components.compute.cu.fsm.ControlUnitState;
import org.ricts.abstractmachine.components.interfaces.ALU;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.interfaces.FetchCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableCuFSM;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableDefaultValueSource;
import org.ricts.abstractmachine.components.observable.ObservableFetchCore;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;
import org.ricts.abstractmachine.ui.storage.RamView;
import org.ricts.abstractmachine.ui.storage.ReadPortView;
import org.ricts.abstractmachine.ui.storage.RomView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 18/01/15.
 */
public class CpuAltCoreView extends RelativeLayout implements Observer {
    private static final String TAG = "CpuAltCoreView";

    private InspectActionResponder responder;

    private TextView pc, ir;
    private TextView stateView, instructionView, aluStateView;
    private String irText;
    private boolean isVisible, irDefaultValueSourceCalled;


    /** Standard Constructors **/
    public CpuAltCoreView(Context context) {
        this(context, null);
    }

    public CpuAltCoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CpuAltCoreView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        float scaleFactor = context.getResources().getDisplayMetrics().density;
        /*** setSelectWidth properties ***/
        setBackgroundColor(context.getResources().getColor(R.color.reg_data_unselected));
        int padding = (int) (10 * scaleFactor);
        setPadding(padding, padding, padding, padding);

        /*** create children ***/
        pc = new TextView(context);
        pc.setId(R.id.CpuCoreView_pc_view);
        pc.setTextColor(context.getResources().getColor(android.R.color.white));
        pc.setBackgroundColor(context.getResources().getColor(R.color.test_color));

        ir = new TextView(context);
        ir.setId(R.id.CpuCoreView_ir_view);
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
        stateView.setId(R.id.CpuCoreView_fsm_view);
        stateView.setTextColor(context.getResources().getColor(android.R.color.white));
        stateView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

        TextView instructionLabel = new TextView(context);
        instructionLabel.setId(R.id.ComputeCoreView_instruction_label);
        instructionLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        instructionLabel.setText(context.getResources().getText(R.string.decoded_instruction_label));

        instructionView = new TextView(context);
        instructionView.setTextColor(context.getResources().getColor(android.R.color.white));
        instructionView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

        TextView aluStateLabel = new TextView(context);
        aluStateLabel.setId(R.id.ComputeCoreView_alu_state_label);
        aluStateLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        aluStateLabel.setText(context.getResources().getText(R.string.alu_state_label));

        aluStateView = new TextView(context);
        aluStateView.setTextColor(context.getResources().getColor(android.R.color.white));
        aluStateView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

        /*** determine children layouts and positions ***/
        int viewWidth = (int) (110 * scaleFactor);

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
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpStateView.addRule(RelativeLayout.RIGHT_OF, stateLabel.getId());
        lpStateView.addRule(RelativeLayout.ALIGN_TOP, stateLabel.getId());
        lpStateView.addRule(RelativeLayout.ALIGN_RIGHT, ir.getId());
        addView(stateView, lpStateView);

        LayoutParams lpInstructionLabel = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpInstructionLabel.addRule(RelativeLayout.BELOW, stateLabel.getId());
        addView(instructionLabel, lpInstructionLabel);

        LayoutParams lpInstructionView = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpInstructionView.addRule(RelativeLayout.RIGHT_OF, instructionLabel.getId());
        lpInstructionView.addRule(RelativeLayout.ALIGN_TOP, instructionLabel.getId());
        lpInstructionView.addRule(RelativeLayout.ALIGN_RIGHT, ir.getId());
        addView(instructionView, lpInstructionView);

        LayoutParams lpAluStateLabel = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpAluStateLabel.addRule(RelativeLayout.BELOW, instructionLabel.getId());
        addView(aluStateLabel, lpAluStateLabel);

        LayoutParams lpAluStateView = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpAluStateView.addRule(RelativeLayout.RIGHT_OF, aluStateLabel.getId());
        lpAluStateView.addRule(RelativeLayout.ALIGN_TOP, aluStateLabel.getId());
        lpAluStateView.addRule(RelativeLayout.ALIGN_RIGHT, ir.getId());
        addView(aluStateView, lpAluStateView);

        /*** Initialise other vars ***/
        isVisible = false;
    }

    public void initCpu(CuFsmInterface fsm, FetchCore regCore, ALU alu,
                        RomView instructionCache, RamView dataMemory){
        /** initialise variables **/
        updateState(fsm.currentState());
        pc.setText(regCore.getPCString());
        ir.setText(regCore.getIRString());
        aluStateView.setText(alu.statusString());

        /** setup callback behaviour **/
        dataMemory.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                responder.onStepAnimationEnd(); // only update when read is finished
            }

            @Override
            public void onReadStart() {

            }
        });

        dataMemory.setWriteResponder(new MemoryPortView.WriteResponder() {
            @Override
            public void onWriteFinished() {
                responder.onStepAnimationEnd(); // only update when write is finished
            }

            @Override
            public void onWriteStart() {

            }
        });

        // N.B: Set instructionCache ReadResponder second to cater for Von Neumann case, i.e., when
        // instructionCache == dataMemory. In this way the correct ReadResponder will be active.
        instructionCache.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                updateIrText(); // only update ir when read finished
                responder.onStepAnimationEnd();
            }

            @Override
            public void onReadStart() {

            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableCuFSM) {
            CuFsmInterface fsm = ((ObservableCuFSM) observable).getType();
            updateState(fsm.currentState());
        }
        else if(observable instanceof ObservableFetchCore) {
            FetchUnit fetchUnit = ((ObservableFetchCore) observable).getType();
            pc.setText(fetchUnit.getPCString());
            irText = fetchUnit.getIRString();

            boolean isUpdateFromSetRegs = o != null && o instanceof ObservableFetchCore.SetRegsObject;
            boolean isUpdateFromReset = irDefaultValueSourceCalled && isUpdateFromSetRegs;
            if(isUpdateFromReset) {
                irDefaultValueSourceCalled = false; // clear indication that update was from reset
            }

            if(!isVisible || isUpdateFromReset){
                updateIrText();
            }
        }
        else if(observable instanceof ObservableDefaultValueSource) {
            irDefaultValueSourceCalled = true; // indicate to reg core that update is from reset
        }
        else if(observable instanceof ObservableDecoderUnit){
            if(o instanceof ObservableDecoderUnit.DecodeParams) {
                ObservableDecoderUnit.DecodeParams decodeParams = (ObservableDecoderUnit.DecodeParams) o;
                instructionView.setText(decodeParams.getInstructionString());

                if(isVisible) {
                    launchAsynchronousOnStepCompleted();
                }
            }
            else if(o instanceof ObservableDecoderUnit.InvalidateParams) {
                instructionView.setText(null);
            }
            else if(o instanceof Boolean){ // update is from a reset
                instructionView.setText(null);
                if(isVisible) {
                    responder.onResetAnimationEnd();
                }
            }
        }
        else if(observable instanceof ObservableComputeAltCore){
            if(o instanceof ObservableComputeAltCore.ExecuteParams) {
                CompCore core = ((ObservableComputeAltCore) observable);
                ALU alu = core.getALU();
                aluStateView.setText(alu.statusString());

                DecoderUnit decoderUnit = core.getDecoderUnit();
                if(isVisible && !decoderUnit.isDataMemoryInstruction()) {
                    launchAsynchronousOnStepCompleted();
                }
            }
            else if(o instanceof ObservableComputeAltCore.InterruptParams) {
                if(isVisible) {
                    launchAsynchronousOnStepCompleted();
                }
            }
            else if(o instanceof Boolean){ // update is from a reset
                aluStateView.setText(null);
            }
        }
    }

    public void launchAsynchronousOnStepCompleted() {
        // Launch thread to ensure that responder.onStepAnimationEnd() is called after
        // InspectActivity.advanceTime() completes
        (new Thread(new Runnable() {
            @Override
            public void run() {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        responder.onStepAnimationEnd();
                    }
                }, 1);
            }
        })).start();
    }

    public void setViewVisibility(boolean visible){
        isVisible = visible;
    }

    public void setActionResponder(InspectActionResponder resp){
        responder = resp;
    }

    private void updateState(String text) {
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

        stateView.setText(getResources().getString(resId));
    }

    private void updateIrText(){
        ir.setText(irText); // only update ir when read finished
    }
}
