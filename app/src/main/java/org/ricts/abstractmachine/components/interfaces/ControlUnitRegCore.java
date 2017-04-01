package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 11/03/2017.
 */

public interface ControlUnitRegCore {
    void fetchInstruction();
    void setPC(int currentPC);
    void setPcAndIr(int currentPC, int currentIR);
    void reset(int currentPC, int currentIR);
    void updatePcWithExpectedValues();
    boolean hasTempRegs();

    int fetchTime();

    int getPC();
    int getIR();

    String getPCString();
    String getIRString();
}
