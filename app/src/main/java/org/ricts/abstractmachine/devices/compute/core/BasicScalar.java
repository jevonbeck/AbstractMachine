package org.ricts.abstractmachine.devices.compute.core;

import org.ricts.abstractmachine.components.compute.cores.UniMemoryComputeCore;
import org.ricts.abstractmachine.components.compute.isa.InstructionGroup;
import org.ricts.abstractmachine.components.compute.isa.IsaDecoder;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.storage.Register;
import org.ricts.abstractmachine.components.storage.RegisterStack;
import org.ricts.abstractmachine.devices.compute.alu.BasicALU;
import org.ricts.abstractmachine.devices.compute.alu.BasicALU.Mneumonics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BasicScalar extends UniMemoryComputeCore {
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
        ADDWIDTH, ADDCWIDTH, SUBWIDTH, SUBCWIDTH, ANDWIDTH, ORWIDTH, XORWIDTH
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

    /* Mneumonic-Value and Instruction Group mappings */
    private OperandInfo dataOpInfo;
    private OperandInfo byteInfo;
    private OperandInfo iAddrOpInfo;
    private OperandInfo dAddrOpInfo;
    private OperandInfo dataRegAddrInfo;
    private OperandInfo dAddrRegAddrInfo;
    private OperandInfo iAddrRegAddrInfo;

    private Map<String, String> mneumonicToGroupMap;
    private Map<String, OperandInfo[]> mneumonicToOperandInfoMap;
    private String[] mneumonicList;

    /* core dependent features */
    public enum StatusFlags {
        CARRY, // CarryIn/BorrowIn
        OVERFLOW, // CarryOut/ActiveLowBorrowOut
        ZERO, // indicates when result is zero
        SIGN // sign of result
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
    private RegisterStack callStack; // presence or absence of on-chip call stack
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

        int byteCount = 1 << byteMultiplierWidth; // for making data-width (dWidth) a multiple of BYTE_WIDTH bits (1 byte)
        dataWidth = BYTE_WIDTH * byteCount;

        dataRegAddrWidth = dRegAdWidth;
        dAddrRegAddrWidth = dAdrRegAdWidth;
        iAddrRegAddrWidth = iAdrRegAdWidth;
        stackAddrWidth = stkAdWidth;

        /* Initialise OperandInfo */
        OperandInfo dataRegBitIndxInfo = new OperandInfo(bitWidth(dataWidth - 1));
        OperandInfo dataRegByteIndxInfo = new OperandInfo(bitWidth(byteCount - 1));
        OperandInfo dataRegByteCountInfo = new OperandInfo(bitWidth(byteCount));
        byteInfo = new OperandInfo(BYTE_WIDTH);

        dataOpInfo = new OperandInfo(dataWidth, true);
        iAddrOpInfo = new OperandInfo(iAddrWidth, true, true);
        dAddrOpInfo = new OperandInfo(dAddrWidth, true, true);

        dataRegAddrInfo = new OperandInfo(dataRegAddrWidth, true, true);
        int maxAddress = 1 << dataRegAddrWidth;
        for(int x=0; x < maxAddress; ++x) {
            dataRegAddrInfo.addMappingWithoutReplacement("R" + x, x);
        }
        
        dAddrRegAddrInfo = new OperandInfo(dAddrRegAddrWidth, true, true);
        maxAddress = 1 << dAddrRegAddrWidth;
        for(int x=0; x < maxAddress; ++x) {
            dAddrRegAddrInfo.addMappingWithoutReplacement("A" + x, x);
        }
        
        iAddrRegAddrInfo = new OperandInfo(iAddrRegAddrWidth, true, true);
        maxAddress = 1 << iAddrRegAddrWidth;
        for(int x=0; x < maxAddress; ++x) {
            iAddrRegAddrInfo.addMappingWithoutReplacement("I" + x, x);
        }

		/* Initialise ISA. N.B: BasicCore has a register machine ISA */
        mneumonicToGroupMap = new HashMap<>();
        mneumonicToOperandInfoMap = new HashMap<>();
        ArrayList<InstructionGroup> instructionSet = new ArrayList<>();
        for(InstructionGrouping group : InstructionGrouping.values()){
            OperandInfo[] array = new OperandInfo[group.getOperandCount()];
            String[] mneumonicArray = group.getMneumonicArr();
            String groupName = group.name();
            instructionSet.add(new InstructionGroup(
                    array, mneumonicArray, groupName));

            for(String mneumonic : mneumonicArray){
                mneumonicToGroupMap.put(mneumonic, groupName);
                mneumonicToOperandInfoMap.put(mneumonic, array);
            }

            switch (group){
                case MultiWidthAluOps:
                    // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
                    array[0] = dataRegAddrInfo; // data register (result)
                    array[1] = dataRegAddrInfo; // data register (A)
                    array[2] = dataRegAddrInfo; // data register (B)
                    array[3] = dataRegByteCountInfo; // byte multiplier
                    break;
                case RegByteManip:
                    // Instructions with 2 data registers (source, destination) and 2 byte-indices
                    array[0] = dataRegAddrInfo; // data register (destination)
                    array[1] = dataRegByteIndxInfo; // byte index
                    array[2] = dataRegAddrInfo; // data register (source)
                    array[3] = dataRegByteIndxInfo; // byte index
                    break;
                case AluOps:
                    // Instructions with 3 data registers (result, A, B)
                    array[0] = dataRegAddrInfo; // data register (result)
                    array[1] = dataRegAddrInfo; // data register (A)
                    array[2] = dataRegAddrInfo; // data register (B)
                    break;
                case ShiftReg:
                    // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
                    array[0] = dataRegAddrInfo; // data register (destination)
                    array[1] = dataRegAddrInfo; // data register (source)
                    array[2] = dataRegBitIndxInfo; // bit-shift amount
                    break;
                case ConditionalBranch:
                    // Instructions with 1 data register, 1 bit-index and 1 instruction address register
                    array[0] = dataRegAddrInfo; // data register
                    array[1] = dataRegBitIndxInfo; // bit index
                    array[2] = iAddrRegAddrInfo; // instruction address register
                    break;
                case ConditionalBranchLiteral:
                    // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
                    array[0] = dataRegAddrInfo; // data register
                    array[1] = dataRegBitIndxInfo; // bit index
                    array[2] = iAddrOpInfo; // instruction address literal
                    break;
                case ByteLoad:
                    // Instructions with 1 data register, 1 byte-index and 1 byte literal
                    array[0] = dataRegAddrInfo; // data register
                    array[1] = dataRegByteIndxInfo; // byte index
                    array[2] = byteInfo; // byte literal
                    break;
                case TwoRegOps:
                    // Instructions with 2 data registers (destination, source)
                    array[0] = dataRegAddrInfo; // data register (destination)
                    array[1] = dataRegAddrInfo; // data register (source)
                    break;
                case InstrAddrConvert:
                    // Instructions with 1 data register and 1 instruction address register
                    array[0] = dataRegAddrInfo; // data register
                    array[1] = iAddrRegAddrInfo; // instruction address register
                    break;
                case DataMemOps:
                    // Instructions with 1 data register and 1 data address register
                    array[0] = dataRegAddrInfo; // data register
                    array[1] = dAddrRegAddrInfo; // data address register
                    break;
                case RegBitManip:
                    // Instructions with 1 data register and 1 bit-index
                    array[0] = dataRegAddrInfo; // register
                    array[1] = dataRegBitIndxInfo; // bit index
                    break;
                case DataAssignLit:
                    // Instructions with 1 data register and 1 data literal
                    array[0] = dataRegAddrInfo; // register
                    array[1] = dataOpInfo; // data literal
                    break;
                case InstrAddressReg:
                    // Instructions with 1 instruction address register
                    array[0] = iAddrRegAddrInfo; // register
                    break;
                case InstrAddressLiteral:
                    // Instructions with 1 instruction address literal
                    array[0] = iAddrOpInfo; // instruction address literal
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

        reset();
    }

    @Override
    public String[] getMneumonicList() {
        return mneumonicList;
    }

    @Override
    public OperandInfo[] getOperandInfoArray(String mneumonic) {
        return mneumonicToOperandInfoMap.get(mneumonic);
    }

    @Override
    public OperandInfo getDataOperandInfo() {
        return dataOpInfo;
    }

    @Override
    public OperandInfo getDataRegOperandInfo() {
        return dataRegAddrInfo;
    }

    @Override
    public OperandInfo getDataAddrOperandInfo() {
        return dAddrOpInfo;
    }

    @Override
    public OperandInfo getInstrAddrOperandInfo() {
        return iAddrOpInfo;
    }

    @Override
    public int getProgramCounterValue() {
        return pcReg.read();
    }

    @Override
    public void reset() {
        /* Initialise core units */
        alu = new BasicALU(dataWidth);
        callStack = new RegisterStack(dataWidth, 1 << stackAddrWidth); // stack with size 2^stackAddrWidth

	    /* Initialise registers */
        dataRegs = new Register[1 << dataRegAddrWidth]; // for data (2^dataRegAddrWidth regs)
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
    public boolean isDataMemInstr(String groupName, int groupIndex) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(groupIndex);

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
    protected boolean isHaltInstr(String groupName, int groupIndex) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        return grouping == InstructionGrouping.NoOperands && grouping.decode(groupIndex) == Instruction.HALT;
    }

    @Override
    protected boolean isSleepInstr(String groupName, int groupIndex) {
        // TODO: implement sleep instruction?
        return false;
    }

    @Override
    protected void fetchOpsExecuteInstr(String groupName, int groupIndex, int[] operands) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(groupIndex);

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
                    case LOADA: // DREG <-- (int) DADREG (put pointer address value in variable - for pointer arithmetic)
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
                    case LOADBYTE: // DREG[BYTEINDEX] <-- BYTE (ASCII/UTF-BYTE_WIDTH character literal assignment)
                        dataRegs[regAddr].write(setWordIn(dataRegs[regAddr].read(), byteLiteral, BYTE_WIDTH, BYTE_WIDTH * byteIndex));
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

                byteLiteral = getWordFrom(dataRegs[sourceRegAddr].read(), BYTE_WIDTH, BYTE_WIDTH * sourceByteIndex);

                switch (instruction) {
                    case MOVEBYTE: // DESTINATION[DBYTEINDEX] <-- SOURCE[SBYTEINDEX] (ASCII/UTF-8 character move)
                        dataRegs[destRegAddr].write(setWordIn(dataRegs[destRegAddr].read(), byteLiteral, BYTE_WIDTH, BYTE_WIDTH * destByteIndex));
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
                    bitWidth = BYTE_WIDTH * byteMultiplier;
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
    protected void updateInternalControlUnitState(String groupName, int groupIndex, int[] operands) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(groupIndex);

        int dRegAddr, bitIndex;
        switch (grouping){
            case NoOperands:
                // Instructions with 0 operands
                switch (instruction) {
                    case POP: // cu <-- predefinedStack.pop(); updateUnderflowFlag(); ('return' control-flow construct)
                        updateProgramCounter(callStack.pop());
                        intFlagsReg.write(setBitValueAtIndex(InterruptFlags.STACKUFLOW.ordinal(), intFlagsReg.read(), callStack.isEmpty()));
                        break;
                    case NOP: // do nothing
                        break;
                    case HALT: // tell Control Unit to stop execution
                        setExpectedControlUnitState(ControlUnitState.HALT);
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
            case InstrAddressReg:
                // Instructions with 1 instruction address register
                switch (instruction) {
                    case JUMP: // cu <-- IADREG ('goto'/'break'/'continue' control-flow construct)
                        updateProgramCounter(instrAddrRegs[operands[0]].read());
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
            default:
                break;
        }
    }

    @Override
    protected void updateProgramCounterOnInterrupt() {
        // TODO: Do nothing for now! Implement appropriate logic when interrupts are implemented
        // TODO: update internal PC to vector location if interrupt
    }

    @Override
    public int executionTime(String groupName, int groupIndex) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(groupIndex);

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
    protected void updateProgramCounterRegs(int programCounter) {
        pcReg.write(programCounter);
    }

    @Override
    protected String insToString(String groupName, int groupIndex, int[] operands) {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        Instruction instruction = grouping.decode(groupIndex);

        int regAddr, dRegAddr, destRegAddr, sourceRegAddr, bitIndex, iAddrRegAddr;
        switch (grouping){
            case RegBitManip:
                // Instructions with 1 data register and 1 bit-index
                regAddr = operands[0];
                bitIndex = operands[1];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(regAddr) + ", " + bitIndex;
            case DataAssignLit:
                // Instructions with 1 data register and 1 data literal
                regAddr = operands[0];
                int data = operands[1];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(regAddr) + ", " + dataOpInfo.getPrettyValue(data);
            case DataMemOps:
                // Instructions with 1 data register and 1 data address register
                dRegAddr = operands[0];
                int dAddrRegAddr = operands[1];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(dRegAddr) + ", " + dAddrRegAddrInfo.getPrettyValue(dAddrRegAddr);
            case ByteLoad:
                // Instructions with 1 data register, 1 byte-index and 1 byte literal
                regAddr = operands[0];
                int byteIndex = operands[1];
                int byteLiteral = operands[2];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(regAddr) + ", " + byteIndex +
                        ", " + byteInfo.getPrettyValue(byteLiteral);
            case TwoRegOps:
                // Instructions with 2 data registers (destination, source)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(destRegAddr) +
                        ", " + dataRegAddrInfo.getPrettyValue(sourceRegAddr);
            case ShiftReg:
                // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
                destRegAddr = operands[0];
                sourceRegAddr = operands[1];
                int shiftAmount = operands[2];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(destRegAddr) +
                        ", " + dataRegAddrInfo.getPrettyValue(sourceRegAddr) + ", " + shiftAmount;
            case RegByteManip:
                // Instructions with 2 data registers (destination, source) and 2 byte-indices (destination, source)
                destRegAddr = operands[0];
                int destByteIndex = operands[1];
                sourceRegAddr = operands[2];
                int sourceByteIndex = operands[3];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(destRegAddr) + ", " + destByteIndex +
                        ", " + dataRegAddrInfo.getPrettyValue(sourceRegAddr) + ", " + sourceByteIndex;
            case AluOps:
                // Instructions with 3 data registers (result, A, B)
                destRegAddr = operands[0];
                int aRegAddr = operands[1];
                int bRegAddr = operands[2];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(destRegAddr) + ", " +
                        dataRegAddrInfo.getPrettyValue(aRegAddr) + ", " + dataRegAddrInfo.getPrettyValue(bRegAddr);
            case MultiWidthAluOps:
                // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
                int resultRegAddr = operands[0];
                int A = operands[1];
                int B = operands[2];
                int byteMultiplier = operands[3];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(resultRegAddr) + ", " +
                        dataRegAddrInfo.getPrettyValue(A) + ", " + dataRegAddrInfo.getPrettyValue(B) + ", " +
                        byteMultiplier;
            case NoOperands:
                // Instructions with 0 operands
                return instruction.name();
            case InstrAddressLiteral:
                // Instructions with 1 instruction address literal
                return instruction.name() + " " + iAddrOpInfo.getPrettyValue(operands[0]);
            case InstrAddressReg:
                // Instructions with 1 instruction address register
                return instruction.name() + " " + iAddrRegAddrInfo.getPrettyValue(operands[0]);
            case InstrAddrConvert:
                // Instructions with 1 data register and 1 instruction address register
                dRegAddr = operands[0];
                iAddrRegAddr = operands[1];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(dRegAddr) + ", " +
                        iAddrRegAddrInfo.getPrettyValue(iAddrRegAddr);
            case ConditionalBranchLiteral:
                // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
                dRegAddr = operands[0];
                bitIndex = operands[1];
                int iAddrLiteral = operands[2];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(dRegAddr) + ", " +
                        bitIndex + ", " + iAddrOpInfo.getPrettyValue(iAddrLiteral);
            case ConditionalBranch:
                // Instructions with 1 data register, 1 bit-index and 1 instruction address register
                dRegAddr = operands[0];
                bitIndex = operands[1];
                iAddrRegAddr = operands[2];

                return instruction.name() + " " + dataRegAddrInfo.getPrettyValue(dRegAddr) + ", " +
                        bitIndex + ", " + iAddrRegAddrInfo.getPrettyValue(iAddrRegAddr);
            default:
                return "";
        }
    }

    @Override
    protected String getGroupName(String mneumonic) {
        return mneumonicToGroupMap.containsKey(mneumonic) ?
                mneumonicToGroupMap.get(mneumonic) : "";
    }

    @Override
    protected String nopMneumonic() {
        return Instruction.NOP.name();
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
