package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 15/07/2017.
 */

public interface DecoderCore {
    void decode(int programCounter, int instruction);
    void updateValues();
    boolean hasTempStorage();

    boolean isValidInstruction();
    String getInstructionGroupName();
    int getInstructionGroupIndex();
    int[] getOperands();

    int getProgramCounter();


}
