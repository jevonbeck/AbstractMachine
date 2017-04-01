package org.ricts.abstractmachine.components.compute.isa;

public class InstructionGroup {
	private OperandInfo[] operandInfos;
	private String groupName;
	private String[] mneumonics;
		
	public InstructionGroup(OperandInfo[] opInfos, String[] mneumonicList, String name){
		mneumonics = mneumonicList;
		groupName = name;
		operandInfos = opInfos; // array of operand metadata (specified in ascending order)
	}
	
	public String mneumonicAt(int index){
		if(0 <= index && index < mneumonics.length)
			return mneumonics[index];
		else 
			return "";
	}
	
	public String groupName(){
		return groupName;
	}
	
	public int instructionCount() {
		return mneumonics.length;
	}	
	
	public OperandInfo[] operandInfoArray() {
		return operandInfos;
	}			
}
