package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

/**
 * Created by Jevon on 09/07/2016.
 */
public class PipelinedControlUnit implements CuDataInterface {
    private boolean branched;
    private String currentState, activeString, haltString, sleepString;

    private ComputeCoreInterface mainCore;
    private ControlUnitEngine cu1; // Control Unit for TU 1
    private ControlUnitEngine cu2; // Control Unit for TU 2

    private Register expectedPC, branchPC, realPC;
    private Register expectedIR, branchIR, realIR;

    public PipelinedControlUnit(ComputeCoreInterface core, ReadPort instructionCache, MemoryPort dataMemory){
        mainCore = core;

        int iAddrWidth = core.iAddrWidth();
        int instrWidth = core.instrWidth();
        expectedPC = new Register(iAddrWidth);
        expectedIR = new Register(instrWidth);

        branchPC = new Register(iAddrWidth);
        branchIR = new Register(instrWidth);

        realPC = new Register(iAddrWidth);
        realIR = new Register(instrWidth);

        activeString = "active";
        haltString = "halt";
        sleepString = "sleep";

        /* N.B. : Both thread units are connected to the same instructionCache and dataMemory!
           During normal operation, one performs a fetch while the other executes... ALWAYS! */

        // thread unit 1 (TU 1) - initial state = 'fetch'
        cu1 = new ControlUnitEngine(this, core, instructionCache, dataMemory);

        // thread unit 2 (TU 2) - initial state = 'execute'
        cu2 = new ControlUnitEngine(this, core, instructionCache, dataMemory);

        // initialise thread units
        reset();
    }

    @Override
    public void setNextFetch(int instructionAddress) {
        // This method is only implemented by non-pipelined control unit
    }

    @Override
    public void setNextFetchAndExecute(int instructionAddress, int nopInstruction) {
        if(isNormalExecution()) { // if normal execution ...
            // ... set branch registers
            branched = true;
            setBranchPC(instructionAddress);
            setBranchIR(nopInstruction);
        }
        else { // ... we need to explicitly set each CU state
            currentState = activeString;

            setRealPC(instructionAddress);
            setRealIR(nopInstruction);
            cu1.setToFetchState();
            cu2.setToExecuteState();
        }
    }

    @Override
    public void reset() {
        setStartExecFrom(0);
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        currentState = activeString;
        branched = false;

        setRealPC(currentPC);
        setRealIR(mainCore.getNopInstruction());
        cu1.setToFetchState();
        cu2.setToExecuteState();
    }

    @Override
    public boolean isPipelined() {
        return true;
    }

    @Override
    public void setNextStateToHalt() {
        // cu1 and cu2 do nothing
        cu1.setNextStateToHalt();
        cu2.setNextStateToHalt();
        currentState = haltString;
    }

    @Override
    public void setNextStateToSleep() {
        // cu1 does nothing while cu2 executes sleep
        cu1.setNextStateToHalt();
        cu2.setNextStateToSleep();
        currentState = sleepString;
    }

    @Override
    public boolean isInHaltState() {
        return currentState.equals(haltString);
    }

    @Override
    public boolean isInSleepState() {
        return currentState.equals(sleepString);
    }

    @Override
    public void performNextAction() {
        // advance both thread units
        cu1.triggerStateChange();
        cu2.triggerStateChange();

        if(isNormalExecution()){
            /*
             * N.B: Only after both thread units have 'executed' can a proper assessment of thread unit synchronisation be made.
             * If one unit executes while the other fetches, then for non-branching instruction, the PC should be one more than the previous value.
             * This is true since the fetch stage increments the PC, while the execute stage only modifies PC if the instruction is branching.
             * If a branch is detected, the next instruction to execute should be a NOP.
             * */

            if(branched){ // if branch has occurred ...
                // ... don't execute instruction that was just fetched!
                setRealPC(getBranchPC());
                setRealIR(getBranchIR());
                branched = false;
            }
            else {
                setRealPC(getExpectedPC());
                setRealIR(getExpectedIR());
            }
        }
    }

    @Override
    public int nextActionDuration() {
        return Math.max(cu1.nextActionDuration(), cu2.nextActionDuration());
    }

    @Override
    public void fetchInstruction(ReadPort instructionCache) {
        int pcValue = getPC();
        setExpectedIR(instructionCache.read(pcValue)); // IR = iCache[PC]
        setExpectedPC(pcValue + 1); // PC += 1
    }

    @Override
    public int getPC() {
        return realPC.read();
    }

    @Override
    public int getIR() {
        return realIR.read();
    }

    @Override
    public String getPCDataString() {
        return realPC.dataString();
    }

    @Override
    public String getIRDataString() {
        return realIR.dataString();
    }

    @Override
    public String getCurrentStateString(){
        return currentState;
    }

    public ControlUnitEngine getCu1(){
        return cu1;
    }

    public ControlUnitEngine getCu2(){
        return cu2;
    }

    private boolean isNormalExecution(){
        return currentState.equals(activeString);
    }

    private int getExpectedPC(){
        return expectedPC.read();
    }

    private void setExpectedPC(int currentPC){
        expectedPC.write(currentPC);
    }

    private int getExpectedIR(){
        return expectedIR.read();
    }

    private void setExpectedIR(int currentIR){
        expectedIR.write(currentIR);
    }

    private int getBranchPC(){
        return branchPC.read();
    }

    private void setBranchPC(int currentPC){
        branchPC.write(currentPC);
    }

    private int getBranchIR(){
        return branchIR.read();
    }

    private void setBranchIR(int currentIR){
        branchIR.write(currentIR);
    }

    private void setRealPC(int currentPC){
        realPC.write(currentPC);
    }

    private void setRealIR(int currentIR){
        realIR.write(currentIR);
    }
}
