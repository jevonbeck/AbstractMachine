package org.ricts.abstractmachine.components.interfaces;

import org.ricts.abstractmachine.components.devices.ComputeDevice;

/**
 * Created by Jevon on 18/12/2015.
 */
public interface ComputeCoreInterface extends ComputeDevice{
    void executeInstruction(int instruction, MemoryPort dataMemory, ControlUnitInterface cu);
    int instrExecTime(int instruction, MemoryPort dataMemory);
    int nopInstruction();
}
