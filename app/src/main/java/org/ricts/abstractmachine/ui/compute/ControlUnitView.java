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
    private RegDataView pc; // Program Counter
    private RegDataView ir; // Instruction Register
    private FSMView stateView; // Control Unit state

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
        pc = new RegDataView(context);
        pc.setId(R.id.ControlUnitView_pc_view);
        pc.setBackgroundColor(context.getResources().getColor(R.color.test_color));
        pc.setUpdateImmediately(false);

        ir = new RegDataView(context);
        ir.setId(R.id.ControlUnitView_ir_view);
        ir.setBackgroundColor(context.getResources().getColor(R.color.test_color));
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

        stateView = new FSMView(context);
        stateView.setId(R.id.ControlUnitView_fsm_view);
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
        if(observable instanceof ObservableControlUnit){
            ObservableControlUnit observed = (ObservableControlUnit) observable;
            ControlUnit controlUnit = observed.getType();

            if(controlUnit.isAboutToExecute()){
                // update immediately when state changes to 'execute' state
                stateView.setUpdateImmediately(true);
                stateView.update(observable, o);
                stateView.setUpdateImmediately(false);
            }
        }
    }

    public void initCU(ObservableControlUnit controlUnit, ComputeCoreView coreView,
                       MemoryPortView instructionCache){
        ObservableRegister obsPC = controlUnit.getType().getPcReg();
        ObservableRegister obsIR = controlUnit.getType().getIrReg();

        /** Add observers to observables **/
        controlUnit.addObserver(stateView);
        obsPC.addObserver(pc);
        obsIR.addObserver(ir);

        /** init displayable values **/
        stateView.setUpdateImmediately(true);
        stateView.update(controlUnit, null);
        stateView.setUpdateImmediately(false);

        pc.setUpdateImmediately(true);
        pc.update(obsPC, null);
        pc.setUpdateImmediately(false);

        ir.setUpdateImmediately(true);
        ir.update(obsIR, null);
        ir.setUpdateImmediately(false);

        /** setup callback behaviour **/
        instructionCache.setReadResponder(new ReadPortView.ReadResponder() {
            @Override
            public void onReadFinished() {
                ir.updateDisplayText();
                //pc.setUpdateImmediately(false);
            }

            @Override
            public void onReadStart() {
                // TODO: verify working
                //pc.setUpdateImmediately(true);
                pc.updateDisplayText();
            }
        });

        coreView.setUpdatePcResponder(new ComputeCoreView.PinsView.UpdateResponder() {
            @Override
            public void onUpdatePcCompleted() {
                pc.updateDisplayText();
                //stateView.updateDisplayText();
            }
        });

        coreView.setHaltCommandResponder(new ComputeCoreView.HaltResponder() {
            @Override
            public void onHaltCompleted() {
                stateView.updateDisplayText();
            }
        });
    }
}
