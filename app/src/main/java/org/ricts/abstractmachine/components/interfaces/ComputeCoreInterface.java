package org.ricts.abstractmachine.components.interfaces;

import org.ricts.abstractmachine.components.ComputeDevice;

/**
 * Created by Jevon on 18/12/2015.
 */
public interface ComputeCoreInterface extends ComputeDevice{
    void executeInstruction(int instruction, MemoryPort dataMemory, RegisterPort PC);
    int instrExecTime(int instruction, MemoryPort dataMemory);

    boolean isHaltInstruction(int instruction);
    int nopInstruction();
}
