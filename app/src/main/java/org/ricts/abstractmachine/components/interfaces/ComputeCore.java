package org.ricts.abstractmachine.components.interfaces;


/**
 * Created by jevon.beckles on 17/08/2017.
 */

public interface ComputeCore {
    void executeInstruction(int programCounter, String mneumonic, int[] operands);
    int instrExecTime(String mneumonic);

    void reset();
    void checkInterrupts();
    void setControlUnit(ControlUnitInterface cu);

    boolean controlUnitUpdated();
    int getProgramCounterValue();

    DecoderUnit getDecoderUnit();
}
