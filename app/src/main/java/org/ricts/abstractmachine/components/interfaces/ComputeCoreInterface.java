package org.ricts.abstractmachine.components.interfaces;

import org.ricts.abstractmachine.components.devicetype.ComputeDevice;

/**
 * Created by Jevon on 18/12/2015.
 */
public interface ComputeCoreInterface extends ComputeDevice{
    void executeInstruction(int programCounter, int instruction, MemoryPort dataMemory, ControlUnitInterface cu);
    int instrExecTime(int instruction, MemoryPort dataMemory);
    void reset();
    int getNopInstruction();

    void checkInterrupts(ControlUnitInterface cu);
}
