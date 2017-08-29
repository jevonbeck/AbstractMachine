package org.ricts.abstractmachine.components.interfaces;

import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.devicetype.ComputeDevice;

/**
 * Created by Jevon on 15/07/2017.
 */

public interface DecoderUnit extends ComputeDevice {
    void decode(int programCounter, int instruction);
    void reset();
    void updateValues();
    void invalidateValues();

    String instrValueString(int instruction);
    String instrAddrValueString(int address);
    String dataAddrValueString(int address);
    String dataValueString(int data);
    int encodeInstruction(String iMneumonic, int [] operands);
    
    boolean hasTempStorage();
    boolean isDataMemoryInstruction();
    boolean isHaltInstruction();
    boolean isSleepInstruction();
    boolean isValidInstruction();
    String getInstructionGroupName();
    int getInstructionGroupIndex();
    String getMneumonic();
    int[] getOperands();
    int getProgramCounter();
    String instrString();
    String getOperandsString();

    String nopMneumonic();
    int getNopInstruction();
    String[] getMneumonicList();
    OperandInfo getDataOperandInfo();
    OperandInfo getDataAddrOperandInfo();
    OperandInfo getInstrAddrOperandInfo();
    OperandInfo[] getOperandInfoArray();
    String[] getOperandLabels();
    String getDescription();
    String getInstructionFormat();
}
