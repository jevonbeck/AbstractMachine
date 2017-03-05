package org.ricts.abstractmachine.components.interfaces;

import org.ricts.abstractmachine.components.devicetype.ComputeDevice;

/**
 * Created by Jevon on 18/12/2015.
 */
public interface ComputeCoreInterface extends ComputeDevice{
    void executeInstruction(int programCounter, int instruction, ControlUnitInterface cu);
    int instrExecTime(int instruction);
    void reset();
    int getNopInstruction();
    void checkInterrupts(ControlUnitInterface cu);

    void setDataMemory(MemoryPort memory);
}
