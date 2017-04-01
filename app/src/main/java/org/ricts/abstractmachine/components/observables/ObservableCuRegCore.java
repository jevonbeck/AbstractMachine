package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.compute.cu.CuRegCore;
import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;

/**
 * Created by Jevon on 11/03/2017.
 */

public class ObservableCuRegCore extends ObservableType<CuRegCore> implements ControlUnitRegCore {
    public ObservableCuRegCore(CuRegCore type) {
        super(type);
    }

    public static class FetchObject {}
    public static class ExpectedPcObject {}

    @Override
    public void fetchInstruction() {
        observable_data.fetchInstruction();
        setChanged();
        notifyObservers(new FetchObject());
    }

    @Override
    public void setPC(int currentPC) {
        observable_data.setPC(currentPC);
        setChanged();
        notifyObservers();
    }

    @Override
    public void setPcAndIr(int currentPC, int currentIR) {
        observable_data.setPcAndIr(currentPC, currentIR);
        setChanged();
        notifyObservers();
    }

    @Override
    public void reset(int currentPC, int currentIR) {
        observable_data.reset(currentPC, currentIR);
        setChanged();
        notifyObservers(true); // to differentiate that this update is from a reset!
    }

    @Override
    public void updatePcWithExpectedValues() {
        observable_data.updatePcWithExpectedValues();
        setChanged();
        notifyObservers(new ExpectedPcObject());
    }

    @Override
    public boolean hasTempRegs() {
        return observable_data.hasTempRegs();
    }

    @Override
    public int fetchTime() {
        return observable_data.fetchTime();
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
    public String getPCString() {
        return observable_data.getPCString();
    }

    @Override
    public String getIRString() {
        return observable_data.getIRString();
    }
}
