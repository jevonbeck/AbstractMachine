package org.ricts.abstractmachine.components.observable;

import org.ricts.abstractmachine.components.compute.core.DecoderCore;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;

/**
 * Created by jevon.beckles on 19/08/2017.
 */

public class ObservableDecoderUnit extends ObservableType<DecoderCore> implements DecoderUnit {

    public ObservableDecoderUnit(DecoderCore type) {
        super(type);
    }

    public static class DecodeParams extends Params {
        private enum Args{
            PROGRAM_COUNTER, INSTRUCTION, INSTR_STR
        }

        public DecodeParams(Object... objects){
            super(objects);
        }

        public int getInstruction() {
            return (Integer) params[Args.INSTRUCTION.ordinal()];
        }

        public int getProgramCounter() {
            return (Integer) params[Args.PROGRAM_COUNTER.ordinal()];
        }

        public String getInstructionString() {
            return (String) params[Args.INSTR_STR.ordinal()];
        }
    }

    public static class GetNopParams {}
    public static class InvalidateParams {}

    @Override
    public void decode(int programCounter, int instruction) {
        observable_data.decode(programCounter, instruction);
        setChanged();

        String instructionText = observable_data.hasTempStorage() ? observable_data.tempInstrString()
            : observable_data.instrString();
        notifyObservers(new DecodeParams(programCounter, instruction, instructionText));
    }

    @Override
    public void reset() {
        observable_data.reset();
        setChanged();
        notifyObservers(true);
    }

    @Override
    public void updateValues() {
        observable_data.updateValues();
        //setChanged();
        //notifyObservers();
    }

    @Override
    public void invalidateValues() {
        observable_data.invalidateValues();
        setChanged();
        notifyObservers(new InvalidateParams());
    }

    @Override
    public int getNopInstruction() {
        int nopInstruction = observable_data.getNopInstruction();
        setChanged();
        notifyObservers(new GetNopParams());
        return nopInstruction;
    }

    @Override
    public boolean hasTempStorage() {
        return observable_data.hasTempStorage();
    }

    @Override
    public boolean isDataMemoryInstruction() {
        return observable_data.isDataMemoryInstruction();
    }

    @Override
    public boolean isHaltInstruction() {
        return observable_data.isHaltInstruction();
    }

    @Override
    public boolean isSleepInstruction() {
        return observable_data.isSleepInstruction();
    }

    @Override
    public boolean isValidInstruction() {
        return observable_data.isValidInstruction();
    }

    @Override
    public String getMneumonic() {
        return observable_data.getMneumonic();
    }

    @Override
    public int[] getOperands() {
        return observable_data.getOperands();
    }

    @Override
    public int getProgramCounter() {
        return observable_data.getProgramCounter();
    }

    @Override
    public String instrString() {
        return observable_data.instrString();
    }

    @Override
    public String getOperandsString() {
        return observable_data.getOperandsString();
    }

    @Override
    public String nopMneumonic() {
        return observable_data.nopMneumonic();
    }

    @Override
    public String[] getMneumonicList() {
        return observable_data.getMneumonicList();
    }

    @Override
    public OperandInfo getDataOperandInfo() {
        return observable_data.getDataOperandInfo();
    }

    @Override
    public OperandInfo getDataAddrOperandInfo() {
        return observable_data.getDataAddrOperandInfo();
    }

    @Override
    public OperandInfo getInstrAddrOperandInfo() {
        return observable_data.getInstrAddrOperandInfo();
    }

    @Override
    public OperandInfo[] getOperandInfoArray() {
        return observable_data.getOperandInfoArray();
    }

    @Override
    public String[] getOperandLabels() {
        return observable_data.getOperandLabels();
    }

    @Override
    public String getDescription() {
        return observable_data.getDescription();
    }

    @Override
    public String getInstructionFormat() {
        return observable_data.getInstructionFormat();
    }

    @Override
    public void setMneumonic(String mneumonic) {
        observable_data.setMneumonic(mneumonic);
    }

    @Override
    public String instrValueString(int instruction) {
        return observable_data.instrValueString(instruction);
    }

    @Override
    public String instrAddrValueString(int address) {
        return observable_data.instrAddrValueString(address);
    }

    @Override
    public String dataAddrValueString(int address) {
        return observable_data.dataAddrValueString(address);
    }

    @Override
    public String dataValueString(int data) {
        return observable_data.dataValueString(data);
    }

    @Override
    public int encodeInstruction(String iMneumonic, int[] operands) {
        return observable_data.encodeInstruction(iMneumonic, operands);
    }

    @Override
    public int dataWidth() {
        return observable_data.dataWidth();
    }

    @Override
    public int instrWidth() {
        return observable_data.instrWidth();
    }

    @Override
    public int iAddrWidth() {
        return observable_data.iAddrWidth();
    }

    @Override
    public int dAddrWidth() {
        return observable_data.dAddrWidth();
    }
}
