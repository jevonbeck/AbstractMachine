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

    public static class ExpectedPcObject {}
    public static class SetRegsObject {}

    public static class FetchObject {
        private String PC;

        public FetchObject(String pc) {
            PC = pc;
        }

        public String getPC() {
            return PC;
        }
    }

    @Override
    public void fetchInstruction() {
        observable_data.fetchInstruction();
        setChanged();

        String pc = observable_data.hasTempRegs() ?
                observable_data.getTempPCString() : observable_data.getPCString();
        notifyObservers(new FetchObject(pc));
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
        notifyObservers(new SetRegsObject());
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
