package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 26/08/2016.
 */
public interface CuInternalInterface extends ControlUnitInterface {
    int getPC();
    int getIR();
    void fetchInstruction(ReadPort instructionCache);
}
