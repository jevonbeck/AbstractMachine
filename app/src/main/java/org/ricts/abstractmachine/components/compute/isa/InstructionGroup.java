package org.ricts.abstractmachine.components.compute.isa;

public class InstructionGroup {
	private int[] operandWidths;
	private String groupName;
	private String[] mneumonics;
		
	public InstructionGroup(int[] opWidths, String[] mneumonicList, String name){
		mneumonics = mneumonicList;
		groupName = name;
		operandWidths = opWidths; // opWidths - array of operand widths (specified in ascending order)
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
	
	public int[] operandWidths() {
		return operandWidths;
	}			
}
