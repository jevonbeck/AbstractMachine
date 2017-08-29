package org.ricts.abstractmachine.components.interfaces;


/**
 * Created by jevon.beckles on 17/08/2017.
 */

public interface CompCore {
    void executeInstruction(int programCounter, String instructionGroupName,
                             int instructionGroupIndex, int[] operands);
    int instrExecTime(String instructionGroupName, int instructionGroupIndex);

    void reset();
    void checkInterrupts();
    void setControlUnit(ControlUnitInterface cu);

    boolean controlUnitUpdated();
    int getProgramCounterValue();

    DecoderUnit getDecoderUnit();
    ALU getALU();
}
