package org.ricts.abstractmachine.ui.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.devices.compute.core.BasicScalar;
import org.ricts.abstractmachine.devices.compute.core.BasicScalarEnums;
import org.ricts.abstractmachine.ui.storage.RamView;
import org.ricts.abstractmachine.ui.storage.RegDataView;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private RegDataView pc; // Program Counter
    private RegDataView ir; // Instruction Register
    private ControlUnit cu; // Control Unit

    private RamView memory;

    private Button advanceButton;
    private TextView sysClockTextView, stateTextView;

    private int sysClock; // system clock

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        int byteMultiplierWidth = 0;
        int dAdWidth = 3;
        int iAdWidth = 3;

        int stkAdWidth = 3;
        int dRegAdWidth = 3;
        int dAdrRegAdWidth = 1;
        int iAdrRegAdWidth = 1;

        BasicScalar core = new BasicScalar(byteMultiplierWidth, dAdWidth, iAdWidth,
                stkAdWidth,dRegAdWidth, dAdrRegAdWidth, iAdrRegAdWidth);

        memory = (RamView) findViewById(R.id.memory);

        pc = (RegDataView) findViewById(R.id.pcRegTextView);
        ir = (RegDataView) findViewById(R.id.irRegTextView);

        sysClockTextView = (TextView) findViewById(R.id.sysClockText);
        stateTextView = (TextView) findViewById(R.id.stateTextView);

        advanceButton = (Button) findViewById(R.id.stepButton);
        advanceButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0) {
                advanceTime();
            }
        });

        memory.initMemory(core.instrWidth(), core.iAddrWidth(), 10);

        ArrayList<Integer> memData = new ArrayList<Integer>();
        int [] operands;

        // JUMP 0x2
        operands = new int[1];
        operands[0] = 2;
        memData.add(core.encodeInstruction(BasicScalarEnums.InstrAddressLiteral.enumName(),
                BasicScalarEnums.InstrAddressLiteral.JUMP.name(), operands));

        memData.add(core.nopInstruction()); // NOP instruction

        // LOAD R3, 1
        operands = new int[2];
        operands[0] = 3;
        operands[1] = 1;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataAssignLit.enumName(),
                BasicScalarEnums.DataAssignLit.LOAD.name(), operands));

        // LOAD R4, 7
        operands[0] = 4;
        operands[1] = 7;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataAssignLit.enumName(),
                BasicScalarEnums.DataAssignLit.LOAD.name(), operands));

        // STOREA R3, A0
        operands[0] = 3;
        operands[1] = 0;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataMemOps.enumName(),
                BasicScalarEnums.DataMemOps.STOREA.name(), operands));

        // ADD R5, R3, R4
        operands = new int[3];
        operands[0] = 5;
        operands[1] = 3;
        operands[2] = 4;
        memData.add(core.encodeInstruction(BasicScalarEnums.AluOps.enumName(),
                BasicScalarEnums.AluOps.ADD.name(), operands));

        // STOREM R5, A0
        operands = new int[2];
        operands[0] = 5;
        operands[1] = 0;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataMemOps.enumName(),
                BasicScalarEnums.DataMemOps.STOREM.name(), operands));

        memory.setMemoryData(memData, 0);

        pc.setDataWidth(core.iAddrWidth());
        ir.setDataWidth(core.instrWidth());
        memory.setReadResponder(ir);
        ir.setDelayEnable(true);

        cu = new ControlUnit(pc, ir, core, memory, memory);

        Log.d(TAG, "instruction width = " + core.instrWidth());
        Toast.makeText(this, "instruction width = " + core.instrWidth(), Toast.LENGTH_SHORT).show();

        setStartExecFrom(0);
        sysClock = 0;

        sysClockTextView.setText(""+sysClock);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setStartExecFrom(int currentPC){
        pc.write(currentPC);
        cu.setToFetchState();
        stateTextView.setText("fetch");
    }

    public void advanceTime(){
        int result = cu.nextActionDuration();
        cu.performNextAction(); // perform action for 'currentState' and go to next state

        if(cu.isAboutToExecute()){
            stateTextView.setText("execute");
        }
        else{
            stateTextView.setText("fetch");
        }

        sysClock += result;
        sysClockTextView.setText(""+sysClock);
    }
}
