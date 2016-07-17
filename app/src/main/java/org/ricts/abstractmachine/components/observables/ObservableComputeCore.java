package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;

/**
 * Created by Jevon on 16/01/2016.
 */
public class ObservableComputeCore<T extends ComputeCore> extends ObservableType<T> implements ComputeCoreInterface {

    public ObservableComputeCore(T core){
        super(core);
    }

    public static class ExecuteParams extends ObservableComputeCore.Params {
        protected enum Args{
            INSTRUCTION, PC_PRE_EXECUTED, PC_POST_EXECUTED
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
    }

    @Override
    public void executeInstruction(int programCounter, int instruction, MemoryPort dataMemory, ControlUnitInterface cu) {
        observable_data.executeInstruction(programCounter, instruction, dataMemory, cu);
        setChanged();
        notifyObservers(new ExecuteParams(instruction, programCounter, observable_data.getProgramCounterValue()));
    }

    @Override
    public int instrExecTime(int instruction, MemoryPort dataMemory) {
        return observable_data.instrExecTime(instruction, dataMemory);
    }

    @Override
    public void reset() {
        observable_data.reset();
        setChanged();
        notifyObservers(true); // to differentiate that this update is a reset!
    }

    @Override
    public int nopInstruction() {
        return observable_data.nopInstruction();
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
