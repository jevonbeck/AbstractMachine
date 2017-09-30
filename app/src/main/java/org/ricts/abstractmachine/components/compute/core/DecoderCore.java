package org.ricts.abstractmachine.components.compute.core;

import android.content.res.Resources;

import org.ricts.abstractmachine.components.compute.isa.InstructionGroup;
import org.ricts.abstractmachine.components.compute.isa.InstructionGroupDecoder;
import org.ricts.abstractmachine.components.compute.isa.IsaDecoder;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.devicetype.InstructionAddressDevice;
import org.ricts.abstractmachine.components.devicetype.InstructionDevice;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;

import java.util.List;

/**
 * Created by Jevon on 15/07/2017.
 */

public abstract class DecoderCore extends Device implements DecoderUnit, InstructionDevice, InstructionAddressDevice {
    protected Resources resources;
    protected int iAddrWidth, dAddrWidth, dataWidth;
    protected String groupName, mneumonic;
    protected int groupIndex;
    protected int[] operands;

    private int instrWidth, instrBitMask, iAddrBitMask;
    private String tempGroupName, tempMneumonic;
    private int tempGroupIndex;
    private int[] tempOperands;

    private IsaDecoder isaDecoder;
    private int storedPC, tempPC;
    private boolean useTempStorage, currentInstructionValid;

    public abstract OperandInfo getDataRegOperandInfo();

    protected abstract String instructionString();
    protected abstract int deriveDataWidth(int configValue);
    protected abstract String getGroupName(String mneumonic);
    protected abstract List<InstructionGroup> createInstructionSet(Integer... widthConfig);

    public DecoderCore(Resources resources, Integer... widthConfig) {
        this(resources, false, widthConfig);
    }

    public DecoderCore(Resources res, boolean stageStorage, Integer... widthConfig) {
        resources = res;
        iAddrWidth = widthConfig[0];
        iAddrBitMask = bitMaskOfWidth(iAddrWidth);

        dAddrWidth = widthConfig[1];
        dataWidth = deriveDataWidth(widthConfig[2]);

        isaDecoder = new IsaDecoder(createInstructionSet(widthConfig));

        instrWidth = isaDecoder.instrWidth();
        instrBitMask = bitMaskOfWidth(instrWidth);

        useTempStorage = stageStorage;
    }

    @Override
    public void decode(int programCounter, int instruction) {
        int instruct = instruction & instrBitMask;
        currentInstructionValid = isaDecoder.isValidInstruction(instruct);
        if(currentInstructionValid) {
            InstructionGroupDecoder decoder = isaDecoder.getDecoderForInstruction(instruct);
            if(useTempStorage){
                tempPC = programCounter & iAddrBitMask;
                tempGroupName = decoder.groupName();
                tempGroupIndex = decoder.decode(instruct);
                tempMneumonic = decoder.getMneumonicAtIndex(tempGroupIndex);
                tempOperands = new int [decoder.operandCount()];
                for(int x=0; x != tempOperands.length; ++x){
                    tempOperands[x] = decoder.getOperand(x, instruct);
                }
            }
            else {
                storedPC = programCounter & iAddrBitMask;
                groupName = decoder.groupName();
                groupIndex = decoder.decode(instruct);
                mneumonic = decoder.getMneumonicAtIndex(groupIndex);
                operands = new int [decoder.operandCount()];
                for(int x=0; x != operands.length; ++x){
                    operands[x] = decoder.getOperand(x, instruct);
                }
            }
        }
    }

    @Override
    public void reset() {
        invalidateValues();
    }

    @Override
    public void updateValues() {
        if(useTempStorage) {
            storedPC = tempPC;
            groupName = tempGroupName;
            groupIndex = tempGroupIndex;
            mneumonic = tempMneumonic;
            operands = tempOperands;
        }
    }

    @Override
    public void invalidateValues() {
        currentInstructionValid = false;
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
    public String getMneumonic() {
        return mneumonic;
    }

    @Override
    public int[] getOperands() {
        return operands;
    }

    @Override
    public String getOperandsString() {
        String result = "";

        OperandInfo[] opInfoArr = getOperandInfoArray();
        for(int x=0; x < opInfoArr.length; ++x) {
            if(x > 0) {
                result += ", ";
            }
            result += opInfoArr[x].getPrettyValue(operands[x]);
        }

        return result;
    }

    @Override
    public int getProgramCounter() {
        return storedPC;
    }

    @Override
    public String instrString() {
        return currentInstructionValid ? instructionString() : null;
    }

    @Override
    public int getNopInstruction() {
        return encodeInstruction(nopMneumonic(), new int [0]);
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
    public int encodeInstruction(String iMneumonic, int [] operands) {
        return isaDecoder.encode(getGroupName(iMneumonic), iMneumonic, operands);
    }

    @Override
    public void setMneumonic(String mneum) {
        mneumonic = mneum;
    }

    public String instrValueString(int instruction) {
        return formatNumberInHex(instruction, instrWidth);
    }

    public String instrAddrValueString(int address) {
        return formatNumberInHex(address, iAddrWidth);
    }

    public String dataAddrValueString(int address) {
        return formatNumberInHex(address, dAddrWidth);
    }

    public String dataValueString(int data) {
        return formatNumberInHex(data, dataWidth);
    }
}
