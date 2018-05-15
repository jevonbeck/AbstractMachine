package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.FetchCore;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

/**
 * Created by Jevon on 11/03/2017.
 */

public class FetchUnit implements FetchCore {
    private Register pc, tempPC; // Program Counter
    private Register instrPC, tempInstrPC; // Instruction Program Counter value
    private Register ir, tempIR; // Instruction Register
    private ReadPort instructionCache;
    private boolean useTempRegs;

    public FetchUnit(ReadPort iCache, int pcWidth, int irWidth) {
        this(iCache, pcWidth, irWidth, false);
    }

    public FetchUnit(ReadPort iCache, int pcWidth, int irWidth, boolean stageStorage) {
        instructionCache = iCache;
        pc = new Register(pcWidth);
        instrPC = new Register(pcWidth);
        ir = new Register(irWidth);
        useTempRegs = stageStorage;

        if(useTempRegs) {
            tempPC = new Register(pcWidth);
            tempInstrPC = new Register(pcWidth);
            tempIR = new Register(irWidth);
        }
    }

    @Override
    public void fetchInstruction(){
        int pcValue = getPC();
        int pcIncrementedValue = pcValue + 1; // IR = iCache[PC]
        int irValue = instructionCache.read(pcValue); // PC += 1

        if(useTempRegs){
            tempInstrPC.write(pcValue);
            tempPC.write(pcIncrementedValue);
            tempIR.write(irValue);
        }
        else {
            instrPC.write(pcValue);
            pc.write(pcIncrementedValue);
            ir.write(irValue);
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
            instrPC.write(tempInstrPC.read());
            pc.write(tempPC.read());
            ir.write(tempIR.read());
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
    public int getInstructionPC() {
        return instrPC.read();
    }

    @Override
    public int getIR() {
        return ir.read();
    }

    @Override
    public String getPCString() {
        return pc.dataString();
    }

    @Override
    public String getInstructionPCString() {
        return instrPC.dataString();
    }

    @Override
    public String getIRString() {
        return ir.dataString();
    }

    public boolean hasTempRegs() {
        return useTempRegs;
    }

    public String getTempPCString(){
        return tempPC.dataString();
    }
}
