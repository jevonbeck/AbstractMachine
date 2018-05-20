package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.core.UniMemoryCpuAltCore;
import org.ricts.abstractmachine.components.compute.cu.fsm.ControlUnitAltFSM;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;
import org.ricts.abstractmachine.components.interfaces.FetchCore;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ControlUnitAlt extends ControlUnitAltCore {
    private static final int DATA_MEM_ID = UniMemoryCpuAltCore.SerializerInputId.DATA_MEM.ordinal();
    private static final int INS_MEM_ID = UniMemoryCpuAltCore.SerializerInputId.INSTRUCTION_MEM.ordinal();

    private Multiplexer mux;
    private ControlUnitAltFSM fsm;

    public ControlUnitAlt(CompCore core, ReadPort instructionCache, Multiplexer muxInterface){
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
    protected CuFsmInterface createMainFSM(FetchCore regCore, CompCore core) {
        fsm = new ControlUnitAltFSM(regCore, core);
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
    protected FetchUnit createRegCore(ReadPort instructionCache, int pcWidth, int irWidth) {
        return new FetchUnit(instructionCache, pcWidth, irWidth);
    }

    @Override
    protected void resetInternal() {
        mux.setSelection(INS_MEM_ID);
    }
}