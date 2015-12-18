package org.ricts.abstractmachine.components.compute.isa;

import java.util.ArrayList;

import org.ricts.abstractmachine.components.devices.Device;

public class IsaDecoder extends Device {
	private ArrayList<InstructionGroupDecoder> instructionDecoders;
	private int instructionWidth;
	private int instructionBitMask;
	private int opcodeWidth;
	private int opcodeMaxIndex;
		
	public IsaDecoder(ArrayList<InstructionGroup> formats){
		instructionDecoders = new ArrayList<InstructionGroupDecoder>();
		for(int x=0; x!= formats.size(); ++x){
			instructionDecoders.add(new InstructionGroupDecoder(formats.get(x)));
		} 
				
		int[] maxWidths = combinedWidthArrDescending(instructionDecoders);
		
		// find minimum number of opcodes for instruction with max operand width 
		ArrayList<Integer> maxIndices = findAllMatch(instructionDecoders, maxWidths[0]);
		
		int opcodeNextIndex = 1; // initialise to 1 to avoid instructions with a value of '0'
		for(int x : maxIndices){
			InstructionGroupDecoder currentDecoder = instructionDecoders.get(x); 
			currentDecoder.startOpcodeRangeFrom(opcodeNextIndex);
			opcodeNextIndex += currentDecoder.instructionCount();
		}
		
		int extra = 0;
		for(int x=0; x!= maxWidths.length-1; ++x){
			int y=x+1; // current width under consideration
			int relativeDiff = maxWidths[x] - maxWidths[y]; // nextHighestWidth - currentWidth
			opcodeNextIndex <<= relativeDiff; // opcodeNextIndex *= Math.pow(2, relativeDiff)
			int availableRange = (1+extra) << relativeDiff; // max no. of instructions allowed by diff without increasing opcodeCount
			// availableRange = (1+extra)*Math.pow(2, relativeDiff)
			
			maxIndices = findAllMatch(instructionDecoders, maxWidths[y]); // find all InstructionGroups with current operand width
			
			for(int z : maxIndices){
				instructionDecoders.get(z).startOpcodeRangeFrom(opcodeNextIndex);
				
				int instrCount = instructionDecoders.get(z).instructionCount();
				int quotient = (instrCount/availableRange) << relativeDiff; // (instrCount/availableRange)*Math.pow(2, relativeDiff)
				int remainder = instrCount%availableRange;
				
				opcodeNextIndex += quotient + remainder;
				extra = availableRange - remainder;	
			}
		}
		opcodeMaxIndex = (opcodeNextIndex - 1) >>> (maxWidths[0] - maxWidths[maxWidths.length-1]);
		// opcodeMaxIndex = (opcodeNextIndex - 1) / Math.pow(2,(maxWidths[0] - maxWidths[maxWidths.length-1]))
		
		opcodeWidth = bitWidth(opcodeMaxIndex);
		instructionWidth = opcodeWidth + maxWidths[0]; // minOpcodeWidth + maxOperandWidth
		instructionBitMask = bitMaskOfWidth(instructionWidth);
		
		// update all opcodeDecoders to ensure that each instruction is unique 
		for(InstructionGroupDecoder currentDecoder : instructionDecoders){
			currentDecoder.updateOpcodeDecoder(instructionWidth);
		}
	}
	
	public int instructionWidth(){
    return instructionWidth;
  }
	
	public boolean isValidInstruction(int instruction){
		return getDecoderIndex(instruction & instructionBitMask) != -1;
	}
	
	public int getDecoderIndex(int instruction){
		int potentialInstruction = instruction & instructionBitMask;
		
		for(int x=0; x != instructionDecoders.size(); ++x){
			if(instructionDecoders.get(x).isValidInstruction(potentialInstruction)){
				return x;
			}
		}
		return -1;
	}		
	
	public String groupName(int decoderIndex){
		if(0 <= decoderIndex && decoderIndex <instructionDecoders.size())
			return instructionDecoders.get(decoderIndex).groupName();
		else
			return "";
	}
	
	
	private boolean isValidGroupName(String groupName){
		return groupNameIndex(groupName) != -1;
	}
	
	private int groupNameIndex(String groupName){
		for(int x=0; x!=instructionDecoders.size(); ++x){
			if(instructionDecoders.get(x).groupName().equals(groupName)){
				return x;
			}			
		}
		return -1;
	}
	
	
	public int encode(String groupName, String mneumonic, int[] operands){
		if(isValidGroupName(groupName)){
			int decoderIndex = groupNameIndex(groupName);
			return instructionDecoders.get(decoderIndex).encode(mneumonic, operands);
		}		
		return -2;
	}
	
	public int decode(int instruction, int decoderIndex){
		int potentialInstruction = instruction & instructionBitMask;
		
		if(0 <= decoderIndex && decoderIndex < instructionDecoders.size()){
			return instructionDecoders.get(decoderIndex).decode(potentialInstruction);
		}
		else
			return -1;
	}
	
	public int operandCount(int decoderIndex){
		if(0 <= decoderIndex && decoderIndex <instructionDecoders.size())
			return instructionDecoders.get(decoderIndex).operandCount();
		else
			return -1;
	}
	
	public int getOperand(int opIndex, int instruction, int decoderIndex){
		int potentialInstruction = instruction & instructionBitMask;
		
		if(0 <= decoderIndex && decoderIndex < instructionDecoders.size())
			if(0 <= opIndex && opIndex < operandCount(decoderIndex))
				return instructionDecoders.get(decoderIndex).getOperand(opIndex, potentialInstruction);
			else
				return -2;
		else
			return -1;
	}	
		
	private ArrayList<Integer> findAllMatch(ArrayList<InstructionGroupDecoder> list, int valToMatch){
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int x=0; x != list.size(); ++x){
			int temp = list.get(x).combinedOperandWidth();
			if(valToMatch == temp){
				result.add(x);
			}
		}
		return result;
	}
	
	private void removeAllMatches(ArrayList<InstructionGroupDecoder> list, int valToMatch){
		for(int x=0; x != list.size(); ++x){
			int temp = list.get(x).combinedOperandWidth();
			if(valToMatch == temp){
				list.remove(x);
				--x;
			}
		}
	}
	
	private int maxCombinedOperandWidth(ArrayList<InstructionGroupDecoder> list){
		int max = 0;
		for(int x=0; x != list.size(); ++x){
			max = Math.max(max, list.get(x).combinedOperandWidth());
		}
		return max;
	}
	
	private int[] combinedWidthArrDescending(ArrayList<InstructionGroupDecoder> list){
		ArrayList<InstructionGroupDecoder> temp = new ArrayList<InstructionGroupDecoder>(list);
		
		ArrayList<Integer> resultList = new ArrayList<Integer>();
		
		while(!temp.isEmpty()){
			// find maximum value and add it to the list
			int max = maxCombinedOperandWidth(temp); 
			resultList.add(max); 
			
			// remove all elements with maximum value from list
			removeAllMatches(temp,max);
		}
		
		int[] result = new int[resultList.size()];
		for(int x=0; x != result.length; ++x){
			result[x] = resultList.get(x);
		}
		
		return result;
	}		
}
