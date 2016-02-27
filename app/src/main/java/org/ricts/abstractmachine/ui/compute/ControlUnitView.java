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
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRegister;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;
import org.ricts.abstractmachine.ui.storage.ReadPortView;
import org.ricts.abstractmachine.ui.storage.RegDataView;

import java.util.Observable;
import java.util.Observer;

public class ControlUnitView extends RelativeLayout implements Observer{
    private TextView pc; // Program Counter
    private TextView ir; // Instruction Register
    private TextView stateView; // Control Unit state

    private String pcText, irText, stateText;
    private boolean updateImmediately;

    public ControlUnitView(Context context) {
        this(context, null);
    }

    public ControlUnitView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlUnitView(Context context, AttributeSet attrs, int defStyle) {
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
    public void update(Observable observable, Object o) {
        ControlUnit controlUnit = ((ObservableControlUnit) observable).getType();

        stateText = controlUnit.currentState().getName();
        pcText = controlUnit.getPcReg().dataString();
        irText = controlUnit.getIrReg().dataString();

        if(updateImmediately){
            updatePC();
            updateIR();
            updateState();
        }
        else {
            if(!controlUnit.isAboutToHalt()){
                updateState();
            }

            if(controlUnit.isAboutToExecute()){
                updatePC();
            }
        }
    }

    public void initCU(ControlUnit controlUnit, ComputeCoreView coreView,
                       MemoryPortView instructionCache){
        /** initialise variables **/
        stateView.setText(controlUnit.currentState().getName());
        pc.setText(controlUnit.getPcReg().dataString());
        ir.setText(controlUnit.getIrReg().dataString());

        /** setup callback behaviour **/
        instructionCache.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                updateIR(); // only update ir when read finished
            }

            @Override
            public void onReadStart() {

            }
        });

        coreView.setUpdatePcResponder(new ComputeCoreView.PinsView.UpdateResponder() {
            @Override
            public void onUpdatePcCompleted() {
                updatePC();
            }
        });

        coreView.setHaltCommandResponder(new ComputeCoreView.HaltResponder() {
            @Override
            public void onHaltCompleted() {
                updateState();
            }
        });
    }

    public void setUpdateImmediately(boolean immediately){
        updateImmediately = immediately;
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
