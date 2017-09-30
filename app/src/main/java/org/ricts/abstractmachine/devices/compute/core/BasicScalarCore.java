package org.ricts.abstractmachine.devices.compute.core;

import org.ricts.abstractmachine.components.compute.core.AluCore;
import org.ricts.abstractmachine.components.compute.core.UniMemoryComputeAltCore;
import org.ricts.abstractmachine.components.compute.interrupt.InterruptSource;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.storage.Register;
import org.ricts.abstractmachine.components.storage.RegisterStack;
import org.ricts.abstractmachine.devices.compute.alu.BasicALU;
import org.ricts.abstractmachine.devices.compute.interrupt.PIC16F877ATimer0;

/**
 * Created by jevon.beckles on 18/08/2017.
 */

public class BasicScalarCore extends UniMemoryComputeAltCore {
    private static final int TMR0_REG_INDEX = PIC16F877ATimer0.Regs.TMR0.ordinal();
    private static final int OPTIONS_REG_INDEX = PIC16F877ATimer0.Regs.OPTION_REG.ordinal();
    private static final int INTERRUPT_VECTOR_ADDRESS = 0x1;
    
    /* core dependent features */
    public enum StatusFlags {
        CARRY, // CarryIn/BorrowIn
        OVERFLOW, // CarryOut/ActiveLowBorrowOut
        ZERO, // indicates when result is zero
        SIGN // sign of result
    }

    public enum InterruptFlags {
        TMR0, STACKOFLOW, STACKUFLOW
    }

    public enum ControlRegFlags {
        INTERRUPTS
    }

    private Register pcReg;
    private Register statusReg;
    private Register controlReg;
    private Register intEnableReg; // interrupt enable
    private Register intFlagsReg; // interrupt flags
    private Register intSourceEnableReg; // interrupt source enable
    private Register[] dataRegs; // (no. of) registers for manipulating data
    private Register[] dataAddrRegs; // (no. of) registers for storing data addresses
    private Register[] instrAddrRegs; // (no. of) registers for storing instruction addresses (temporarily)
    private RegisterStack callStack; // presence or absence of on-chip call stack
    private BasicALU alu; // operations allowed by ALU

    private BasicScalarDecoder decoderCore;
    private Register tmr0Reg, optionsReg;
    private int iAddrWidth, dAddrWidth, dataWidth;

    public BasicScalarCore(ObservableDecoderUnit decoder) {
        super(decoder);
        decoderCore = (BasicScalarDecoder) decoder.getType();
        alu = (BasicALU) aluCore;

        iAddrWidth = decoderCore.iAddrWidth();
        dataWidth = decoderCore.dataWidth();
        dAddrWidth = decoderCore.dAddrWidth();
    }

    @Override
    public boolean isEnabled(String sourceName) {
        InterruptFlags interrupt = Enum.valueOf(InterruptFlags.class, sourceName);
        return getBitAtIndex(interrupt.ordinal(), intSourceEnableReg.read());
    }

    @Override
    public void raiseInterrupt(String sourceName) {
        InterruptFlags interrupt = Enum.valueOf(InterruptFlags.class, sourceName);
        intFlagsReg.write(setBitAtIndex(interrupt.ordinal(), intFlagsReg.read()));
    }

    @Override
    public int[] getRegData(String sourceName) {
        int [] result;

        InterruptFlags interrupt = Enum.valueOf(InterruptFlags.class, sourceName);
        switch(interrupt) {
            case TMR0:
                result = new int[PIC16F877ATimer0.Regs.values().length];
                result[TMR0_REG_INDEX] = tmr0Reg.read();
                result[OPTIONS_REG_INDEX] = optionsReg.read();
                break;
            default:
                result = new int[0];
        }

        return result;
    }

    @Override
    public void setRegData(String sourceName, int[] data) {
        InterruptFlags interrupt = Enum.valueOf(InterruptFlags.class, sourceName);
        switch(interrupt) {
            case TMR0:
                tmr0Reg.write(data[TMR0_REG_INDEX]);
                break;
        }
    }

    @Override
    public int getProgramCounterValue() {
        return pcReg.read();
    }

