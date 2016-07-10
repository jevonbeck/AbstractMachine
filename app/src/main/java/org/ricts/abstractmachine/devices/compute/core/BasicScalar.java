package org.ricts.abstractmachine.devices.compute.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.isa.InstructionGroup;
import org.ricts.abstractmachine.components.compute.isa.IsaDecoder;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.components.storage.Register;
import org.ricts.abstractmachine.datastructures.Stack;
import org.ricts.abstractmachine.devices.compute.alu.BasicALU;
import org.ricts.abstractmachine.devices.compute.alu.BasicALU.Mneumonics;

public class BasicScalar extends ComputeCore {
    /*** Start of Instruction Definitions ***/
    public enum Instruction {
        POP, NOP, HALT,
        JUMP, PUSH, STOREPC,
        JUMPL,
        LOAD,
        SETB, CLRB,
        LOADM, STOREM, LOADA, STOREA,
        LOADI, STOREI,
        MOVE, NOT, RLC, RRC, INC, DEC,
        LOADBYTE,
        JUMPIFBC, JUMPIFBS,
        JUMPIFBCL, JUMPIFBSL,
        SHIFTL, SHIFTR,
        ADD, ADDWC, SUB, SUBWB, AND, OR, XOR,
        MOVEBYTE,
        ADDWIDTH, ADDCWIDTH, SUBWIDTH, SUBCWIDTH, ANDWIDTH, ORWIDTH, XORWIDTH;
    }

    public enum InstructionGrouping {
        NoOperands(new Instruction[]{Instruction.POP, Instruction.NOP, Instruction.HALT}, 0),
        InstrAddressReg(new Instruction[]{Instruction.JUMP, Instruction.PUSH, Instruction.STOREPC}, 1),
        InstrAddressLiteral(new Instruction[]{Instruction.JUMPL}, 1),
        DataAssignLit(new Instruction[]{Instruction.LOAD}, 2),
        RegBitManip(new Instruction[]{Instruction.SETB, Instruction.CLRB}, 2),
        DataMemOps(new Instruction[]{Instruction.LOADM, Instruction.STOREM, Instruction.LOADA, Instruction.STOREA}, 2),
        InstrAddrConvert(new Instruction[]{Instruction.LOADI, Instruction.STOREI}, 2),
        TwoRegOps(new Instruction[]{Instruction.MOVE, Instruction.NOT, Instruction.RLC,
                Instruction.RRC, Instruction.INC, Instruction.DEC}, 2),
        ByteLoad(new Instruction[]{Instruction.LOADBYTE}, 3),
        ConditionalBranch(new Instruction[]{Instruction.JUMPIFBC, Instruction.JUMPIFBS}, 3),
        ConditionalBranchLiteral(new Instruction[]{Instruction.JUMPIFBCL, Instruction.JUMPIFBSL}, 3),
        ShiftReg(new Instruction[]{Instruction.SHIFTL, Instruction.SHIFTR}, 3),
        AluOps(new Instruction[]{Instruction.ADD, Instruction.ADDWC, Instruction.SUB,
                Instruction.SUBWB, Instruction.AND, Instruction.OR, Instruction.XOR}, 3),
        RegByteManip(new Instruction[]{Instruction.MOVEBYTE}, 4),
        MultiWidthAluOps(new Instruction[]{Instruction.ADDWIDTH, Instruction.ADDCWIDTH,
                Instruction.SUBWIDTH, Instruction.SUBCWIDTH, Instruction.ANDWIDTH,
                Instruction.ORWIDTH, Instruction.XORWIDTH}, 4);

        private Instruction[] instructionSet;
        private String[] mneumonicArr;
        private int operandCount;

        InstructionGrouping(Instruction[] set, int opCount){
            instructionSet = set;
            operandCount = opCount;

            mneumonicArr = new String[instructionSet.length];
            for(int x=0; x!= instructionSet.length; ++x){
                mneumonicArr[x] = instructionSet[x].name();
            }
        }

        public String[] getMneumonicArr() {
            return mneumonicArr;
        }

        public Instruction decode(int opcode){
            return instructionSet[opcode];
        }

        public int getOperandCount(){
            return operandCount;
        }
    }
    /*** End of Instruction Definitions ***/

