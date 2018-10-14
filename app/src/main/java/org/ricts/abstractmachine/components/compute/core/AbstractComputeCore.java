package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.interrupt.InterruptSource;
import org.ricts.abstractmachine.components.compute.interrupt.InterruptTarget;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;

public abstract class AbstractComputeCore extends Device implements ComputeCore, InterruptTarget {
    protected ControlUnitInterface cu;

    private DecoderUnit decoderCore;
    private boolean pcUpdated, cuUpdated;
    private ControlUnitState internalControlUnitState = ControlUnitState.ACTIVE;

    protected enum ControlUnitState {
        ACTIVE, SLEEP, HALT
    }

    protected abstract void fetchOpsExecuteInstr(String mneumonic, int[] operands);
    protected abstract void vectorToInterruptHandler();
    protected abstract void updateProgramCounterRegs(int programCounter);

    public AbstractComputeCore(DecoderUnit decoder) {
        decoderCore = decoder;
        resetUpdatedFlagsState();
    }

    @Override
    public void executeInstruction(int programCounter, String mneumonic, int[] operands) {
        resetUpdatedFlagsState();
        updateProgramCounterRegs(programCounter);
        latchInterruptSources();

        // fetch/indirect operands and execute instruction
        getOpsExecuteInstruction(mneumonic, operands);

        // apply changes to Control Unit as appropriate
        switch (internalControlUnitState){
            case ACTIVE:
                // check for interrupts and vector internal Program Counter appropriately
                int pcValueAfterExecute = getProgramCounterValue();
                vectorToInterruptHandler();
                int finalPC = getProgramCounterValue();

                boolean interruptOccurred = pcValueAfterExecute != finalPC;
                if(pcUpdated || interruptOccurred){
                    writeToControlUnit();
                }
                break;
            case SLEEP:
                cu.setNextStateToSleep();
                break;
            case HALT:
                cu.setNextStateToHalt();
                break;
        }
	}

    @Override
    public void checkInterrupts() {
        resetUpdatedFlagsState();
        latchInterruptSources();
        int before = getProgramCounterValue();
        vectorToInterruptHandler();
        int after = getProgramCounterValue();

        if(before != after){
            writeToControlUnit();
        }
    }

    @Override
    public void setControlUnit(ControlUnitInterface controlUnit) {
        cu = controlUnit;
    }

    @Override
    public boolean controlUnitUpdated() {
        return cuUpdated;
    }

    @Override
    public DecoderUnit getDecoderUnit() {
        return decoderCore;
    }

    protected InterruptSource [] getInterruptSources() {
        return new InterruptSource[0];
    }

    protected void updateProgramCounter(int programCounter){
        updateProgramCounterRegs(programCounter);
        pcUpdated = true;
    }

    protected void setInternalControlUnitState(ControlUnitState state){
        internalControlUnitState = state;
    }

    private void getOpsExecuteInstruction(String mneumonic, int[] operands) {
        setInternalControlUnitState(ControlUnitState.ACTIVE);
        fetchOpsExecuteInstr(mneumonic, operands);
    }

    private void latchInterruptSources() {
        for(InterruptSource source : getInterruptSources()) {
            source.updateTarget();
        }
    }

    private void writeToControlUnit(){
        cuUpdated = true;
        int newPcValue = getProgramCounterValue();
        if(cu.isPipelined()){
            cu.setNextFetchAndExecute(newPcValue, decoderCore.getNopInstruction());
        }
        else {
            cu.setNextFetch(newPcValue);
        }
    }

    private void resetUpdatedFlagsState() {
        pcUpdated = false;
        cuUpdated = false;
    }
}
