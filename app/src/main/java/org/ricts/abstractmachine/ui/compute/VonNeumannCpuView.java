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
public class VonNeumannCpuView extends RelativeLayout implements ThreadProcessingUnit {
    private RegDataView pc; // Program Counter
    private RegDataView ir; // Instruction Register
    private TextView stateView;

    private ControlUnit cu; // Control Unit

    /** Standard Constructors **/
    public VonNeumannCpuView(Context context) {
        super(context);

        init(context);
    }

    public VonNeumannCpuView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public VonNeumannCpuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context) {
        float scaleFactor = context.getResources().getDisplayMetrics().density;
        /*** init properties ***/
        setBackgroundColor(context.getResources().getColor(R.color.reg_data_unselected));
        int padding = (int) (10 * scaleFactor);
        setPadding(padding, padding, padding, padding);

        /*** create children ***/
        pc = new RegDataView(context);
        //pc.setId(R.id.vonneumanncpuview_pcview);
        pc.setBackgroundColor(context.getResources().getColor(R.color.test_color));
        pc.setTextColor(context.getResources().getColor(android.R.color.white));

        ir = new RegDataView(context);
        ir.setId(R.id.vonneumanncpuview_irview);
        ir.setBackgroundColor(context.getResources().getColor(R.color.test_color));
        ir.setTextColor(context.getResources().getColor(android.R.color.white));

        TextView pcLabel = new TextView(context);
        pcLabel.setId(R.id.vonneumanncpuview_pclabel);
        pcLabel.setTypeface(Typeface.MONOSPACE);
        pcLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        pcLabel.setText("PC");

        TextView irLabel = new TextView(context);
        irLabel.setId(R.id.vonneumanncpuview_irlabel);
        irLabel.setTypeface(Typeface.MONOSPACE);
        irLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        irLabel.setText("IR");

        TextView stateLabel = new TextView(context);
        stateLabel.setId(R.id.vonneumanncpuview_statelabel);
        stateLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        stateLabel.setText("State:");

        stateView = new TextView(context);
        //stateView.setId(R.id.vonneumanncpuview_stateview);
        stateView.setTextColor(context.getResources().getColor(android.R.color.white));
        stateView.setBackgroundColor(context.getResources().getColor(R.color.test_color2));

        /*** determine children layouts and positions based on attributes ***/
        int viewWidth = (int) (70 * scaleFactor);

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
    }

    public void initCpu(ComputeCore core, RamView mainMemory){
        pc.setDataWidth(core.iAddrWidth());
        ir.setDataWidth(core.instrWidth());
        mainMemory.setAnimationResponder(ir);
        ir.setDelayEnable(true);

        cu = new ControlUnit(pc, ir, core, mainMemory, mainMemory);

        setStartExecFrom(0);
    }

    @Override
    public void setStartExecFrom(int currentPC){
        pc.write(currentPC);
        cu.setToFetchState();
        stateView.setText("fetch");
    }

    @Override
    public int nextActionTransitionTime(){
        return cu.nextActionDuration();
    }

    @Override
    public void triggerNextAction(){
        cu.performNextAction(); // perform action for 'currentState' and go to next state

        if(cu.isAboutToExecute()){
            stateView.setText("execute");
        }
        else{
            stateView.setText("fetch");
        }
    }
}