    private Map<String, String> mneumonicToGroupMap;
    private String[] mneumonicList;

    /* core dependent features */
    public enum StatusFlags {
        CARRY, OVERFLOW, ZERO, SIGN
    /*
      CARRY - CarryIn/BorrowIn
      OVERFLOW - CarryOut/ActiveLowBorrowOut
      SIGN - sign of result
      ZERO - indicates that result is zero
    */
    }

    public enum InterruptFlags {
        STACKOFLOW, STACKUFLOW
    }

    private int stackAddrWidth, dataRegAddrWidth, dAddrRegAddrWidth, iAddrRegAddrWidth;
    private Register pcReg;
    private Register statusReg;
    private Register intEnableReg; // interrupt enable
    private Register intFlagsReg; // interrupt flags
    private Register[] dataRegs; // (no. of) registers for manipulating data
    private Register[] dataAddrRegs; // (no. of) registers for storing data addresses
    private Register[] instrAddrRegs; // (no. of) registers for storing instruction addresses (temporarily)
    private Stack callStack; // presence or absence of on-chip call stack
    private BasicALU alu; // operations allowed by ALU

    public BasicScalar(int byteMultiplierWidth, int dAdWidth, int iAdWidth, int stkAdWidth,
                       int dRegAdWidth, int dAdrRegAdWidth, int iAdrRegAdWidth) {
        // byteMultiplierWidth - for making number of bytes in dataWidth a power of 2
        // dRegAdWidth - for accessing data registers.
        // dAdrRegAdWidth - for accessing data address registers.
        // iAdrRegAdWidth - for accessing instruction address registers. iAdrRegAdWidth >= 1 (PC must be one of them)

		/* Initialise important widths */
        iAddrWidth = iAdWidth;
        dAddrWidth = dAdWidth;

        int byteCount = (int) Math.pow(2, byteMultiplierWidth); // for making data-width (dWidth) a multiple of 8 bits (1 byte)
        dataWidth = 8 * byteCount;

        dataRegAddrWidth = dRegAdWidth;
        dAddrRegAddrWidth = dAdrRegAdWidth;
        iAddrRegAddrWidth = iAdrRegAdWidth;
        stackAddrWidth = stkAdWidth;

		/* Initialise ISA. N.B: BasicCore has a register machine ISA */
        mneumonicToGroupMap = new HashMap<String, String>();
        ArrayList<InstructionGroup> instructionSet = new ArrayList<InstructionGroup>();
        for(InstructionGrouping group : InstructionGrouping.values()){
            int[] array = new int[group.getOperandCount()];
            String[] mneumonicArray = group.getMneumonicArr();
            String groupName = group.name();
            instructionSet.add(new InstructionGroup(
                    array, mneumonicArray, groupName));

            for(String mneumonic : mneumonicArray){
                mneumonicToGroupMap.put(mneumonic, groupName);
            }

            switch (group){
                case MultiWidthAluOps:
                    // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
                    array[0] = dataRegAddrWidth; // data register (result)
                    array[1] = dataRegAddrWidth; // data register (A)
                    array[2] = dataRegAddrWidth; // data register (B)
                    array[3] = byteMultiplierWidth; // byte multiplier
                    break;
                case RegByteManip:
                    // Instructions with 2 data registers (source, destination) and 2 byte-indices
                    array[0] = dataRegAddrWidth; // data register (destination)
                    array[1] = bitWidth(byteCount - 1); // byte index
                    array[2] = dataRegAddrWidth; // data register (source)
                    array[3] = bitWidth(byteCount - 1); // byte index
                    break;
                case AluOps:
                    // Instructions with 3 data registers (result, A, B)
                    array[0] = dataRegAddrWidth; // data register (result)
                    array[1] = dataRegAddrWidth; // data register (A)
                    array[2] = dataRegAddrWidth; // data register (B)
                    break;
                case ShiftReg:
                    // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
                    array[0] = dataRegAddrWidth; // data register (destination)
                    array[1] = dataRegAddrWidth; // data register (source)
                    array[2] = bitWidth(dataWidth - 1); // bit-shift amount
                    break;
                case ConditionalBranch:
                    // Instructions with 1 data register, 1 bit-index and 1 instruction address register
                    array[0] = dataRegAddrWidth; // data register
                    array[1] = bitWidth(dataWidth - 1); // bit index
                    array[2] = iAddrRegAddrWidth; // instruction address register
                    break;
                case ConditionalBranchLiteral:
                    // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
                    array[0] = dataRegAddrWidth; // data register
                    array[1] = bitWidth(dataWidth - 1); // bit index
                    array[2] = iAddrWidth; // instruction address literal
                    break;
                case ByteLoad:
                    // Instructions with 1 data register, 1 byte-index and 1 byte literal
                    array[0] = dataRegAddrWidth; // data register
                    array[1] = bitWidth(byteCount - 1); // byte index
                    array[2] = 8; // byte literal
                    break;
                case TwoRegOps:
                    // Instructions with 2 data registers (destination, source)
                    array[0] = dataRegAddrWidth; // data register (destination)
                    array[1] = dataRegAddrWidth; // data register (source)
                    break;
                case InstrAddrConvert:
                    // Instructions with 1 data register and 1 instruction address register
                    array[0] = dataRegAddrWidth; // data register
                    array[1] = iAddrRegAddrWidth; // instruction address register
                    break;
                case DataMemOps:
                    // Instructions with 1 data register and 1 data address register
                    array[0] = dataRegAddrWidth; // data register
                    array[1] = dAddrRegAddrWidth; // data address register
                    break;
                case RegBitManip:
                    // Instructions with 1 data register and 1 bit-index
                    array[0] = dataRegAddrWidth; // register
                    array[1] = bitWidth(dataWidth - 1); // bit index
                    break;
                case DataAssignLit:
                    // Instructions with 1 data register and 1 data literal
                    array[0] = dataRegAddrWidth; // register
                    array[1] = dataWidth; // data literal
                    break;
                case InstrAddressReg:
                    // Instructions with 1 instruction address register
                    array[0] = iAddrRegAddrWidth; // register
                    break;
                case InstrAddressLiteral:
                    // Instructions with 1 instruction address literal
                    array[0] = iAddrWidth; // instruction address literal
                    break;
                case NoOperands: // Instructions with 0 operands
                default:
                    break;
            }
        }

        Instruction[] insValues = Instruction.values();
        mneumonicList = new String[insValues.length];
        for(int x = 0; x < mneumonicList.length; ++x){
            mneumonicList[x] = insValues[x].name();
        }

        instrDecoder = new IsaDecoder(instructionSet);
        instrWidth = instrDecoder.instructionWidth();
        instrBitMask = bitMaskOfWidth(instrWidth);

        nopMneumonic = Instruction.NOP.name();
        reset();
    }

