package org.ricts.abstractmachine.components.compute.core;

import android.content.res.Resources;

import org.ricts.abstractmachine.components.compute.interrupt.InterruptSource;
import org.ricts.abstractmachine.components.compute.interrupt.InterruptTarget;
import org.ricts.abstractmachine.components.compute.isa.InstructionGroupDecoder;
import org.ricts.abstractmachine.components.compute.isa.IsaDecoder;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;

public abstract class ComputeCore extends Device implements ComputeCoreInterface, InterruptTarget {
	protected IsaDecoder instrDecoder;

	protected int instrWidth;  
	protected int instrBitMask;
	protected int iAddrWidth;
	protected int dAddrWidth;
	protected int dataWidth;

    protected ControlUnitInterface cu;
    protected Resources resources;

    private boolean pcUpdated = false;
    private ControlUnitState internalControlUnitState = ControlUnitState.ACTIVE;

    private InterruptSource [] interruptSources;

    protected enum ControlUnitState {
        ACTIVE, SLEEP, HALT
    }

    public abstract String [] getMneumonicList();
    public abstract String [] getOperandLabels(String mneumonic);
    public abstract String getDescription(String mneumonic);
    public abstract String getInstructionFormat(String mneumonic);
    public abstract OperandInfo[] getOperandInfoArray(String mneumonic);
    public abstract OperandInfo getDataOperandInfo();
    public abstract OperandInfo getDataRegOperandInfo();
    public abstract OperandInfo getDataAddrOperandInfo();
    public abstract OperandInfo getInstrAddrOperandInfo();
    public abstract int getProgramCounterValue();
    public abstract void reset();

    protected abstract boolean isDataMemInstr(String groupName, int groupIndex);
    protected abstract boolean isHaltInstr(String groupName, int groupIndex);
    protected abstract boolean isSleepInstr(String groupName, int groupIndex);
    protected abstract void fetchOpsExecuteInstr(String groupName, int groupIndex, int[] operands);
    protected abstract void vectorToInterruptHandler();
    protected abstract int executionTime(String groupName, int groupIndex);
    protected abstract void updateProgramCounterRegs(int programCounter);
    protected abstract InterruptSource [] createInterruptSources();
    protected abstract String insToString(String groupName, int groupIndex, int[] operands);
    protected abstract String getGroupName(String mneumonic);
    protected abstract String nopMneumonic();

    public ComputeCore(Resources res) {
        resources = res;
        interruptSources = createInterruptSources();
    }

	@Override
	public int instrWidth(){
		return instrWidth;
	} 
  
	@Override
	public int dAddrWidth(){
		return dAddrWidth;
	}
  
	@Override
	public int iAddrWidth(){
		return iAddrWidth;
	}
  
	@Override
	public int dataWidth(){
		return dataWidth;
	}

    @Override
    public void executeInstruction(int programCounter, int instruction) {
        updateProgramCounterRegs(programCounter);
        latchInterruptSources();

		int instruct = instruction & instrBitMask;
		if(instrDecoder.isValidInstruction(instruct)){
			// decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruct);

			String groupName = decoder.groupName();
			int groupIndex = decoder.decode(instruct);
			
			int[] operands = new int [decoder.operandCount()];
			for(int x=0; x != operands.length; ++x){
				operands[x] = decoder.getOperand(x, instruct);
			}

			// fetch/indirect operands and execute instruction
            getOpsExecuteInstruction(groupName, groupIndex, operands);

            // apply changes to Control Unit as appropriate
            switch (internalControlUnitState){
                case ACTIVE:
                    // check for interrupts and vector internal Program Counter appropriately
                    int pcValueAfterExecute = getProgramCounterValue();
                    vectorToInterruptHandler();
                    int finalPC = getProgramCounterValue();

                    boolean interruptOccurred = pcValueAfterExecute != finalPC;
                    if(pcUpdated || interruptOccurred){
                        writeToControlUnit(cu);
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
        else {
            cu.setNextStateToHalt();
        }
	}

    @Override
    public int instrExecTime(int instruction) {
		int instruct = instruction & instrBitMask;
		if(instrDecoder.isValidInstruction(instruct)){
			// decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruct);

            String groupName = decoder.groupName();
            int groupIndex = decoder.decode(instruct);
						
			// determine execute time for instruction
			return executionTime(groupName, groupIndex);
		}
		
		return -1;
	}

    @Override
    public void checkInterrupts() {
        latchInterruptSources();
        int before = getProgramCounterValue();
        vectorToInterruptHandler();
        int after = getProgramCounterValue();

        if(before != after){
            writeToControlUnit(cu);
        }
    }

    @Override
    public int getNopInstruction() {
        return encodeInstruction(nopMneumonic(), new int [0]);
    }

    @Override
    public void setControlUnit(ControlUnitInterface controlUnit) {
        cu = controlUnit;
    }

    public boolean isHaltInstruction(int instruction) {
        int instruct = instruction & instrBitMask;
        if(instrDecoder.isValidInstruction(instruct)){
            // decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruct);

            String groupName = decoder.groupName();
            int groupIndex = decoder.decode(instruct);

            // determine if instruction halts the CPU
            return isHaltInstr(groupName, groupIndex);
        }
        return true;
    }

    public boolean isSleepInstruction(int instruction) {
        if(instrDecoder.isValidInstruction(instruction)){
            // decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruction);

            String groupName = decoder.groupName();
            int groupIndex = decoder.decode(instruction);

            // determine if instruction halts the CPU
            return isSleepInstr(groupName, groupIndex);
        }
        return false;
    }
    
    public boolean isDataMemoryInstruction(int instruction){
        int instruct = instruction & instrBitMask;
        if(instrDecoder.isValidInstruction(instruct)){
            // decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruct);

            String groupName = decoder.groupName();
            int groupIndex = decoder.decode(instruct);

            // determine if instruction accesses data memory
            return isDataMemInstr(groupName, groupIndex);
        }
        return false;
    }

    public String instrString(int instruction) {
        int instruct = instruction & instrBitMask;
        if(instrDecoder.isValidInstruction(instruct)){
            // decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruct);

            String groupName = decoder.groupName();
            int groupIndex = decoder.decode(instruct);

            int[] operands = new int [decoder.operandCount()];
            for(int x=0; x != operands.length; ++x){
                operands[x] = decoder.getOperand(x, instruct);
            }

            // print string version of instruction
            return insToString(groupName, groupIndex, operands);
        }
        return "Ins invalid!";
    }

    public String instrValueString(int instruction) {
        return formatNumberInHex(instruction, instrWidth);
    }

    public String instrAddrValueString(int address) {
        return formatNumberInHex(address, iAddrWidth);
    }

    public String dataValueString(int data) {
        return formatNumberInHex(data, dataWidth);
    }

    public String dataAddrValueString(int address) {
        return formatNumberInHex(address, dAddrWidth);
    }

    public int encodeInstruction(String iMneumonic, int [] operands) {
		return instrDecoder.encode(getGroupName(iMneumonic), iMneumonic, operands);
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

    private void writeToControlUnit(ControlUnitInterface cu){
        int newPcValue = getProgramCounterValue();
        if(cu.isPipelined()){
            cu.setNextFetchAndExecute(newPcValue, getNopInstruction());
        }
        else {
            cu.setNextFetch(newPcValue);
        }
    }
}
