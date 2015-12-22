package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.devices.Device;
import org.ricts.abstractmachine.components.compute.isa.IsaDecoder;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;

public abstract class ComputeCore extends Device implements ComputeCoreInterface {
	protected IsaDecoder instrDecoder;
	protected String nopGroupName, nopMneumonic;
	
	protected int instrWidth;  
	protected int instrBitMask;
	protected int iAddrWidth;
	protected int dAddrWidth;
	protected int dataWidth;

    protected abstract boolean isDataMemInstr(String groupName, int enumOrdinal);
    protected abstract boolean isHaltInstr(String groupName, int enumOrdinal);
    protected abstract void fetchOpsExecuteInstr(String groupName, int enumOrdinal, int[] operands, MemoryPort dataMemory);
	protected abstract void updateProgramCounter(String groupName, int enumOrdinal, int[] operands, ControlUnitInterface cu);
    protected abstract int executionTime(String groupName, int enumOrdinal, MemoryPort dataMemory);
    protected abstract String insToString(String groupName, int enumOrdinal, int[] operands);

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
    public void executeInstruction(int instruction, MemoryPort dataMemory, ControlUnitInterface cu) {
		int instruct = instruction & instrBitMask;
		if(instrDecoder.isValidInstruction(instruct)){
			// decode instruction
			int decoderIndex = instrDecoder.getDecoderIndex(instruct);
			
			String groupName = instrDecoder.groupName(decoderIndex);
			int enumOrdinal = instrDecoder.decode(instruct, decoderIndex);
			
			int[] operands = new int [instrDecoder.operandCount(decoderIndex)];
			for(int x=0; x != operands.length; ++x){
				operands[x] = instrDecoder.getOperand(x, instruct, decoderIndex);
			}
						
			// fetch operands and execute instruction
			fetchOpsExecuteInstr(groupName, enumOrdinal, operands, dataMemory);

			// update Program Counter based on execution result
			updateProgramCounter(groupName, enumOrdinal, operands, cu);
		}
        else {
            cu.setToHaltState();
        }
	}

    @Override
    public int instrExecTime(int instruction, MemoryPort dataMemory) {
		int instruct = instruction & instrBitMask;
		if(instrDecoder.isValidInstruction(instruct)){
			// decode instruction
			int decoderIndex = instrDecoder.getDecoderIndex(instruct);
			
			String groupName = instrDecoder.groupName(decoderIndex);
			int enumOrdinal = instrDecoder.decode(instruct, decoderIndex);
						
			// determine execute time for instruction
			return executionTime(groupName, enumOrdinal, dataMemory);			
		}
		
		return -1;
	}

    public boolean isHaltInstruction(int instruction) {
        int instruct = instruction & instrBitMask;
        if(instrDecoder.isValidInstruction(instruct)){
            // decode instruction
            int decoderIndex = instrDecoder.getDecoderIndex(instruct);

            String groupName = instrDecoder.groupName(decoderIndex);
            int enumOrdinal = instrDecoder.decode(instruct, decoderIndex);

            // determine if instruction halts the CPU
            return isHaltInstr(groupName, enumOrdinal);
        }
        return true;
    }

    @Override
    public int nopInstruction() {
        return instrDecoder.encode(nopGroupName, nopMneumonic, new int [0]);
    }

    public boolean isDataMemoryInstruction(int instruction){
        int instruct = instruction & instrBitMask;
        if(instrDecoder.isValidInstruction(instruct)){
            // decode instruction
            int decoderIndex = instrDecoder.getDecoderIndex(instruct);

            String groupName = instrDecoder.groupName(decoderIndex);
            int enumOrdinal = instrDecoder.decode(instruct, decoderIndex);

            // determine if instruction accesses data memory
            return isDataMemInstr(groupName, enumOrdinal);
        }
        return false;
    }

    public String instrString(int instruction) {
        int instruct = instruction & instrBitMask;
        if(instrDecoder.isValidInstruction(instruct)){
            // decode instruction
            int decoderIndex = instrDecoder.getDecoderIndex(instruct);

            String groupName = instrDecoder.groupName(decoderIndex);
            int enumOrdinal = instrDecoder.decode(instruct, decoderIndex);

            int[] operands = new int [instrDecoder.operandCount(decoderIndex)];
            for(int x=0; x != operands.length; ++x){
                operands[x] = instrDecoder.getOperand(x, instruct, decoderIndex);
            }

            // print string version of instruction
            return  insToString(groupName, enumOrdinal, operands);
        }
        return "Ins invalid!";
    }

    public int encodeInstruction(String iGroupName, String iMneumonic, int [] operands) {
		return instrDecoder.encode(iGroupName, iMneumonic, operands);
	}
}