    @Override
    public void reset() {
        int stackAddrWidth = decoderCore.getStackAddrWidth();
        int dataRegAddrWidth = decoderCore.getDataRegAddrWidth();
        int dAddrRegAddrWidth = decoderCore.getdAddrRegAddrWidth();
        int iAddrRegAddrWidth = decoderCore.getiAddrRegAddrWidth();

        /* Initialise core units */
        alu = new BasicALU(dataWidth);
        callStack = new RegisterStack(iAddrWidth, 1 << stackAddrWidth); // stack with size 2^stackAddrWidth

	    /* Initialise registers */
        dataRegs = new Register[1 << dataRegAddrWidth]; // for data (2^dataRegAddrWidth regs)
        pcReg = new Register(iAddrWidth);
        statusReg = new Register(StatusFlags.values().length);
        intEnableReg = new Register(InterruptFlags.values().length);
        intFlagsReg = new Register(InterruptFlags.values().length);
        intSourceEnableReg = new Register(InterruptFlags.values().length);
        controlReg = new Register(dataWidth);
        optionsReg = new Register(dataWidth);
        tmr0Reg = new Register(dataWidth);

        int dataRegAddrCount = 0;
        dataRegs[dataRegAddrCount++] = statusReg;
        dataRegs[dataRegAddrCount++] = intEnableReg;
        dataRegs[dataRegAddrCount++] = intFlagsReg;
        dataRegs[dataRegAddrCount++] = intSourceEnableReg;
        dataRegs[dataRegAddrCount++] = controlReg;
        dataRegs[dataRegAddrCount++] = optionsReg;
        dataRegs[dataRegAddrCount++] = tmr0Reg;
        for (int x = dataRegAddrCount; x < dataRegs.length; ++x) {
            dataRegs[x] = new Register(dataWidth);
        }

        dataAddrRegs = new Register[1 << dAddrRegAddrWidth]; // for data addresses (2^dAddrRegAddrWidth regs)
        for (int x = 0; x < dataAddrRegs.length; ++x) {
            dataAddrRegs[x] = new Register(dAddrWidth);
        }

        instrAddrRegs = new Register[1 << iAddrRegAddrWidth]; // for instruction addresses (2^iAddrRegAddrWidth regs)
        int instrAddrRegAddrCount = 0;
        instrAddrRegs[instrAddrRegAddrCount++] = pcReg;
        for (int x = instrAddrRegAddrCount; x < instrAddrRegs.length; ++x) {
            instrAddrRegs[x] = new Register(iAddrWidth);
        }
    }

    @Override
    protected AluCore createALU(int dataWidth) {
        return new BasicALU(dataWidth);
    }

    @Override
    public int instrExecTime(String instructionGroupName, int instructionGroupIndex) {
        BasicScalarDecoder.InstructionGrouping grouping = Enum.valueOf(BasicScalarDecoder.InstructionGrouping.class, instructionGroupName);
        BasicScalarDecoder.Instruction instruction = grouping.decode(instructionGroupIndex);

        switch (grouping){
            case DataMemOps:
                // Instructions with 1 data register and 1 data address register
                switch (instruction) {
                    case LOADM: // DREG <-- MEMORY[DADREG] (dereference pointer and assign value to variable)
                    case STOREM: // MEMORY[DADREG] <-- DREG (assign variable to dereferenced pointer)
                        return dataMemory.accessTime();
                    default:
                        return 1;
                }
            default:
                return 1;
        }
    }

