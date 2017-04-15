package org.ricts.abstractmachine.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.devices.compute.core.BasicScalar;
import org.ricts.abstractmachine.ui.compute.CpuCoreView;
import org.ricts.abstractmachine.ui.storage.RamView;

import java.util.ArrayList;

public class TestActivity extends Activity {
    private CpuCoreView cpu;

    private TextView sysClockTextView;
    private int sysClock; // system clock

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        int byteMultiplierWidth = 0;
        int dAdWidth = 3;
        int iAdWidth = 3;

        int stkAdWidth = 3;
        int dRegAdWidth = 3;
        int dAdrRegAdWidth = 1;
        int iAdrRegAdWidth = 1;

        BasicScalar core = new BasicScalar(getResources(), byteMultiplierWidth, dAdWidth, iAdWidth,
                stkAdWidth,dRegAdWidth, dAdrRegAdWidth, iAdrRegAdWidth);

        ArrayList<Integer> memData = new ArrayList<Integer>();
        int [] operands;

        /*
        // JUMP 0x2
        operands = new int[1];
        operands[0] = 2;
        memData.add(core.encodeInstruction(BasicScalarEnums.InstrAddressLiteral.enumName(),
                BasicScalarEnums.InstrAddressLiteral.JUMP.name(), operands));

        memData.add(core.getNopInstruction()); // NOP instruction

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
        */
        RAM memory = new RAM(core.instrWidth(), core.iAddrWidth(), 10);
        memory.setData(memData, 0);

        // Setup Main UI
        RamView memoryView = (RamView) findViewById(R.id.memory);
        memoryView.setDataSource(memory);

        cpu = (CpuCoreView) findViewById(R.id.cpuView);
        //cpu.initCpu(core, memoryView);

        // Advance UI Setup
        sysClock = 0;
        sysClockTextView = (TextView) findViewById(R.id.sysClockText);
        sysClockTextView.setText(String.valueOf(sysClock));

        Button advanceButton = (Button) findViewById(R.id.stepButton);
        advanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                advanceTime();
            }
        });
    }

    private void advanceTime(){
        /*
        int result = cpu.nextActionTransitionTime();
        cpu.triggerNextAction();

        sysClock += result;
        sysClockTextView.setText(String.valueOf(sysClock));
        */
    }
}
