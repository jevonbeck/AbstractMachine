package org.ricts.abstractmachine.devices.compute.core;

import org.ricts.abstractmachine.components.compute.core.AbstractUniMemoryComputeCore;
import org.ricts.abstractmachine.components.compute.core.AluCore;
import org.ricts.abstractmachine.components.compute.interrupt.InterruptSource;
import org.ricts.abstractmachine.components.interfaces.Bit;
import org.ricts.abstractmachine.components.interfaces.Register;
import org.ricts.abstractmachine.components.interfaces.RegisterFile;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.storage.AbstractRegisterFile;
import org.ricts.abstractmachine.components.storage.RegisterImpl;
import org.ricts.abstractmachine.components.storage.RegisterStack;
import org.ricts.abstractmachine.devices.compute.alu.RegisteredALU;
import org.ricts.abstractmachine.devices.compute.interrupt.PIC16F877ATimer0;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jevon.beckles on 18/08/2017.
 */

public class BasicScalarCore extends AbstractUniMemoryComputeCore {
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

    public enum NamedRegister {
        STATUS(0), INT_ENABLE(1), INT_FLAGS(2), CONTROL(3), OPTIONS_REG(4), TMR0(5);

        private int address;

        NamedRegister(int addr) {
            address = addr;
        }

        public int getAddress() {
            return address;
        }
    }

    private Register pcReg;
    private Register intEnableReg; // interrupt enable
    private Register intFlagsReg; // interrupt flags
    private Register[] dataAddrRegs; // (no. of) registers for storing data addresses
    private Register[] instrAddrRegs; // (no. of) registers for storing instruction addresses (temporarily)
    private RegisterStack callStack; // presence or absence of on-chip call stack
    private RegisterFile dataRegFile;
    private Bit globalInterruptEnable;
    private InterruptSource [] interruptSources;

    private BasicScalarDecoder decoderCore;
    private RegisteredALU aluCore;
    private Register tmr0Reg, optionsReg;
    private int iAddrWidth, dAddrWidth, dataWidth;

    public BasicScalarCore(ObservableDecoderUnit decoder) {
        super(decoder);
        decoderCore = (BasicScalarDecoder) decoder.getType();

        iAddrWidth = decoderCore.iAddrWidth();
        dataWidth = decoderCore.dataWidth();
        dAddrWidth = decoderCore.dAddrWidth();
    }

    @Override
    public boolean isEnabled(String sourceName) {
        InterruptFlags interrupt = Enum.valueOf(InterruptFlags.class, sourceName);
        return intEnableReg.getBitAt(interrupt.ordinal()).read();
    }

    @Override
    public void raiseInterrupt(String sourceName) {
        InterruptFlags interrupt = Enum.valueOf(InterruptFlags.class, sourceName);
        intFlagsReg.getBitAt(interrupt.ordinal()).set();
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
        callStack = new RegisterStack(iAddrWidth, 1 << stackAddrWidth); // stack with size 2^stackAddrWidth

	    /* Initialise registers */
        dataRegFile = new BasicScalarRegFile(decoderCore.dataWidth(), dataRegAddrWidth);

        Register statusReg = dataRegFile.getRegisterByName(NamedRegister.STATUS.name());
        aluCore = new RegisteredALU(
                new AluCore(decoderCore.dataWidth(),
                statusReg.getBitAt(StatusFlags.CARRY.ordinal()), statusReg.getBitAt(StatusFlags.SIGN.ordinal()),
                statusReg.getBitAt(StatusFlags.ZERO.ordinal()), statusReg.getBitAt(StatusFlags.OVERFLOW.ordinal()))
        );

        pcReg = new RegisterImpl(iAddrWidth);
        intEnableReg = dataRegFile.getRegisterByName(NamedRegister.INT_ENABLE.name());
        intFlagsReg = dataRegFile.getRegisterByName(NamedRegister.INT_FLAGS.name());

        Register controlReg = dataRegFile.getRegisterByName(NamedRegister.CONTROL.name());
        globalInterruptEnable = controlReg.getBitAt(ControlRegFlags.INTERRUPTS.ordinal());

        optionsReg = dataRegFile.getRegisterByName(NamedRegister.OPTIONS_REG.name());
        tmr0Reg = dataRegFile.getRegisterByName(NamedRegister.TMR0.name());

        interruptSources = createInterruptSources();

        dataAddrRegs = new Register[1 << dAddrRegAddrWidth]; // for data addresses (2^dAddrRegAddrWidth regs)
        for (int x = 0; x < dataAddrRegs.length; ++x) {
            dataAddrRegs[x] = new RegisterImpl(dAddrWidth);
        }

        instrAddrRegs = new Register[1 << iAddrRegAddrWidth]; // for instruction addresses (2^iAddrRegAddrWidth regs)
        int instrAddrRegAddrCount = 0;
        instrAddrRegs[instrAddrRegAddrCount++] = pcReg;
        for (int x = instrAddrRegAddrCount; x < instrAddrRegs.length; ++x) {
            instrAddrRegs[x] = new RegisterImpl(iAddrWidth);
        }
    }

