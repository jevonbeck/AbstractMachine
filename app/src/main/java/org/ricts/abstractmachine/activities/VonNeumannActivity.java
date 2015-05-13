package org.ricts.abstractmachine.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.devices.compute.core.BasicScalar;
import org.ricts.abstractmachine.devices.compute.core.BasicScalarEnums;
import org.ricts.abstractmachine.ui.compute.CpuCoreView;
import org.ricts.abstractmachine.ui.storage.RamView;

import java.util.ArrayList;

public class VonNeumannActivity extends ActionBarActivity {
    private static final String TAG = "VonNeumannActivity";

    private CpuCoreView cpu;
    private TextView sysClockTextView;
    private int sysClock; // system clock

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_von_neumann);

        int clockFreq = 1;
        int byteMultiplierWidth = 0;
        int dAdWidth = 3;
        int iAdWidth = 3;

        int stkAdWidth = 3;
        int dRegAdWidth = 3;
        int dAdrRegAdWidth = 1;
        int iAdrRegAdWidth = 1;

        BasicScalar core = new BasicScalar(clockFreq, byteMultiplierWidth, dAdWidth, iAdWidth,
                stkAdWidth,dRegAdWidth, dAdrRegAdWidth, iAdrRegAdWidth);

        RamView memory = (RamView) findViewById(R.id.memory);

        cpu = (CpuCoreView) findViewById(R.id.cpuView);

        sysClockTextView = (TextView) findViewById(R.id.sysClockText);

        Button advanceButton = (Button) findViewById(R.id.stepButton);
        advanceButton.setOnClickListener(new View.OnClickListener() {
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

        // LOAD R3, 1 ; R3 <-- 1
        operands = new int[2];
        operands[0] = 3;
        operands[1] = 1;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataAssignLit.enumName(),
                BasicScalarEnums.DataAssignLit.LOAD.name(), operands));

        // LOAD R4, 7 ; R4 <-- 7
        operands[0] = 4;
        operands[1] = 7;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataAssignLit.enumName(),
                BasicScalarEnums.DataAssignLit.LOAD.name(), operands));

        // STOREA R3, A0 ; A0 <-- R3
        operands[0] = 3;
        operands[1] = 0;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataMemOps.enumName(),
                BasicScalarEnums.DataMemOps.STOREA.name(), operands));

        // ADD R5, R3, R4 ; R5 <-- R3 + R4
        operands = new int[3];
        operands[0] = 5;
        operands[1] = 3;
        operands[2] = 4;
        memData.add(core.encodeInstruction(BasicScalarEnums.AluOps.enumName(),
                BasicScalarEnums.AluOps.ADD.name(), operands));

        // STOREM R5, A0 ; MEM[A0] <-- R5
        operands = new int[2];
        operands[0] = 5;
        operands[1] = 0;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataMemOps.enumName(),
                BasicScalarEnums.DataMemOps.STOREM.name(), operands));

        memory.setMemoryData(memData, 0);
        cpu.initCpu(core, memory);

        Log.d(TAG, "instruction width = " + core.instrWidth());
        Toast.makeText(this, "instruction width = " + core.instrWidth(), Toast.LENGTH_SHORT).show();

        sysClock = 0;

        sysClockTextView.setText(""+sysClock);
    }

    private void advanceTime(){
        int result = cpu.nextActionTransitionTime();
        cpu.triggerNextAction(); // perform action for 'currentState' and go to next state

        sysClock += result;
        sysClockTextView.setText(""+sysClock);
    }
}
