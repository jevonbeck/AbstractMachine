package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.ui.storage.RamView;
import org.ricts.abstractmachine.ui.storage.RegDataView;

/**
 * Created by Jevon on 18/01/15.
 */
public class CpuCoreView extends RelativeLayout implements ThreadProcessingUnit {
    private RegDataView pc; // Program Counter
    private RegDataView ir; // Instruction Register
    private TextView stateView, instructionView;

    private ControlUnit cu; // Control Unit
    private ComputeCore mainCore;

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
        /*** init properties ***/
        setBackgroundColor(context.getResources().getColor(R.color.reg_data_unselected));
        int padding = (int) (10 * scaleFactor);
        setPadding(padding, padding, padding, padding);

        /*** create children ***/
        pc = new RegDataView(context);
        pc.setBackgroundColor(context.getResources().getColor(R.color.test_color));
        pc.setTextColor(context.getResources().getColor(android.R.color.white));

        ir = new RegDataView(context);
        ir.setId(R.id.cpucoreview_irview);
        ir.setBackgroundColor(context.getResources().getColor(R.color.test_color));
        ir.setTextColor(context.getResources().getColor(android.R.color.white));

        TextView pcLabel = new TextView(context);
        pcLabel.setId(R.id.cpucoreview_pclabel);
        pcLabel.setTypeface(Typeface.MONOSPACE);
        pcLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        pcLabel.setText("PC");

        TextView irLabel = new TextView(context);
        irLabel.setId(R.id.cpucoreview_irlabel);
        irLabel.setTypeface(Typeface.MONOSPACE);
        irLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        irLabel.setText("IR");

        TextView stateLabel = new TextView(context);
        stateLabel.setId(R.id.cpucoreview_statelabel);
        stateLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        stateLabel.setText("State:");

        stateView = new TextView(context);
        stateView.setTextColor(context.getResources().getColor(android.R.color.white));
        stateView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

        TextView instructionLabel = new TextView(context);
        instructionLabel.setId(R.id.cpucoreview_instuctionlabel);
        instructionLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        instructionLabel.setText("Ins:");

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
    }

    public void initCpu(ComputeCore core, RamView mainMemory){
        mainCore = core;

        pc.setDataWidth(mainCore.iAddrWidth());
        ir.setDataWidth(mainCore.instrWidth());
        mainMemory.setAnimationResponder(ir);
        ir.setDelayEnable(true);

        cu = new ControlUnit(pc, ir, mainCore, mainMemory, mainMemory);

        setStartExecFrom(0);
    }

    @Override
    public void setStartExecFrom(int currentPC){
        pc.write(currentPC);
        cu.setToFetchState();
        stateView.setText(cu.getCurrentState().name());
    }

    @Override
    public int nextActionTransitionTime(){
        return cu.nextActionDuration();
    }

    @Override
    public void triggerNextAction(){
        cu.performNextAction(); // perform action for 'currentState' and go to next state
        stateView.setText(cu.getCurrentState().name());

        if(!cu.isAboutToExecute()){
            instructionView.setText(mainCore.instrString(ir.read()));
        }
    }
}
