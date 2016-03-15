package org.ricts.abstractmachine.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.system.SystemArchitecture;
import org.ricts.abstractmachine.components.system.VonNeumannArchitecture;
import org.ricts.abstractmachine.devices.compute.core.BasicScalar;
import org.ricts.abstractmachine.devices.compute.core.BasicScalarEnums;
import org.ricts.abstractmachine.ui.fragments.VonNeumannCoreFragment;
import org.ricts.abstractmachine.ui.fragments.VonNeumannSystemFragment;

import java.util.ArrayList;

public class VonNeumannActivity extends InspectActivity {

    @Override
    protected SystemArchitecture createSystemArchitecture(ComputeCore core, Bundle options) {
        return new VonNeumannArchitecture(core, 10);
    }

    @Override
    protected void initSystemArchitecture(SystemArchitecture architecture, Bundle options) {
        ComputeCore core = (ComputeCore) architecture.getComputeCore().getType();

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

        VonNeumannArchitecture vonNeumannArchitecture = (VonNeumannArchitecture) architecture;
        vonNeumannArchitecture.initMemory(memData, 0);
    }

    @Override
    protected PagerAdapter createAdapter(SystemArchitecture architecture) {
        return new SystemViewAdapter(getSupportFragmentManager(), (VonNeumannArchitecture) architecture);
    }

    private static class SystemViewAdapter extends FragmentPagerAdapter {
        private static final String TAG = "SystemViewAdapter";

        private ObservableComputeCore mainCore;
        private ObservableRAM mainMemory;
        private ObservableControlUnit cu;

        public SystemViewAdapter(FragmentManager fm, VonNeumannArchitecture architecture) {
            super(fm);
            mainCore = architecture.getComputeCore();
            mainMemory = architecture.getMainMemory();
            cu = architecture.getControlUnit();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return VonNeumannSystemFragment.newInstance(mainCore, mainMemory, cu);
                case 1:
                    return VonNeumannCoreFragment.newInstance(mainCore, mainMemory, cu);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public String getPageTitle(int position){
            switch (position){
                case 0:
                    return "System";
                case 1:
                    return "Core";
                default:
                    return null;
            }
        }
    }
}
