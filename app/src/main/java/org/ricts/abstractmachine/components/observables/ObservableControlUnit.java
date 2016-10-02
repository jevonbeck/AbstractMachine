package org.ricts.abstractmachine.components.observables;


import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

/**
 * Created by Jevon on 23/01/2016.
 */
public class ObservableControlUnit extends ObservableType<CuDataInterface> implements CuDataInterface {
    public ObservableControlUnit(CuDataInterface type) {
        super(type);
    }

    @Override
    public void setNextFetch(int instructionAddress) {
        observable_data.setNextFetch(instructionAddress);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setNextFetchAndExecute(int instructionAddress, int nopInstruction) {
        observable_data.setNextFetchAndExecute(instructionAddress, nopInstruction);
        setChanged();
        notifyObservers();
    }

    @Override
    public void reset() {
        observable_data.reset();
        setChanged();
        notifyObservers(true); // to differentiate that this update is a reset!
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        observable_data.setStartExecFrom(currentPC);
        setChanged();
        notifyObservers(true); // to differentiate that this update is equivalent to a reset!
    }

    @Override
    public void setNextStateToHalt() {
        observable_data.setNextStateToHalt();
        setChanged();
        notifyObservers();
    }

    @Override
    public void setNextStateToSleep() {
        observable_data.setNextStateToSleep();
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
    public void fetchInstruction(ReadPort instructionCache) {
        observable_data.fetchInstruction(instructionCache);
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean isPipelined() {
        return observable_data.isPipelined();
    }

    @Override
    public boolean isInHaltState() {
        return observable_data.isInHaltState();
    }

    @Override
    public boolean isInSleepState() {
        return observable_data.isInSleepState();
    }

    @Override
    public int nextActionDuration() {
        return observable_data.nextActionDuration();
    }

    @Override
    public String getPCDataString() {
        return observable_data.getPCDataString();
    }

    @Override
    public String getIRDataString() {
        return observable_data.getIRDataString();
    }

    @Override
    public String getCurrentStateString() {
        return observable_data.getCurrentStateString();
    }

    @Override
    public int getPC() {
        return observable_data.getPC();
    }

    @Override
    public int getIR() {
        return observable_data.getIR();
    }
}
