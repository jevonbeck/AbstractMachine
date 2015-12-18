package org.ricts.abstractmachine.ui.compute;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

/**
 * Created by Jevon on 18/12/2015.
 */
public class ComputeCoreView extends RelativeLayout implements ComputeCoreInterface {
    private ComputeCore mainCore;

    private TextView instructionView;

    public ComputeCoreView(Context context) {
        this(context, null);
    }

    public ComputeCoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComputeCoreView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        int viewWidth = (int) (80 * scaleFactor);

        LayoutParams lpInstructionLabel = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(instructionLabel, lpInstructionLabel);

        LayoutParams lpInstructionView = new LayoutParams(
                viewWidth, LayoutParams.WRAP_CONTENT);
        lpInstructionView.addRule(RelativeLayout.RIGHT_OF, instructionLabel.getId());
        lpInstructionView.addRule(RelativeLayout.ALIGN_TOP, instructionLabel.getId());
        addView(instructionView, lpInstructionView);
    }

    public void setComputeCore(ComputeCore core){
        mainCore = core;
    }

    @Override
    public void executeInstruction(int instruction, MemoryPort dataMemory, RegisterPort PC) {
        instructionView.setText(mainCore.instrString(instruction));
        mainCore.executeInstruction(instruction, dataMemory, PC);
    }

    @Override
    public int instrExecTime(int instruction, MemoryPort dataMemory) {
        return mainCore.instrExecTime(instruction, dataMemory);
    }

    @Override
    public boolean isHaltInstruction(int instruction) {
        return mainCore.isHaltInstruction(instruction);
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
}
