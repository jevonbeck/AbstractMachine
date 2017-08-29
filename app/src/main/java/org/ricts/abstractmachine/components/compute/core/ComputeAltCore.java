package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.interrupt.InterruptSource;
import org.ricts.abstractmachine.components.compute.interrupt.InterruptTarget;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.ALU;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;

public abstract class ComputeAltCore extends Device implements CompCore, InterruptTarget {
    protected ControlUnitInterface cu;
    protected AluCore aluCore;

    private DecoderUnit decoderCore;
    private boolean pcUpdated = false;
    private boolean cuUpdated = false;
    private ControlUnitState internalControlUnitState = ControlUnitState.ACTIVE;
    private InterruptSource [] interruptSources;

    protected enum ControlUnitState {
        ACTIVE, SLEEP, HALT
    }

    protected abstract AluCore createALU(int dataWidth);
    protected abstract void fetchOpsExecuteInstr(String groupName, int groupIndex, int[] operands);
    protected abstract void vectorToInterruptHandler();
    protected abstract void updateProgramCounterRegs(int programCounter);
    protected abstract InterruptSource [] createInterruptSources();

    public ComputeAltCore(DecoderUnit decoder) {
        decoderCore = decoder;
        aluCore = createALU(decoderCore.dataWidth());
        interruptSources = createInterruptSources();
    }

    @Override
    public void executeInstruction(int programCounter, String instructionGroupName,
                                   int instructionGroupIndex, int[] operands) {
        cuUpdated = false;
        updateProgramCounterRegs(programCounter);
        latchInterruptSources();

        // fetch/indirect operands and execute instruction
        getOpsExecuteInstruction(instructionGroupName, instructionGroupIndex, operands);

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

    @Override
    public ALU getALU() {
        return aluCore;
    }

    protected void updateProgramCounter(int programCounter){
        updateProgramCounterRegs(programCounter);
        pcUpdated = true;
    }

    protected void setInternalControlUnitState(ControlUnitState state){
        internalControlUnitState = state;
    }

    private void getOpsExecuteInstruction(String groupName, int groupIndex, int[] operands) {
        pcUpdated = false;
        setInternalControlUnitState(ControlUnitState.ACTIVE);
        fetchOpsExecuteInstr(groupName, groupIndex, operands);
    }

    private void latchInterruptSources() {
        for(InterruptSource source : interruptSources) {
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
}