    @Override
    public String[] getMneumonicList() {
        return mneumonicList;
    }

    @Override
    public int getOperandCount(String mneumonic) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, getGroupName(mneumonic));
        return grouping.getOperandCount();
    }

    @Override
    public int getProgramCounterValue() {
        return pcReg.read();
    }

    @Override
    public void reset() {
        /* Initialise core units */
        alu = new BasicALU(dataWidth);

        // TODO: use better stack logic (RAM is counter-intuitive for on-chip stack)
        RAM stackRam = new RAM(dataWidth, stackAddrWidth, 0);
        callStack = new Stack(stackRam, 0, (int) Math.pow(2, stackAddrWidth));

	    /* Initialise registers */
        dataRegs = new Register[(int) Math.pow(2, dataRegAddrWidth)]; // for data
        pcReg = new Register(iAddrWidth);
        statusReg = new Register(StatusFlags.values().length);
        intEnableReg = new Register(InterruptFlags.values().length);
        intFlagsReg = new Register(InterruptFlags.values().length);

        int dataRegAddrCount = 0;
        dataRegs[dataRegAddrCount++] = statusReg;
        dataRegs[dataRegAddrCount++] = intEnableReg;
        dataRegs[dataRegAddrCount++] = intFlagsReg;
        for (int x = dataRegAddrCount; x < dataRegs.length; ++x) {
            dataRegs[x] = new Register(dataWidth);
        }

        dataAddrRegs = new Register[(int) Math.pow(2, dAddrRegAddrWidth)]; // for data addresses
        for (int x = 0; x < dataAddrRegs.length; ++x) {
            dataAddrRegs[x] = new Register(dAddrWidth);
        }

        instrAddrRegs = new Register[(int) Math.pow(2, iAddrRegAddrWidth)]; // for instruction addresses
        int instrAddrRegAddrCount = 0;
        instrAddrRegs[instrAddrRegAddrCount++] = pcReg;
        for (int x = instrAddrRegAddrCount; x < instrAddrRegs.length; ++x) {
            instrAddrRegs[x] = new Register(iAddrWidth);
        }
    }

    @Override
    public boolean isDataMemInstr(String groupName, int enumOrdinal) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(enumOrdinal);

        switch (grouping){
            case DataMemOps:
                switch (instruction){
                    case LOADM:
                    case STOREM:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    @Override
    protected boolean isHaltInstr(String groupName, int enumOrdinal) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        return grouping == InstructionGrouping.NoOperands && grouping.decode(enumOrdinal) == Instruction.HALT;
    }

    @Override
    protected void fetchOpsExecuteInstr(String groupName, int enumOrdinal, int[] operands, MemoryPort dataMemory) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(enumOrdinal);

        int regAddr, destRegAddr, sourceRegAddr, byteLiteral;
        switch (grouping){
            case RegBitManip:
                // Instructions with 1 data register and 1 bit-index
                regAddr = operands[0];
                int bitIndex = operands[1];

                switch (instruction) {
                    case SETB: // DREG[BITINDEX] <-- true (boolean variable assignment)
                        dataRegs[regAddr].write(setBitAtIndex(bitIndex, dataRegs[regAddr].read()));
                        break;
                    case CLRB: // DREG[BITINDEX] <-- false (boolean variable assignment)
                        dataRegs[regAddr].write(clearBitAtIndex(bitIndex, dataRegs[regAddr].read()));
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
            case DataMemOps:
                // Instructions with 1 data register and 1 data address register
                int dRegAddr = operands[0];
                int dAddrRegAddr = operands[1];

                switch (instruction) {
                    case LOADM: // DREG <-- MEMORY[DADREG] (dereference pointer and assign value to variable)
                        dataRegs[dRegAddr].write(dataMemory.read(dataAddrRegs[dAddrRegAddr].read()));
                        break;
                    case STOREM: // MEMORY[DADREG] <-- DREG (assign variable to dereferenced pointer)
                        dataMemory.write(dataAddrRegs[dAddrRegAddr].read(), dataRegs[dRegAddr].read());
                        break;
                    case LOADA: // DREG <-- (int) DADREG (dereference pointer and assign value to variable)
                        dataRegs[dRegAddr].write(dataAddrRegs[dAddrRegAddr].read());
                        break;
                    case STOREA: // DADREG <-- (data address) DREG (pointer assignment) [OS level operation / result of call to 'new']
                        dataAddrRegs[dAddrRegAddr].write(dataRegs[dRegAddr].read());
                        break;
                }
                break;
            case ByteLoad:
                // Instructions with 1 data register, 1 byte-index and 1 byte literal
                regAddr = operands[0];
                int byteIndex = operands[1];
                byteLiteral = operands[2];

                switch (instruction) {
                    case LOADBYTE: // DREG[BYTEINDEX] <-- BYTE (ASCII/UTF-8 character literal assignment)
                        dataRegs[regAddr].write(setWordIn(dataRegs[regAddr].read(), 8 * byteIndex, byteLiteral, 8));
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
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.COMP, dataRegs[sourceRegAddr].read()));
                        break;
                    case RLC: // DESTINATION <-- ROTATE_LEFT_WITH_CARRY(SOURCE)
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.RLC, dataRegs[sourceRegAddr].read()));
                        break;
                    case RRC: // DESTINATION <-- ROTATE_RIGHT_WITH_CARRY(SOURCE)
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.RRC, dataRegs[sourceRegAddr].read()));
                        break;
                    case INC: // DESTINATION <-- SOURCE + 1
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.INC, dataRegs[sourceRegAddr].read()));
                        break;
                    case DEC: // DESTINATION <-- SOURCE - 1 (useful for end of array indexing with DESTINATION)
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.DEC, dataRegs[sourceRegAddr].read()));
                        break;
                }
                updateStatusReg();
                break;
            case ShiftReg:
                // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];
                int shiftAmount = operands[2];

                switch (instruction) {
                    case SHIFTL: // DESTINATION <-- (SOURCE << SHIFTAMOUNT)
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.SHIFTL, dataRegs[sourceRegAddr].read(), shiftAmount));
                        break;
                    case SHIFTR: // DESTINATION <-- (SOURCE >> SHIFTAMOUNT)
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.SHIFTR, dataRegs[sourceRegAddr].read(), shiftAmount));
                        break;
                }
                break;
            case RegByteManip:
                // Instructions with 2 data registers (destination, source) and 2 byte-indices (destination, source)
                destRegAddr = operands[0];
                int destByteIndex = operands[1];
                sourceRegAddr = operands[2];
                int sourceByteIndex = operands[3];

                byteLiteral = getWordFrom(dataRegs[sourceRegAddr].read(), 8, 8 * sourceByteIndex);

                switch (instruction) {
                    case MOVEBYTE: // DESTINATION[DBYTEINDEX] <-- SOURCE[SBYTEINDEX] (ASCII/UTF-8 character move)
                        dataRegs[destRegAddr].write(setWordIn(dataRegs[destRegAddr].read(), byteLiteral, 8, 8 * destByteIndex));
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
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.ADD, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case ADDWC: // RESULT <-- A + B + CARRY
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.ADDWC, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case SUB: // RESULT <-- A - B
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.SUB, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case SUBWB: // RESULT <-- A - B + BORROW
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.SUBWB, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case AND: // RESULT <-- A & B
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.AND, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case OR: // RESULT <-- A | B
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.OR, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                    case XOR: // RESULT <-- A ^ B
                        dataRegs[destRegAddr].write(alu.result(Mneumonics.XOR, dataRegs[aRegAddr].read(), dataRegs[bRegAddr].read()));
                        break;
                }
                updateStatusReg();
                break;
            case MultiWidthAluOps:
                // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
                int bitWidth;
                int byteMultiplier = operands[3];
                if (byteMultiplier != 0) {
                    bitWidth = 8 * byteMultiplier;
                } else {
                    bitWidth = 4; // nibble
                }
                int byteMask = bitMaskOfWidth(bitWidth);

                int resultRegAddr = operands[0];
                int A = dataRegs[operands[1]].read() & byteMask;
                int B = dataRegs[operands[2]].read() & byteMask;

                updateAluCarry();
                alu.result(Mneumonics.UPDATEWIDTH, bitWidth); // temporarily modify ALU dataWidth to set appropriately set ALU flags
                switch (instruction) {
                    case ADDWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] + B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(Mneumonics.ADD, A, B) & byteMask);
                        break;
                    case ADDCWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] + B[((BYTEMULT*8)-1):0] + CARRY
                        dataRegs[resultRegAddr].write(alu.result(Mneumonics.ADDWC, A, B) & byteMask);
                        break;
                    case SUBWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] - B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(Mneumonics.SUB, A, B) & byteMask);
                        break;
                    case SUBCWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] - B[((BYTEMULT*8)-1):0] + BORROW
                        dataRegs[resultRegAddr].write(alu.result(Mneumonics.SUBWB, A, B) & byteMask);
                        break;
                    case ANDWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] & B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(Mneumonics.AND, A, B) & byteMask);
                        break;
                    case ORWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] | B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(Mneumonics.OR, A, B) & byteMask);
                        break;
                    case XORWIDTH: // RESULT <-- A[((BYTEMULT*8)-1):0] ^ B[((BYTEMULT*8)-1):0]
                        dataRegs[resultRegAddr].write(alu.result(Mneumonics.XOR, A, B) & byteMask);
                        break;
                }
                alu.result(Mneumonics.UPDATEWIDTH, dataWidth); // restore ALU dataWidth
                updateStatusReg(); // flags are in accordance with previously set dataWidth
                break;
            default:
                break;
        }
    }

    @Override
    protected void updateProgramCounter(String groupName, int enumOrdinal, int[] operands, ControlUnitInterface cu) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(enumOrdinal);

        int dRegAddr, bitIndex;
        switch (grouping){
            case NoOperands:
                // Instructions with 0 operands
                switch (instruction) {
                    case POP: // cu <-- predefinedStack.pop(); updateUnderflowFlag(); ('return' control-flow construct)
                        updatePC(cu, callStack.pop());
                        intFlagsReg.write(setBitValueAtIndex(InterruptFlags.STACKUFLOW.ordinal(), intFlagsReg.read(), callStack.isEmpty()));
                        break;
                    case NOP: // do nothing
                        break;
                    case HALT: // tell Control Unit to stop execution
                        cu.setNextStateToHalt();
                        break;
                }
                break;
            case InstrAddressLiteral:
                // Instructions with 1 instruction address literal
                switch (instruction) {
                    case JUMPL: // cu <-- INSTRLIT ('goto'/'break'/'continue' control-flow construct)
                        updatePC(cu, operands[0]);
                        break;
                }
                break;
            case InstrAddressReg:
                // Instructions with 1 instruction address register
                switch (instruction) {
                    case JUMP: // cu <-- IADREG ('goto'/'break'/'continue' control-flow construct)
                        updatePC(cu, instrAddrRegs[operands[0]].read());
                        break;
                    case PUSH: // predefStack.push(IADREG); updateOverflowFlag(); (part of 'function-call' control-flow construct)
                        callStack.push(instrAddrRegs[operands[0]].read());

                        intFlagsReg.write(setBitValueAtIndex(InterruptFlags.STACKOFLOW.ordinal(), intFlagsReg.read(), callStack.isFull()));
                        break;
                    case STOREPC: // IADREG <-- cu (part of 'switch' statement / look-up table / 'function-call' control-flow construct)
                        instrAddrRegs[operands[0]].write(pcReg.read());
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
            case ConditionalBranchLiteral:
                // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
                dRegAddr = operands[0];
                bitIndex = operands[1];
                int iAddrLiteral = operands[2];

                switch (instruction) {
                    case JUMPIFBCL: // IF (!DREG[BITINDEX]) cu <-- IADLITERAL ('for'/'while'/'if-else' sourceReg[bitIndex])
                        if (!getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                            updatePC(cu, iAddrLiteral);
                        }
                        break;
                    case JUMPIFBSL: // IF (DREG[BITINDEX]) cu <-- IADLITERAL ('do-while' sourceReg[bitIndex])
                        if (getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                            updatePC(cu, iAddrLiteral);
                        }
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
                            updatePC(cu, iAddrRegValue);
                        }
                        break;
                    case JUMPIFBS: // IF (DREG[BITINDEX]) cu <-- IADREG ('do-while' sourceReg[bitIndex])
                        if (getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                            updatePC(cu, iAddrRegValue);
                        }
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public int executionTime(String groupName, int enumOrdinal, MemoryPort dataMemory) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(enumOrdinal);

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
    protected void updateProgramCounterReg(int programCounter) {
        pcReg.write(programCounter);
    }

    @Override
    protected String insToString(String groupName, int enumOrdinal, int[] operands) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(enumOrdinal);

        int regAddr, dRegAddr, destRegAddr, sourceRegAddr, bitIndex, iAddrRegAddr;
        switch (grouping){
            case RegBitManip:
                // Instructions with 1 data register and 1 bit-index
                regAddr = operands[0];
                bitIndex = operands[1];

                return instruction.name() + " R" + regAddr + ", " + bitIndex;
            case DataAssignLit:
                // Instructions with 1 data register and 1 data literal
                regAddr = operands[0];
                int data = operands[1];

                return instruction.name() + " R" + regAddr + ", " + formatNumberInHex(data, dataWidth);
            case DataMemOps:
                // Instructions with 1 data register and 1 data address register
                dRegAddr = operands[0];
                int dAddrRegAddr = operands[1];

                return instruction.name() + " R" + dRegAddr + ", A" + dAddrRegAddr;
            case ByteLoad:
                // Instructions with 1 data register, 1 byte-index and 1 byte literal
                regAddr = operands[0];
                int byteIndex = operands[1];
                int byteLiteral = operands[2];

                return instruction.name() + " R" + regAddr + ", " + byteIndex + ", " +
                        formatNumberInHex(byteLiteral, 3);
            case TwoRegOps:
                // Instructions with 2 data registers (destination, source)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];

                return instruction.name() + " R" + destRegAddr + ", R" + sourceRegAddr;
            case ShiftReg:
                // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];
                int shiftAmount = operands[2];

                return instruction.name() + " R" + destRegAddr + ", R" + sourceRegAddr + ", " + shiftAmount;
            case RegByteManip:
                // Instructions with 2 data registers (destination, source) and 2 byte-indices (destination, source)
                destRegAddr = operands[0];
                int destByteIndex = operands[1];
                sourceRegAddr = operands[2];
                int sourceByteIndex = operands[3];

                return instruction.name() + " R" + destRegAddr + ", " + destByteIndex +
                        ", R" + sourceRegAddr + ", " + sourceByteIndex;
            case AluOps:
                // Instructions with 3 data registers (result, A, B)
                destRegAddr = operands[0];
                int aRegAddr = operands[1];
                int bRegAddr = operands[2];

                return instruction.name() + " R" + destRegAddr + ", R" + aRegAddr + ", R" + bRegAddr;
            case MultiWidthAluOps:
                // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
                int resultRegAddr = operands[0];
                int A = operands[1];
                int B = operands[2];
                int byteMultiplier = operands[3];

                return instruction.name() + " R" + resultRegAddr + ", R" + A +
                        ", R" + B + ", " + byteMultiplier;
            case NoOperands:
                // Instructions with 0 operands
                return instruction.name();
            case InstrAddressLiteral:
                // Instructions with 1 instruction address literal
                return instruction.name() + " " + formatNumberInHex(operands[0], iAddrWidth);
            case InstrAddressReg:
                // Instructions with 1 instruction address register
                return instruction.name() + " I" + operands[0];
            case InstrAddrConvert:
                // Instructions with 1 data register and 1 instruction address register
                dRegAddr = operands[0];
                iAddrRegAddr = operands[1];

                return instruction.name() + " R" + dRegAddr + ", I" + iAddrRegAddr;
            case ConditionalBranchLiteral:
                // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
                dRegAddr = operands[0];
                bitIndex = operands[1];
                int iAddrLiteral = operands[2];

                return instruction.name() + " R" + dRegAddr + ", " + bitIndex + ", " +
                        formatNumberInHex(iAddrLiteral, iAddrWidth);
            case ConditionalBranch:
                // Instructions with 1 data register, 1 bit-index and 1 instruction address register
                dRegAddr = operands[0];
                bitIndex = operands[1];
                iAddrRegAddr = operands[2];

                return instruction.name() + " R" + dRegAddr + ", " + bitIndex + ", I" + iAddrRegAddr;
            default:
                return "";
        }
    }

    @Override
    protected String getGroupName(String mneumonic) {
        return mneumonicToGroupMap.containsKey(mneumonic) ?
                mneumonicToGroupMap.get(mneumonic) : "";
    }

    private void updatePC(ControlUnitInterface cu, int newPcValue){
        pcReg.write(newPcValue);
        cu.setPC(newPcValue);
    }

    private void updateAluCarry() {
        if (getBitAtIndex(StatusFlags.CARRY.ordinal(), statusReg.read())) {
            alu.result(Mneumonics.SETC);
        } else {
            alu.result(Mneumonics.CLRC);
        }
    }

    private void updateStatusReg() {
        //statusReg.setOutput( setBitValueAtIndex(StatusFlags.CARRY.ordinal(), statusReg.output(), alu.carryFlag()) );
        statusReg.write(setBitValueAtIndex(StatusFlags.OVERFLOW.ordinal(), statusReg.read(), alu.overflowFlag()));
        statusReg.write(setBitValueAtIndex(StatusFlags.SIGN.ordinal(), statusReg.read(), alu.signFlag()));
        statusReg.write(setBitValueAtIndex(StatusFlags.ZERO.ordinal(), statusReg.read(), alu.zeroFlag()));
    }
}