    @Override
    public int instrExecTime(String mneumonic) {
        BasicScalarDecoder.Instruction instruction = Enum.valueOf(BasicScalarDecoder.Instruction.class, mneumonic);
        switch (instruction) {
            case LOADM: // DREG <-- MEMORY[DADREG] (dereference pointer and assign value to variable)
            case STOREM: // MEMORY[DADREG] <-- DREG (assign variable to dereferenced pointer)
                return dataMemory.accessTime();
            default:
                return 1;
        }
    }

    @Override
    protected void fetchOpsExecuteInstr(String mneumonic, int[] operands) {
        BasicScalarDecoder.Instruction instruction = Enum.valueOf(BasicScalarDecoder.Instruction.class, mneumonic);

        int regAddr, destRegAddr, aRegAddr, bRegAddr, sourceRegAddr, byteLiteral, bitIndex, dRegAddr;
        switch (instruction) {
            // Instructions with 0 operands
            case POP: // cu <-- predefinedStack.pop(); updateUnderflowFlag(); ('return' control-flow construct)
                popCallStack();
                break;
            case NOP: // do nothing
                break;
            case HALT: // tell Control Unit to stop execution
                setInternalControlUnitState(ControlUnitState.HALT);
                break;
            case RETFIE:
                globalInterruptEnable.set(); // re-enable interrupts
                popCallStack();
                break;

            // Instructions with 1 instruction address register
            case JUMP: // cu <-- IADREG ('goto'/'break'/'continue' control-flow construct)
                updateProgramCounter(instrAddrRegs[operands[0]].read());
                break;
            case PUSH: // predefStack.push(IADREG); updateOverflowFlag(); (part of 'function-call' control-flow construct)
                pushCallStack(instrAddrRegs[operands[0]].read());
                break;
            case STOREPC: // IADREG <-- cu (part of 'switch' statement / look-up table / 'function-call' control-flow construct)
                instrAddrRegs[operands[0]].write(pcReg.read());
                break;

            // Instructions with 1 instruction address literal
            case JUMPL: // cu <-- INSTRLIT ('goto'/'break'/'continue' control-flow construct)
                updateProgramCounter(operands[0]);
                break;

            // Instructions with 1 data register and 1 data literal
            case LOAD: // DREG <-- DATA (variable assignment)
                dataRegFile.write(operands[0], operands[1]);
                break;

            // Instructions with 1 data register and 1 bit-index
            case SETB: // DREG[BITINDEX] <-- true (boolean variable assignment)
            case CLRB: // DREG[BITINDEX] <-- false (boolean variable assignment)
                regAddr = operands[0];
                bitIndex = operands[1];
                switch (instruction) {
                    case SETB: // DREG[BITINDEX] <-- true (boolean variable assignment)
                        dataRegFile.getRegisterAt(regAddr).getBitAt(bitIndex).set();
                        break;
                    case CLRB: // DREG[BITINDEX] <-- false (boolean variable assignment)
                        dataRegFile.getRegisterAt(regAddr).getBitAt(bitIndex).clear();
                        break;
                }
                break;

            // Instructions with 1 data register and 1 data address register
            case LOADM: // DREG <-- MEMORY[DADREG] (dereference pointer and assign value to variable)
            case STOREM: // MEMORY[DADREG] <-- DREG (assign variable to dereferenced pointer)
            case LOADA: // DREG <-- (int) DADREG (put pointer address value in variable - for pointer arithmetic)
            case STOREA: // DADREG <-- (data address) DREG (pointer assignment) [OS level operation / result of call to 'new']
                dRegAddr = operands[0];
                int dAddrRegAddr = operands[1];
                switch (instruction) {
                    case LOADM: // DREG <-- MEMORY[DADREG] (dereference pointer and assign value to variable)
                        dataRegFile.write(dRegAddr, dataMemory.read(dataAddrRegs[dAddrRegAddr].read()));
                        break;
                    case STOREM: // MEMORY[DADREG] <-- DREG (assign variable to dereferenced pointer)
                        dataMemory.write(dataAddrRegs[dAddrRegAddr].read(), dataRegFile.read(dRegAddr));
                        break;
                    case LOADA: // DREG <-- (int) DADREG (put pointer address value in variable - for pointer arithmetic)
                        dataRegFile.write(dRegAddr, dataAddrRegs[dAddrRegAddr].read());
                        break;
                    case STOREA: // DADREG <-- (data address) DREG (pointer assignment) [OS level operation / result of call to 'new']
                        dataAddrRegs[dAddrRegAddr].write(dataRegFile.read(dRegAddr));
                        break;
                }
                break;

            // Instructions with 1 data register and 1 instruction address register
            case LOADI: // DREG <-- (int) IADREG (dereference pointer and assign value to variable)
            case STOREI: // IADREG <-- (instruction address) DREG [OS level operation / load start address of new program]
                dRegAddr = operands[0];
                int iAddrRegAddr = operands[1];
                switch (instruction) {
                    case LOADI: // DREG <-- (int) IADREG (dereference pointer and assign value to variable)
                        dataRegFile.write(dRegAddr, instrAddrRegs[iAddrRegAddr].read());
                        break;
                    case STOREI: // IADREG <-- (instruction address) DREG [OS level operation / load start address of new program]
                        instrAddrRegs[iAddrRegAddr].write(dataRegFile.read(dRegAddr));
                        break;
                }
                break;

            // Instructions with 2 data registers (destination, source)
            case MOVE: // DESTINATION <-- SOURCE (variable assignment)
            case NOT: // DESTINATION <-- 1's_COMPLEMENT(SOURCE)
            case RLC: // DESTINATION <-- ROTATE_LEFT_WITH_CARRY(SOURCE)
            case RRC: // DESTINATION <-- ROTATE_RIGHT_WITH_CARRY(SOURCE)
            case INC: // DESTINATION <-- SOURCE + 1
            case DEC: // DESTINATION <-- SOURCE - 1 (useful for end of array indexing with DESTINATION)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];

                switch (instruction) {
                    case MOVE: // DESTINATION <-- SOURCE (variable assignment)
                        dataRegFile.write(destRegAddr, dataRegFile.read(sourceRegAddr));
                        break;
                    case NOT: // DESTINATION <-- 1's_COMPLEMENT(SOURCE)
                        aluCore.onesComplement(dataRegFile.getRegisterAt(sourceRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case RLC: // DESTINATION <-- ROTATE_LEFT_WITH_CARRY(SOURCE)
                        aluCore.rotateLeftWithCarry(dataRegFile.getRegisterAt(sourceRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case RRC: // DESTINATION <-- ROTATE_RIGHT_WITH_CARRY(SOURCE)
                        aluCore.rotateRightWithCarry(dataRegFile.getRegisterAt(sourceRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case INC: // DESTINATION <-- SOURCE + 1
                        aluCore.increment(dataRegFile.getRegisterAt(sourceRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case DEC: // DESTINATION <-- SOURCE - 1 (useful for end of array indexing with DESTINATION)
                        aluCore.decrement(dataRegFile.getRegisterAt(sourceRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                }
                break;

            // Instructions with 1 data register, 1 byte-index and 1 byte literal
            case LOADBYTE: // DREG[BYTEINDEX] <-- BYTE (ASCII/UTF-BYTE_WIDTH character literal assignment)
                regAddr = operands[0];
                int byteIndex = operands[1];
                byteLiteral = operands[2];
                dataRegFile.write(regAddr, setWordIn(dataRegFile.read(regAddr), byteLiteral, BYTE_WIDTH, BYTE_WIDTH * byteIndex));
                break;

            // Instructions with 1 data register, 1 bit-index and 1 instruction address register
            case JUMPIFBC: // IF (!DREG[BITINDEX]) cu <-- IADREG ('for'/'while'/'if-else' sourceReg[bitIndex])
            case JUMPIFBS: // IF (DREG[BITINDEX]) cu <-- IADREG ('do-while' sourceReg[bitIndex])
                dRegAddr = operands[0];
                bitIndex = operands[1];
                int iAddrRegValue = instrAddrRegs[operands[2]].read();
                switch (instruction) {
                    case JUMPIFBC: // IF (!DREG[BITINDEX]) cu <-- IADREG ('for'/'while'/'if-else' sourceReg[bitIndex])
                        if (!dataRegFile.getRegisterAt(dRegAddr).getBitAt(bitIndex).read()) {
                            updateProgramCounter(iAddrRegValue);
                        }
                        break;
                    case JUMPIFBS: // IF (DREG[BITINDEX]) cu <-- IADREG ('do-while' sourceReg[bitIndex])
                        if (dataRegFile.getRegisterAt(dRegAddr).getBitAt(bitIndex).read()) {
                            updateProgramCounter(iAddrRegValue);
                        }
                        break;
                }
                break;

            // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
            case JUMPIFBCL: // IF (!DREG[BITINDEX]) cu <-- IADLITERAL ('for'/'while'/'if-else' sourceReg[bitIndex])
            case JUMPIFBSL: // IF (DREG[BITINDEX]) cu <-- IADLITERAL ('do-while' sourceReg[bitIndex])
                dRegAddr = operands[0];
                bitIndex = operands[1];
                int iAddrLiteral = operands[2];
                switch (instruction) {
                    case JUMPIFBCL: // IF (!DREG[BITINDEX]) cu <-- IADLITERAL ('for'/'while'/'if-else' sourceReg[bitIndex])
                        if (!dataRegFile.getRegisterAt(dRegAddr).getBitAt(bitIndex).read()) {
                            updateProgramCounter(iAddrLiteral);
                        }
                        break;
                    case JUMPIFBSL: // IF (DREG[BITINDEX]) cu <-- IADLITERAL ('do-while' sourceReg[bitIndex])
                        if (dataRegFile.getRegisterAt(dRegAddr).getBitAt(bitIndex).read()) {
                            updateProgramCounter(iAddrLiteral);
                        }
                        break;
                }
                break;

            // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
            case SHIFTL: // DESTINATION <-- (SOURCE << SHIFTAMOUNT)
            case SHIFTR: // DESTINATION <-- (SOURCE >> SHIFTAMOUNT)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];
                int shiftAmount = operands[2];
                switch (instruction) {
                    case SHIFTL: // DESTINATION <-- (SOURCE << SHIFTAMOUNT)
                        aluCore.logicalShiftLeft(dataRegFile.getRegisterAt(sourceRegAddr), shiftAmount, dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case SHIFTR: // DESTINATION <-- (SOURCE >> SHIFTAMOUNT)
                        aluCore.logicalShiftRight(dataRegFile.getRegisterAt(sourceRegAddr), shiftAmount, dataRegFile.getRegisterAt(destRegAddr));
                        break;
                }
                break;

            // Instructions with 3 data registers (result, A, B)
            case ADD: // RESULT <-- A + B
            case ADDWC: // RESULT <-- A + B + CARRY
            case SUB: // RESULT <-- A - B
            case SUBWB: // RESULT <-- A - B + BORROW
            case AND: // RESULT <-- A & B
            case OR: // RESULT <-- A | B
            case XOR: // RESULT <-- A ^ B
                destRegAddr = operands[0];
                aRegAddr = operands[1];
                bRegAddr = operands[2];

                switch (instruction) {
                    case ADD: // RESULT <-- A + B
                        aluCore.add(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case ADDWC: // RESULT <-- A + B + CARRY
                        aluCore.addWithCarry(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case SUB: // RESULT <-- A - B
                        aluCore.sub(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case SUBWB: // RESULT <-- A - B + BORROW
                        aluCore.subWithBorrow(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case AND: // RESULT <-- A & B
                        aluCore.and(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case OR: // RESULT <-- A | B
                        aluCore.or(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                    case XOR: // RESULT <-- A ^ B
                        aluCore.xor(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(destRegAddr));
                        break;
                }
                break;

            // Instructions with 2 data registers (destination, source) and 2 byte-indices (destination, source)
            case MOVEBYTE: // DESTINATION[DBYTEINDEX] <-- SOURCE[SBYTEINDEX] (ASCII/UTF-8 character move)
                destRegAddr = operands[0];
                int destByteIndex = operands[1];
                sourceRegAddr = operands[2];
                int sourceByteIndex = operands[3];

                byteLiteral = getWordFrom(dataRegFile.read(sourceRegAddr), BYTE_WIDTH, BYTE_WIDTH * sourceByteIndex);
                dataRegFile.write(destRegAddr, setWordIn(dataRegFile.read(destRegAddr), byteLiteral, BYTE_WIDTH, BYTE_WIDTH * destByteIndex));
                break;

            // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
            case ADDWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] + B[((BYTEMULT*8)-1):0]
            case ADDCWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] + B[((BYTEMULT*8)-1):0] + CARRY
            case SUBWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] - B[((BYTEMULT*8)-1):0]
            case SUBCWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] - B[((BYTEMULT*8)-1):0] + BORROW
            case ANDWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] & B[((BYTEMULT*8)-1):0]
            case ORWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] | B[((BYTEMULT*8)-1):0]
            case XORWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] ^ B[((BYTEMULT*8)-1):0]
                int byteMultiplier = operands[3];
                int bitWidth = (byteMultiplier != 0) ? BYTE_WIDTH * byteMultiplier : 4;

                int resultRegAddr = operands[0];
                aRegAddr = operands[1];
                bRegAddr = operands[2];

                aluCore.updateDataWidth(bitWidth); // temporarily modify ALU dataWidth to set appropriately set ALU flags
                switch (instruction) {
                    case ADDWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] + B[((BYTEMULT*8)-1):0]
                        aluCore.add(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(resultRegAddr));
                        break;
                    case ADDCWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] + B[((BYTEMULT*8)-1):0] + CARRY
                        aluCore.addWithCarry(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(resultRegAddr));
                        break;
                    case SUBWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] - B[((BYTEMULT*8)-1):0]
                        aluCore.sub(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(resultRegAddr));
                        break;
                    case SUBCWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] - B[((BYTEMULT*8)-1):0] + BORROW
                        aluCore.subWithBorrow(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(resultRegAddr));
                        break;
                    case ANDWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] & B[((BYTEMULT*8)-1):0]
                        aluCore.and(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(resultRegAddr));
                        break;
                    case ORWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] | B[((BYTEMULT*8)-1):0]
                        aluCore.or(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(resultRegAddr));
                        break;
                    case XORWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] ^ B[((BYTEMULT*8)-1):0]
                        aluCore.xor(dataRegFile.getRegisterAt(aRegAddr), dataRegFile.getRegisterAt(bRegAddr), dataRegFile.getRegisterAt(resultRegAddr));
                        break;
                }
                aluCore.updateDataWidth(dataWidth); // restore ALU dataWidth
                break;
        }
    }

    @Override
    protected void vectorToInterruptHandler() {
        if(globalInterruptEnable.read()){ // interrupts enabled globally
            // Interrupt enable bits and flags are aligned in respective registers.
            // Logical AND comparison determines whether interrupts are enabled and flags raised.
            int compareResult = intEnableReg.read() & intFlagsReg.read();

            // As this device has only 1 interrupt vector, any non-zero result for logical AND comparison
            // results in vectoring to that location.
            if(compareResult != 0) {
                // temporarily disable interrupts to avoid further interrupts
                globalInterruptEnable.clear();

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
    protected InterruptSource[] getInterruptSources() {
        return interruptSources;
    }

    private InterruptSource[] createInterruptSources() {
        return new InterruptSource[]{
                new PIC16F877ATimer0(InterruptFlags.TMR0.name(), this,
                        tmr0Reg, optionsReg.getBitAt(5), optionsReg.getSubRegister(3,0), optionsReg.getBitAt(3))
        };
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

    private static class BasicScalarRegFile extends AbstractRegisterFile {
        public BasicScalarRegFile(int dataWidth, int addrWidth) {
            super(dataWidth, addrWidth);
        }

        @Override
        protected Map<String, Integer> createAddressMap() {
            Map<String, Integer> addressMap = new HashMap<>();
            for(NamedRegister named : NamedRegister.values()) {
                addressMap.put(named.name(), named.getAddress());
            }
            return addressMap;
        }
    }

}
