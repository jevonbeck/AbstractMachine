package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;

/**
 * Created by Jevon on 16/01/2016.
 */
public class ObservableComputeCore<T extends ComputeCore> extends ObservableType<T> implements ComputeCoreInterface {
    private boolean isControlUnitPipelined;

    public ObservableComputeCore(T core){
        super(core);
    }

    public static class ExecuteParams extends ObservableComputeCore.Params {
        protected enum Args{
            INSTRUCTION, PC_PRE_EXECUTED, PC_POST_EXECUTED, CU_IS_PIPELINED
        }

        public ExecuteParams(Object... objects){
            super(objects);
        }

        public int getInstruction() {
            return (Integer) params[Args.INSTRUCTION.ordinal()];
        }

        public int getPcPreExecute() {
            return (Integer) params[Args.PC_PRE_EXECUTED.ordinal()];
        }

        public int getPcPostExecute(){
            return (Integer) params[Args.PC_POST_EXECUTED.ordinal()];
        }

        public boolean getCuIsPipelined(){
            return (Boolean) params[Args.CU_IS_PIPELINED.ordinal()];
        }
    }

    public static class GetNopParams extends ObservableComputeCore.Params {
        public GetNopParams(Object... objects){
            super(objects);
        }

        public int getNopInstruction() {
            return (Integer) params[0];
        }
    }

    @Override
    public void executeInstruction(int programCounter, int instruction) {
        observable_data.executeInstruction(programCounter, instruction);
        setChanged();
        notifyObservers(new ExecuteParams(instruction, programCounter,
                observable_data.getProgramCounterValue(), isControlUnitPipelined));
    }

    @Override
    public int instrExecTime(int instruction) {
        return observable_data.instrExecTime(instruction);
    }

    @Override
    public void reset() {
        observable_data.reset();
        setChanged();
        notifyObservers(true); // to differentiate that this update is a reset!
    }

    @Override
    public int getNopInstruction() {
        int nopInstruction = observable_data.getNopInstruction();
        setChanged();
        notifyObservers(new GetNopParams(nopInstruction));
        return nopInstruction;
    }

    @Override
    public void checkInterrupts() {
        observable_data.checkInterrupts();
        setChanged();
        notifyObservers(); // TODO: determine if arguments needed
    }

    @Override
    public void setControlUnit(ControlUnitInterface cu) {
        observable_data.setControlUnit(cu);
        isControlUnitPipelined = cu.isPipelined();
    }

    @Override
    public int dAddrWidth() {
        return observable_data.dAddrWidth();
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
    public int dataWidth() {
        return observable_data.dataWidth();
    }
}