    @Override
    protected void fetchOpsExecuteInstr(String groupName, int groupIndex, int[] operands) {
        BasicScalarDecoder.InstructionGrouping grouping = Enum.valueOf(BasicScalarDecoder.InstructionGrouping.class, groupName);
        BasicScalarDecoder.Instruction instruction = grouping.decode(groupIndex);

        int regAddr, destRegAddr, sourceRegAddr, byteLiteral, dRegAddr, bitIndex;
        switch (grouping){
            case NoOperands:
                // Instructions with 0 operands
                switch (instruction) {
                    case POP: // cu <-- predefinedStack.pop(); updateUnderflowFlag(); ('return' control-flow construct)
                        popCallStack();
                        break;
                    case NOP: // do nothing
                        break;
                    case HALT: // tell Control Unit to stop execution
                        setInternalControlUnitState(ControlUnitState.HALT);
                        break;
                    case RETFIE:
                        controlReg.write(setBitAtIndex(ControlRegFlags.INTERRUPTS.ordinal(), controlReg.read())); // re-enable interrupts
                        popCallStack();
                        break;
                }
                break;
            case InstrAddressReg:
                // Instructions with 1 instruction address register
                switch (instruction) {
                    case JUMP: // cu <-- IADREG ('goto'/'break'/'continue' control-flow construct)
                        updateProgramCounter(instrAddrRegs[operands[0]].read());
                        break;
                    case PUSH: // predefStack.push(IADREG); updateOverflowFlag(); (part of 'function-call' control-flow construct)
                        pushCallStack(instrAddrRegs[operands[0]].read());
                        break;
                    case STOREPC: // IADREG <-- cu (part of 'switch' statement / look-up table / 'function-call' control-flow construct)
                        instrAddrRegs[operands[0]].write(pcReg.read());
                        break;
                }
                break;
            case InstrAddressLiteral:
                // Instructions with 1 instruction address literal
                switch (instruction) {
                    case JUMPL: // cu <-- INSTRLIT ('goto'/'break'/'continue' control-flow construct)
                        updateProgramCounter(operands[0]);
                        break;
                }
                break;
            case DataAssignLit:
                // Instructions with 1 data register and 1 data literal
                switch (instruction) {
                    case LOAD: // DREG <-- DATA (variable assignment)
                        dataRegs[operands[0]].write(operands[1]);
                        break;
                }
                break;
            case RegBitManip:
                // Instructions with 1 data register and 1 bit-index
                regAddr = operands[0];
                bitIndex = operands[1];

                switch (instruction) {
                    case SETB: // DREG[BITINDEX] <-- true (boolean variable assignment)
                        dataRegs[regAddr].write(setBitAtIndex(bitIndex, dataRegs[regAddr].read()));
                        break;
                    case CLRB: // DREG[BITINDEX] <-- false (boolean variable assignment)
                        dataRegs[regAddr].write(clearBitAtIndex(bitIndex, dataRegs[regAddr].read()));
                        break;
                }
                break;
            case DataMemOps:
                // Instructions with 1 data register and 1 data address register
                dRegAddr = operands[0];
                int dAddrRegAddr = operands[1];

                switch (instruction) {
                    case LOADM: // DREG <-- MEMORY[DADREG] (dereference pointer and assign value to variable)
                        dataRegs[dRegAddr].write(dataMemory.read(dataAddrRegs[dAddrRegAddr].read()));
                        break;
                    case STOREM: // MEMORY[DADREG] <-- DREG (assign variable to dereferenced pointer)
                        dataMemory.write(dataAddrRegs[dAddrRegAddr].read(), dataRegs[dRegAddr].read());
                        break;
                    case LOADA: // DREG <-- (int) DADREG (put pointer address value in variable - for pointer arithmetic)
                        dataRegs[dRegAddr].write(dataAddrRegs[dAddrRegAddr].read());
                        break;
                    case STOREA: // DADREG <-- (data address) DREG (pointer assignment) [OS level operation / result of call to 'new']
                        dataAddrRegs[dAddrRegAddr].write(dataRegs[dRegAddr].read());
                        break;
                }
                break;
            case InstrAddrConvert:
                // Instructions with 1 data register and 1 instruction address register
                dRegAddr = operands[0];
                int iAddrRegAddr = operands[1];

                switch (instruction) {
                    case LOADI: // DREG <-- (int) IADREG (dereference pointer and assign value to variable)
                        dataRegs[dRegAddr].write(instrAddrRegs[iAddrRegAddr].read());
                        break;
                    case STOREI: // IADREG <-- (instruction address) DREG [OS level operation / load start address of new program]
                        instrAddrRegs[iAddrRegAddr].write(dataRegs[dRegAddr].read());
                        break;
                }
                break;
            case TwoRegOps:
                // Instructions with 2 data registers (destination, source)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];

                updateAluCarry();
                switch (instruction) {
                    case MOVE: // DESTINATION <-- SOURCE (variable assignment)
                        dataRegs[destRegAddr].write(dataRegs[sourceRegAddr].read());
                        break;
                    case NOT: // DESTINATION <-- 1's_COMPLEMENT(SOURCE)
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.COMP, dataRegs[sourceRegAddr].read()));
                        break;
                    case RLC: // DESTINATION <-- ROTATE_LEFT_WITH_CARRY(SOURCE)
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.RLC, dataRegs[sourceRegAddr].read()));
                        break;
                    case RRC: // DESTINATION <-- ROTATE_RIGHT_WITH_CARRY(SOURCE)
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.RRC, dataRegs[sourceRegAddr].read()));
                        break;
                    case INC: // DESTINATION <-- SOURCE + 1
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.INC, dataRegs[sourceRegAddr].read()));
                        break;
                    case DEC: // DESTINATION <-- SOURCE - 1 (useful for end of array indexing with DESTINATION)
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.DEC, dataRegs[sourceRegAddr].read()));
                        break;
                }
                updateStatusReg();
                break;
            case ByteLoad:
                // Instructions with 1 data register, 1 byte-index and 1 byte literal
                regAddr = operands[0];
                int byteIndex = operands[1];
                byteLiteral = operands[2];

                switch (instruction) {
                    case LOADBYTE: // DREG[BYTEINDEX] <-- BYTE (ASCII/UTF-BYTE_WIDTH character literal assignment)
                        dataRegs[regAddr].write(setWordIn(dataRegs[regAddr].read(), byteLiteral, BYTE_WIDTH, BYTE_WIDTH * byteIndex));
                        break;
                }
                break;
            case ConditionalBranch:
                // Instructions with 1 data register, 1 bit-index and 1 instruction address register
                dRegAddr = operands[0];
                bitIndex = operands[1];
                int iAddrRegValue = instrAddrRegs[operands[2]].read();

                switch (instruction) {
                    case JUMPIFBC: // IF (!DREG[BITINDEX]) cu <-- IADREG ('for'/'while'/'if-else' sourceReg[bitIndex])
                        if (!getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                            updateProgramCounter(iAddrRegValue);
                        }
                        break;
                    case JUMPIFBS: // IF (DREG[BITINDEX]) cu <-- IADREG ('do-while' sourceReg[bitIndex])
                        if (getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                            updateProgramCounter(iAddrRegValue);
                        }
                        break;
                }
                break;
            case ConditionalBranchLiteral:
                // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
                dRegAddr = operands[0];
                bitIndex = operands[1];
                int iAddrLiteral = operands[2];

                switch (instruction) {
                    case JUMPIFBCL: // IF (!DREG[BITINDEX]) cu <-- IADLITERAL ('for'/'while'/'if-else' sourceReg[bitIndex])
                        if (!getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                            updateProgramCounter(iAddrLiteral);
                        }
                        break;
                    case JUMPIFBSL: // IF (DREG[BITINDEX]) cu <-- IADLITERAL ('do-while' sourceReg[bitIndex])
                        if (getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                            updateProgramCounter(iAddrLiteral);
                        }
                        break;
                }
                break;
            case ShiftReg:
                // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];
                int shiftAmount = operands[2];

                switch (instruction) {
                    case SHIFTL: // DESTINATION <-- (SOURCE << SHIFTAMOUNT)
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.SHIFTL, dataRegs[sourceRegAddr].read(), shiftAmount));
                        break;
                    case SHIFTR: // DESTINATION <-- (SOURCE >> SHIFTAMOUNT)
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.SHIFTR, dataRegs[sourceRegAddr].read(), shiftAmount));
                        break;
                }
                break;
            case AluOps:
                // Instructions with 3 data registers (result, A, B)
                destRegAddr = operands[0];
                int aRegAddr = operands[1];
                int bRegAddr = operands[2];

                updateAluCarry();
                switch (instruction) {
                    case ADD: // RESULT <-- A + B
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.ADD, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case ADDWC: // RESULT <-- A + B + CARRY
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.ADDWC, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case SUB: // RESULT <-- A - B
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.SUB, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case SUBWB: // RESULT <-- A - B + BORROW
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.SUBWB, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case AND: // RESULT <-- A & B
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.AND, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case OR: // RESULT <-- A | B
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.OR, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case XOR: // RESULT <-- A ^ B
                        dataRegs[destRegAddr].write(alu.result(BasicALU.Mneumonics.XOR, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                }
                updateStatusReg();
                break;
            case RegByteManip:
                // Instructions with 2 data registers (destination, source) and 2 byte-indices (destination, source)
                destRegAddr = operands[0];
                int destByteIndex = operands[1];
                sourceRegAddr = operands[2];
                int sourceByteIndex = operands[3];

                byteLiteral = getWordFrom(dataRegs[sourceRegAddr].read(), BYTE_WIDTH, BYTE_WIDTH * sourceByteIndex);

                switch (instruction) {
                    case MOVEBYTE: // DESTINATION[DBYTEINDEX] <-- SOURCE[SBYTEINDEX] (ASCII/UTF-8 character move)
                        dataRegs[destRegAddr].write(setWordIn(dataRegs[destRegAddr].read(), byteLiteral, BYTE_WIDTH, BYTE_WIDTH * destByteIndex));
                        break;
                }
                break;
            case MultiWidthAluOps:
                // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
                int byteMultiplier = operands[3];
                int bitWidth = (byteMultiplier != 0) ? BYTE_WIDTH * byteMultiplier : 4;
                int byteMask = bitMaskOfWidth(bitWidth);

                int resultRegAddr = operands[0];
                int A = dataRegs[operands[1]].read() & byteMask;
                int B = dataRegs[operands[2]].read() & byteMask;

                updateAluCarry();
                alu.result(BasicALU.Mneumonics.UPDATEWIDTH, bitWidth); // temporarily modify ALU dataWidth to set appropriately set ALU flags
                switch (instruction) {
                    case ADDWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] + B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(BasicALU.Mneumonics.ADD, A, B) & byteMask);
                        break;
                    case ADDCWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] + B[((BYTEMULT*8)-1):0] + CARRY
                        dataRegs[resultRegAddr].write(alu.result(BasicALU.Mneumonics.ADDWC, A, B) & byteMask);
                        break;
                    case SUBWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] - B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(BasicALU.Mneumonics.SUB, A, B) & byteMask);
                        break;
                    case SUBCWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] - B[((BYTEMULT*8)-1):0] + BORROW
                        dataRegs[resultRegAddr].write(alu.result(BasicALU.Mneumonics.SUBWB, A, B) & byteMask);
                        break;
                    case ANDWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] & B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(BasicALU.Mneumonics.AND, A, B) & byteMask);
                        break;
                    case ORWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] | B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(BasicALU.Mneumonics.OR, A, B) & byteMask);
                        break;
                    case XORWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] ^ B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(BasicALU.Mneumonics.XOR, A, B) & byteMask);
                        break;
                }
                alu.result(BasicALU.Mneumonics.UPDATEWIDTH, dataWidth); // restore ALU dataWidth
                updateStatusReg(); // flags are in accordance with previously set dataWidth
                break;
            default:
                break;
        }
    }

    @Override
    protected void vectorToInterruptHandler() {
        if(getBitAtIndex(ControlRegFlags.INTERRUPTS.ordinal(), controlReg.read())){ // interrupts enabled globally
            // Interrupt enable bits and flags are aligned in respective registers.
            // Logical AND comparison determines whether interrupts are enabled and flags raised.
            int compareResult = intEnableReg.read() & intFlagsReg.read();

            // As this device has only 1 interrupt vector, any non-zero result for logical AND comparison
            // results in vectoring to that location.
            if(compareResult != 0) {
                // temporarily disable interrupts to avoid further interrupts
                controlReg.write(clearBitAtIndex(ControlRegFlags.INTERRUPTS.ordinal(), controlReg.read()));

                pushCallStack(pcReg.read()); // store current program location
                updateProgramCounter(INTERRUPT_VECTOR_ADDRESS); // go to interrupt vector
            }
        }
    }

    @Override
    protected void updateProgramCounterRegs(int programCounter) {
        pcReg.write(programCounter);
    }

    @Override
    protected InterruptSource[] createInterruptSources() {
        return new InterruptSource[]{
                new PIC16F877ATimer0(InterruptFlags.TMR0.name(), this)
        };
    }

    private void updateAluCarry() {
        if (getBitAtIndex(StatusFlags.CARRY.ordinal(), statusReg.read())) {
            alu.result(BasicALU.Mneumonics.SETC);
        } else {
            alu.result(BasicALU.Mneumonics.CLRC);
        }
    }
    
    private void updateStatusReg() {
        statusReg.write(setBitValueAtIndex(StatusFlags.OVERFLOW.ordinal(), statusReg.read(), alu.overflowFlag()));
        statusReg.write(setBitValueAtIndex(StatusFlags.SIGN.ordinal(), statusReg.read(), alu.signFlag()));
        statusReg.write(setBitValueAtIndex(StatusFlags.ZERO.ordinal(), statusReg.read(), alu.zeroFlag()));
    }

    private void pushCallStack(int value) {
        callStack.push(value);
        intFlagsReg.write(setBitValueAtIndex(InterruptFlags.STACKOFLOW.ordinal(),
                intFlagsReg.read(), callStack.isFull()));
    }

    private void popCallStack() {
        updateProgramCounter(callStack.pop());
        intFlagsReg.write(setBitValueAtIndex(InterruptFlags.STACKUFLOW.ordinal(),
                intFlagsReg.read(), callStack.isEmpty()));
    }
}
