package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

/**
 * Created by Jevon on 11/03/2017.
 */

public class CuRegCore implements ControlUnitRegCore {
    private Register pc, tempPC; // Program Counter
    private Register ir, tempIR; // Instruction Register
    private ReadPort instructionCache;
    private boolean useTempRegs;

    public CuRegCore(ReadPort iCache, int pcWidth, int irWidth) {
        this(iCache, pcWidth, irWidth, false);
    }

    public CuRegCore(ReadPort iCache, int pcWidth, int irWidth, boolean stageStorage) {
        instructionCache = iCache;
        pc = new Register(pcWidth);
        ir = new Register(irWidth);
        useTempRegs = stageStorage;

        if(useTempRegs) {
            tempPC = new Register(pcWidth);
            tempIR = new Register(irWidth);
        }
    }

    @Override
    public void fetchInstruction(){
        int pcValue = getPC();
        int pcIncrementedValue = pcValue + 1; // IR = iCache[PC]
        int irValue = instructionCache.read(pcValue); // PC += 1

        if(useTempRegs){
            tempPC.write(pcIncrementedValue);
            tempIR.write(irValue);
        }
        else {
            setPcAndIr(pcIncrementedValue, irValue);
        }
    }

    @Override
    public void setPC(int currentPC){
        pc.write(currentPC);
    }

    @Override
    public void setPcAndIr(int currentPC, int currentIR){
        pc.write(currentPC);
        ir.write(currentIR);
    }

    @Override
    public void updatePcWithExpectedValues() {
        if(useTempRegs){
            setPcAndIr(tempPC.read(), tempIR.read());
        }
    }

    @Override
    public int fetchTime() {
        return instructionCache.accessTime();
    }

    @Override
    public int getPC(){
        return pc.read();
    }

    @Override
    public int getIR() {
        return ir.read();
    }

    @Override
    public String getPCDataString() {
        return pc.dataString();
    }

    @Override
    public String getIRDataString() {
        return ir.dataString();
    }
}
