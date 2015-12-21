package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.ui.storage.RegDataView;

public class ControlUnitView extends RelativeLayout implements ControlUnitInterface {
    private RegDataView pc; // Program Counter
    private RegDataView ir; // Instruction Register
    private TextView stateView;
    private ControlUnit cu;

    public ControlUnitView(Context context) {
        this(context, null);
    }

    public ControlUnitView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlUnitView(Context context, AttributeSet attrs, int defStyle) {
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
        pc.setUpdateImmediately(false);

        ir = new RegDataView(context);
        ir.setId(R.id.ControlUnitView_ir_view);
        ir.setBackgroundColor(context.getResources().getColor(R.color.test_color));
        ir.setTextColor(context.getResources().getColor(android.R.color.white));
        ir.setUpdateImmediately(false);

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
        stateView.setTextColor(context.getResources().getColor(android.R.color.white));
        stateView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

        /*** determine children layouts and positions ***/
        int viewWidth = (int) (110 * scaleFactor);

        RelativeLayout.LayoutParams lpPcLabel = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        addView(pcLabel, lpPcLabel);

        RelativeLayout.LayoutParams lpPcView = new RelativeLayout.LayoutParams(
                viewWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpPcView.addRule(RelativeLayout.RIGHT_OF, pcLabel.getId());
        lpPcView.addRule(RelativeLayout.ALIGN_TOP, pcLabel.getId());
        addView(pc, lpPcView);

        RelativeLayout.LayoutParams lpIrLabel = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpIrLabel.addRule(RelativeLayout.BELOW, pcLabel.getId());
        lpIrLabel.addRule(RelativeLayout.ALIGN_LEFT, pcLabel.getId());
        addView(irLabel, lpIrLabel);

        RelativeLayout.LayoutParams lpIrView = new RelativeLayout.LayoutParams(
                viewWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpIrView.addRule(RelativeLayout.RIGHT_OF, irLabel.getId());
        lpIrView.addRule(RelativeLayout.ALIGN_TOP, irLabel.getId());
        addView(ir, lpIrView);

        RelativeLayout.LayoutParams lpStateLabel = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpStateLabel.addRule(RelativeLayout.BELOW, irLabel.getId());
        addView(stateLabel, lpStateLabel);

        RelativeLayout.LayoutParams lpStateView = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpStateView.addRule(RelativeLayout.RIGHT_OF, stateLabel.getId());
        lpStateView.addRule(RelativeLayout.ALIGN_TOP, stateLabel.getId());
        lpStateView.addRule(RelativeLayout.ALIGN_RIGHT, ir.getId());
        addView(stateView, lpStateView);
    }

    @Override
    public void setToFetchState() {
        cu.setToFetchState();
        updateStateView();
    }

    @Override
    public void setToExecuteState() {
        cu.setToExecuteState();
        updateStateView();
    }

    @Override
    public void performNextAction() {
        cu.performNextAction();
        updateStateView();
    }

    @Override
    public boolean isAboutToExecute() {
        return cu.isAboutToExecute();
    }

    @Override
    public int nextActionDuration(){
        return cu.nextActionDuration();
    }

    @Override
    public int getPC() {
        return cu.getPC();
    }

    @Override
    public void setPC(int currentPC) {
        cu.setPC(currentPC);
    }

    @Override
    public void setIR(int currentIR) {
        cu.setIR(currentIR);
    }

    public void initCU(ComputeCoreInterface core, ReadPort instructionCache, MemoryPort dataMemory){
        cu = new ControlUnit(pc,  ir, core, instructionCache, dataMemory);
        pc.setDataWidth(core.iAddrWidth());
        ir.setDataWidth(core.instrWidth());

        updateStateView();
    }

    public void updatePcView(){
        pc.updateDisplayText();
    }

    public void updateIrView(){
        ir.updateDisplayText();
    }

    private void updateStateView(){
        stateView.setText(cu.getCurrentState());
    }
}
