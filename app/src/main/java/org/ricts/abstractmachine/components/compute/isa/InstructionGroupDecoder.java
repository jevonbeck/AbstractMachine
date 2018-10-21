package org.ricts.abstractmachine.components.compute.isa;

import android.util.Log;
import org.ricts.abstractmachine.components.devicetype.Device;

public class InstructionGroupDecoder extends Device {
	private static final String TAG = "InstructionGroupDecoder";
	
	private InstructionGroup instructionFormat;
	private OpDecoder[] operandDecoders;
	private OpDecoder opcodeDecoder;
	private int[] opcodeRange;
	private int operandsCombinedWidth;
	
	public InstructionGroupDecoder(InstructionGroup format){
		instructionFormat = format; 
		opcodeRange = new int[instructionFormat.instructionCount()];

        OperandInfo[] infoArray = instructionFormat.operandInfoArray();
		operandDecoders = new OpDecoder[infoArray.length];
		
		int offset = 0;
		for (int x=infoArray.length-1; x>= 0; --x){
            int dataWidth = infoArray[x].getDataWidth();
			operandDecoders[x] = new OpDecoder(dataWidth, offset);
			offset += dataWidth;
		}

        operandsCombinedWidth = offset;
	}
	
	public void startOpcodeRangeFrom(int startIndex){
		for(int x=0; x!= opcodeRange.length; ++x){
			opcodeRange[x] = startIndex + x;
		}
		opcodeDecoder = new OpDecoder(bitWidth(opcodeRange[opcodeRange.length-1]),operandsCombinedWidth);
	}
	
	public String decode(int instruction){
		return  getMneumonicAtIndex(opcodeIndex(opcodeDecoder.getOpFrom(instruction)));
	}

	public int encode(String mneumonic, int[] operands){
		Log.d(TAG, "groupName = " + instructionFormat.groupName());
		Log.d(TAG, "mneumonic = " + mneumonic);
		
		if(isVaildMneumonic(mneumonic) && operandsAreValid(operands)){
			int opcode = opcodeRange[mneumonicIndex(mneumonic)];
			
			Log.d(TAG, "opcode = " + opcode);
			
			int instruction = 0;
			instruction = opcodeDecoder.putOpIn(instruction, opcode);
			
			for(int x=0; x != operands.length; ++x){
				instruction = operandDecoders[x].putOpIn(instruction, operands[x]);
			}
			
			return instruction;
		}		
		
		return -1;
	}		
	
	public void updateOpcodeDecoder(int instructionWidth){
		if(opcodeDecoder.dataWidth() + operandsCombinedWidth != instructionWidth){
			opcodeDecoder.updateDataWidth(instructionWidth - operandsCombinedWidth);
		}
	}
	
	public boolean isValidInstruction(int instruction){
		return isValidOpcode(extractOpcode(instruction));
	}
	
	public int getOperand(int operandPos, int instruction){
		return operandDecoders[operandPos].getOpFrom(instruction);
	}
	
	public int operandCount(){
		return operandDecoders.length;
	}

	public String[] getMneumonics() {
		return instructionFormat.getMneumonics();
	}
	
	private boolean operandsAreValid(int[] operands){
		return operands.length == operandDecoders.length;
	}
	
	private boolean isVaildMneumonic(String mneumonic){
		return mneumonicIndex(mneumonic) != -1;
	}
	
	private int extractOpcode(int instruction){
		return opcodeDecoder.getOpFrom(instruction);
	}
	
	private boolean isValidOpcode(int opcode){		
		return opcodeIndex(opcode) != -1;
	}		
	
	private int opcodeIndex(int opcode){
		for(int x=0; x!= opcodeRange.length; ++x){
			if(opcodeRange[x] == opcode){
				return x;
			}
		}
		return -1;
	}
	
	private int mneumonicIndex(String mneumonic){
		int instructionCount = instructionFormat.instructionCount(); 
		for(int x=0; x!= instructionCount; ++x){
			if(instructionFormat.mneumonicAt(x).equals(mneumonic)){
				return x;
			}
		}
		return -1;
	}

    private String getMneumonicAtIndex(int groupIndex) {
        return groupIndex < instructionFormat.instructionCount() ?
                instructionFormat.mneumonicAt(groupIndex) : null;
    }
	
	public String groupName(){
		return instructionFormat.groupName();
	}
	
	public int combinedOperandWidth(){		
		return operandsCombinedWidth;
	}
	
	public int instructionCount(){
		return instructionFormat.instructionCount();
	}	
}
