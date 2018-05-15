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

    protected abstract boolean isDataMemInstr(String mneumonic);
    protected abstract boolean isHaltInstr(String mneumonic);
    protected abstract boolean isSleepInstr(String mneumonic);
    protected abstract void fetchOpsExecuteInstr(String mneumonic, int[] operands);
    protected abstract void vectorToInterruptHandler();
    protected abstract int executionTime(String mneumonic);
    protected abstract void updateProgramCounterRegs(int programCounter);
    protected abstract InterruptSource [] createInterruptSources();
    protected abstract String insToString(String mneumonic, int[] operands);
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

			String mneumonic = decoder.decode(instruct);
			
			int[] operands = new int [decoder.operandCount()];
			for(int x=0; x != operands.length; ++x){
				operands[x] = decoder.getOperand(x, instruct);
			}

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

            String mneumonic = decoder.decode(instruct);

            // determine execute time for instruction
			return executionTime(mneumonic);
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

            String mneumonic = decoder.decode(instruct);

            // determine if instruction halts the CPU
            return isHaltInstr(mneumonic);
        }
        return true;
    }

    public boolean isSleepInstruction(int instruction) {
        if(instrDecoder.isValidInstruction(instruction)){
            // decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruction);

            String mneumonic = decoder.decode(instruction);

            // determine if instruction halts the CPU
            return isSleepInstr(mneumonic);
        }
        return false;
    }
    
    public boolean isDataMemoryInstruction(int instruction){
        int instruct = instruction & instrBitMask;
        if(instrDecoder.isValidInstruction(instruct)){
            // decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruct);

            String mneumonic = decoder.decode(instruct);

            // determine if instruction accesses data memory
            return isDataMemInstr(mneumonic);
        }
        return false;
    }

    public String instrString(int instruction) {
        int instruct = instruction & instrBitMask;
        if(instrDecoder.isValidInstruction(instruct)){
            // decode instruction
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruct);

            String mneumonic = decoder.decode(instruct);

            int[] operands = new int [decoder.operandCount()];
            for(int x=0; x != operands.length; ++x){
                operands[x] = decoder.getOperand(x, instruct);
            }

            // print string version of instruction
            return insToString(mneumonic, operands);
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
		return instrDecoder.encode(iMneumonic, operands);
	}

    protected void updateProgramCounter(int programCounter){
        updateProgramCounterRegs(programCounter);
        pcUpdated = true;
    }

    protected void setInternalControlUnitState(ControlUnitState state){
        internalControlUnitState = state;
    }

    private void getOpsExecuteInstruction(String mneumonic, int[] operands) {
        pcUpdated = false;
        setInternalControlUnitState(ControlUnitState.ACTIVE);
        fetchOpsExecuteInstr(mneumonic, operands);
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
