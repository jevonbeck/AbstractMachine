package org.ricts.abstractmachine.components.compute.isa;

import java.util.ArrayList;
import java.util.List;

import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.devicetype.InstructionDevice;

public class IsaDecoder extends Device implements InstructionDevice {
    private ArrayList<InstructionGroupDecoder> instructionDecoders;
    private int instructionWidth;
    private int instructionBitMask;

    public IsaDecoder(List<InstructionGroup> formats) {
        instructionDecoders = new ArrayList<>();
        for (int x = 0; x != formats.size(); ++x) {
            instructionDecoders.add(new InstructionGroupDecoder(formats.get(x)));
        }

        int[] maxWidths = combinedWidthArrDescending(instructionDecoders);

        // find minimum number of opcodes for instruction with max operand width
        ArrayList<Integer> maxIndices = findAllMatch(instructionDecoders, maxWidths[0]);

        int opcodeNextIndex = 1; // initialise to 1 to avoid instructions with a value of '0'
        for (int x : maxIndices) {
            InstructionGroupDecoder currentDecoder = instructionDecoders.get(x);
            currentDecoder.startOpcodeRangeFrom(opcodeNextIndex);
            opcodeNextIndex += currentDecoder.instructionCount();
        }

        for (int x = 0; x != maxWidths.length - 1; ++x) {
            int y = x + 1; // index of current width under consideration
            int relativeDiff = maxWidths[x] - maxWidths[y]; // nextHighestWidth - currentWidth
            opcodeNextIndex <<= relativeDiff; // opcodeNextIndex *= Math.pow(2, relativeDiff)

            maxIndices = findAllMatch(instructionDecoders, maxWidths[y]); // find all InstructionGroups with current operand width

            for (int z : maxIndices) {
                InstructionGroupDecoder currentDecoder = instructionDecoders.get(z);
                currentDecoder.startOpcodeRangeFrom(opcodeNextIndex);
                opcodeNextIndex += currentDecoder.instructionCount();
            }
        }
        int opcodeMaxIndex = (opcodeNextIndex - 1) >>> (maxWidths[0] - maxWidths[maxWidths.length - 1]);
        // opcodeMaxIndex = (opcodeNextIndex - 1) / Math.pow(2,(maxWidths[0] - maxWidths[maxWidths.length-1]))

        int opcodeWidth = bitWidth(opcodeMaxIndex);
        instructionWidth = opcodeWidth + maxWidths[0]; // minOpcodeWidth + maxOperandWidth
        instructionBitMask = bitMaskOfWidth(instructionWidth);

        // update all opcodeDecoders to ensure that each instruction is unique
        for (InstructionGroupDecoder currentDecoder : instructionDecoders) {
            currentDecoder.updateOpcodeDecoder(instructionWidth);
        }
    }

    @Override
    public int instrWidth() {
        return instructionWidth;
    }

    public boolean isValidInstruction(int instruction) {
        return getDecoderForInstruction(instruction) != null;
    }

    public InstructionGroupDecoder getDecoderForInstruction(int instruction) {
        int potentialInstruction = instruction & instructionBitMask;

        for (int x = 0; x != instructionDecoders.size(); ++x) {
            InstructionGroupDecoder decoder = instructionDecoders.get(x);
            if (decoder.isValidInstruction(potentialInstruction)) {
                return decoder;
            }
        }
        return null;
    }

    private boolean isValidGroupName(String groupName) {
        return groupNameIndex(groupName) != -1;
    }

    private int groupNameIndex(String groupName) {
        for (int x = 0; x != instructionDecoders.size(); ++x) {
            if (instructionDecoders.get(x).groupName().equals(groupName)) {
                return x;
            }
        }
        return -1;
    }

    public int encode(String groupName, String mneumonic, int[] operands) {
        if (isValidGroupName(groupName)) {
            int decoderIndex = groupNameIndex(groupName);
            return instructionDecoders.get(decoderIndex).encode(mneumonic, operands);
        }
        return -2;
    }

    private ArrayList<Integer> findAllMatch(ArrayList<InstructionGroupDecoder> list, int valToMatch) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int x = 0; x != list.size(); ++x) {
            int temp = list.get(x).combinedOperandWidth();
            if (valToMatch == temp) {
                result.add(x);
            }
        }
        return result;
    }

    private void removeAllMatches(ArrayList<InstructionGroupDecoder> list, int valToMatch) {
        for (int x = 0; x != list.size(); ++x) {
            int temp = list.get(x).combinedOperandWidth();
            if (valToMatch == temp) {
                list.remove(x);
                --x;
            }
        }
    }

    private int maxCombinedOperandWidth(ArrayList<InstructionGroupDecoder> list) {
        int max = 0;
        for (int x = 0; x != list.size(); ++x) {
            max = Math.max(max, list.get(x).combinedOperandWidth());
        }
        return max;
    }

    private int[] combinedWidthArrDescending(ArrayList<InstructionGroupDecoder> list) {
        ArrayList<InstructionGroupDecoder> temp = new ArrayList<>(list);

        ArrayList<Integer> resultList = new ArrayList<>();

        while (!temp.isEmpty()) {
            // find maximum value and add it to the list
            int max = maxCombinedOperandWidth(temp);
            resultList.add(max);

            // remove all elements with maximum value from list
            removeAllMatches(temp, max);
        }

        int[] result = new int[resultList.size()];
        for (int x = 0; x != result.length; ++x) {
            result[x] = resultList.get(x);
        }

        return result;
    }
}
