package org.ricts.abstractmachine.components.observable;

import org.ricts.abstractmachine.components.interfaces.ALU;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;

/**
 * Created by Jevon on 16/01/2016.
 */
public class ObservableComputeAltCore<T extends CompCore> extends ObservableType<T> implements CompCore {
    public ObservableComputeAltCore(T core){
        super(core);
    }

    public static class ExecuteParams {}
    public static class InterruptParams {}

    @Override
    public void executeInstruction(int programCounter, String instructionGroupName, int instructionGroupIndex, int[] operands) {
        observable_data.executeInstruction(programCounter, instructionGroupName, instructionGroupIndex, operands);
        setChanged();
        notifyObservers(new ExecuteParams());
    }

    @Override
    public int instrExecTime(String instructionGroupName, int instructionGroupIndex) {
        return observable_data.instrExecTime(instructionGroupName, instructionGroupIndex);
    }

    @Override
    public void reset() {
        observable_data.reset();
        setChanged();
        notifyObservers(true); // to differentiate that this update is a reset!
    }

    @Override
    public void checkInterrupts() {
        observable_data.checkInterrupts();
        setChanged();
        notifyObservers(new InterruptParams());
    }

    @Override
    public void setControlUnit(ControlUnitInterface cu) {
        observable_data.setControlUnit(cu);
    }

    @Override
    public boolean controlUnitUpdated() {
        return observable_data.controlUnitUpdated();
    }

    @Override
    public int getProgramCounterValue() {
        return observable_data.getProgramCounterValue();
    }

    @Override
    public DecoderUnit getDecoderUnit() {
        return observable_data.getDecoderUnit();
    }

    @Override
    public ALU getALU() {
        return observable_data.getALU();
    }
}
