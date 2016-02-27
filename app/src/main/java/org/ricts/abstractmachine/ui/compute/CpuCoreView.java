package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.ui.storage.RamView;
import org.ricts.abstractmachine.ui.storage.ReadPortView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 18/01/15.
 */
public class CpuCoreView extends RelativeLayout implements Observer {
    private TextView pc, ir;
    private TextView stateView, instructionView;
    private String irText;
    private boolean updateIrImmediately;


    /** Standard Constructors **/
    public CpuCoreView(Context context) {
        this(context, null);
    }

    public CpuCoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CpuCoreView(Context context, AttributeSet attrs, int defStyle) {
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

        /*** Initialise other vars ***/
        updateIrImmediately = false;
    }

    public void initCpu(ControlUnit controlUnit, RamView mainMemory){
        /** initialise variables **/
        stateView.setText(controlUnit.currentState().getName());
        pc.setText(controlUnit.getPcReg().dataString());
        ir.setText(controlUnit.getIrReg().dataString());

        /** setup callback behaviour **/
        mainMemory.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                updateIrText(); // only update ir when read finished
            }

            @Override
            public void onReadStart() {

            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableControlUnit){
            ControlUnit controlUnit = ((ObservableControlUnit) observable).getType();

            stateView.setText(controlUnit.currentState().getName());
            pc.setText(controlUnit.getPcReg().dataString());
            irText = controlUnit.getIrReg().dataString();
            if(updateIrImmediately)
                updateIrText();
        }
        else if(observable instanceof ObservableComputeCore){
            if(o instanceof ObservableComputeCore.ExecuteParams) {
                ObservableComputeCore.ExecuteParams params = (ObservableComputeCore.ExecuteParams) o;
                ComputeCore core = (ComputeCore) ((ObservableComputeCore) observable).getType();

                instructionView.setText(core.instrString(params.getInstruction()));
            }
        }
    }

    public void setUpdateIrImmediately(boolean immediately){
        updateIrImmediately = immediately;
    }

    private void updateIrText(){
        ir.setText(irText); // only update ir when read finished
    }
}
