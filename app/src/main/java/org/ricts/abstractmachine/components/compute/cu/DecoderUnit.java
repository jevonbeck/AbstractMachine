package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.isa.InstructionGroupDecoder;
import org.ricts.abstractmachine.components.compute.isa.IsaDecoder;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.devicetype.InstructionAddressDevice;
import org.ricts.abstractmachine.components.devicetype.InstructionDevice;
import org.ricts.abstractmachine.components.interfaces.DecoderCore;

/**
 * Created by Jevon on 15/07/2017.
 */

public class DecoderUnit extends Device implements DecoderCore, InstructionDevice, InstructionAddressDevice {
    private IsaDecoder isaDecoder;

    private boolean useTempStorage;

    private int iAddrWidth;
    private int instrBitMask, iAddrBitMask;

    private boolean currentInstructionValid;
    private String groupName, tempGroupName;
    private int groupIndex, tempGroupIndex;
    private int[] operands, tempOperands;

    private int storedPC;

    public DecoderUnit(IsaDecoder decoder, int pcWidth) {
        this(decoder, pcWidth, false);
    }

    public DecoderUnit(IsaDecoder decoder, int pcWidth, boolean stageStorage) {
        isaDecoder = decoder;
        instrBitMask = bitMaskOfWidth(isaDecoder.instrWidth());

        iAddrWidth = pcWidth;
        iAddrBitMask = bitMaskOfWidth(iAddrWidth);

        useTempStorage = stageStorage;
    }

    @Override
    public void decode(int programCounter, int instruction) {
        storedPC = programCounter & iAddrBitMask;

        int instruct = instruction & instrBitMask;
        currentInstructionValid = isaDecoder.isValidInstruction(instruct);
        if(currentInstructionValid) {
            InstructionGroupDecoder decoder = isaDecoder.getDecoderForInstruction(instruct);
            if(useTempStorage){
                tempGroupName = decoder.groupName();
                tempGroupIndex = decoder.decode(instruct);
                tempOperands = new int [decoder.operandCount()];
                for(int x=0; x != tempOperands.length; ++x){
                    tempOperands[x] = decoder.getOperand(x, instruct);
                }
            }
            else {
                groupName = decoder.groupName();
                groupIndex = decoder.decode(instruct);
                operands = new int [decoder.operandCount()];
                for(int x=0; x != operands.length; ++x){
                    operands[x] = decoder.getOperand(x, instruct);
                }
            }
        }
    }

    @Override
    public void updateValues() {
        if(useTempStorage) {
            groupName = tempGroupName;
            groupIndex = tempGroupIndex;
            operands = tempOperands;
        }
    }

    @Override
    public boolean hasTempStorage() {
        return useTempStorage;
    }

    @Override
    public boolean isValidInstruction() {
        return currentInstructionValid;
    }

    @Override
    public String getInstructionGroupName() {
        return groupName;
    }

    @Override
    public int getInstructionGroupIndex() {
        return groupIndex;
    }

    @Override
    public int[] getOperands() {
        return operands;
    }

    @Override
    public int getProgramCounter() {
        return storedPC;
    }

    @Override
    public int instrWidth() {
        return isaDecoder.instrWidth();
    }

    @Override
    public int iAddrWidth() {
        return iAddrWidth;
    }
}
