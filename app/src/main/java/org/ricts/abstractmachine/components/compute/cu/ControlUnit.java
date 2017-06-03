package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.core.UniMemoryCpuCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;
import org.ricts.abstractmachine.components.interfaces.DefaultValueSource;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ControlUnit extends ControlUnitCore {
    private static final int DATA_MEM_ID = UniMemoryCpuCore.SerializerInputId.DATA_MEM.ordinal();
    private static final int INS_MEM_ID = UniMemoryCpuCore.SerializerInputId.INSTRUCTION_MEM.ordinal();

    private Multiplexer mux;
    private ControlUnitFSM fsm;

    public ControlUnit(ComputeCoreInterface core, ReadPort instructionCache, Multiplexer muxInterface){
        super(core, instructionCache);
        mux = muxInterface;

        // initialise
        reset();
    }

    @Override
    public void performNextAction(){
        int selection = fsm.isInExecuteState() ? DATA_MEM_ID : INS_MEM_ID;
        mux.setSelection(selection);

        mainFSM.triggerStateChange();
    }

    @Override
    public int nextActionDuration(){ // in clock cycles
        return fsm.nextActionDuration();
    }

    @Override
    public void setNextFetch(int instructionAddress){
        regCore.setPC(instructionAddress);
        mainFSM.setNextState(FETCH_STATE);
    }

    @Override
    public void setNextFetchAndExecute(int instructionAddress, int nopInstruction) {
        // This method is only implemented by pipelined control unit
    }

    @Override
    public boolean isPipelined() {
        return false;
    }

    @Override
    protected CuFsmInterface createMainFSM(ControlUnitRegCore regCore, ComputeCoreInterface core) {
        fsm = new ControlUnitFSM(regCore, core);
        return fsm;
    }

    @Override
    protected DefaultValueSource createDefaultValueSource() {
        return new DefaultValueSource(){
            @Override
            public int defaultValue() {
                return 0;
            }
        };
    }

    @Override
    protected CuRegCore createRegCore(ReadPort instructionCache, int pcWidth, int irWidth) {
        return new CuRegCore(instructionCache, pcWidth, irWidth);
    }

    @Override
    protected void resetInternal() {
        mux.setSelection(INS_MEM_ID);
    }
}