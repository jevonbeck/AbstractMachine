package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

/**
 * Created by Jevon on 23/01/2016.
 */
public class ObservableControlUnit extends ObservableFSM<ControlUnit> implements ControlUnitInterface {
    public ObservableControlUnit(ControlUnit type) {
        super(type);
    }

    @Override
    public int getPC() {
        return observable_data.getPC();
    }

    @Override
    public int getIR() {
        return observable_data.getIR();
    }

    @Override
    public void setPC(int currentPC) {
        observable_data.setPC(currentPC);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setIR(int currentIR) {
        observable_data.setIR(currentIR);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        observable_data.setStartExecFrom(currentPC);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setNextExecFrom(int currentPC) {
        observable_data.setNextExecFrom(currentPC);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setToExecuteState() {
        observable_data.setToExecuteState();
        setChanged();
        notifyObservers();
    }

    @Override
    public void setNextStateToHalt() {
        observable_data.setNextStateToHalt();
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean isAboutToFetch() {
        return observable_data.isAboutToFetch();
    }

    @Override
    public boolean isAboutToExecute() {
        return observable_data.isAboutToExecute();
    }

    @Override
    public void fetchInstruction(ReadPort instructionCache) {
        observable_data.fetchInstruction(instructionCache);
        setChanged();
        notifyObservers();
    }

    @Override
    public void performNextAction() {
        observable_data.performNextAction();
        setChanged();
        notifyObservers();
    }

    @Override
    public int nextActionDuration() {
        return observable_data.nextActionDuration();
    }
}
