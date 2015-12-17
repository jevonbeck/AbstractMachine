package org.ricts.abstractmachine.devices.compute.core;

import java.util.ArrayList;

import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.compute.isa.InstructionGroup;
import org.ricts.abstractmachine.components.compute.isa.IsaDecoder;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.components.storage.Register;
import org.ricts.abstractmachine.datastructures.Stack;
import org.ricts.abstractmachine.devices.compute.alu.BasicALU;
import org.ricts.abstractmachine.devices.compute.alu.BasicALU.Mneumonics;

public class BasicScalar extends ComputeCore {
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


    /* core dependent features */
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

		/* Initialise protected variables first */
        iAddrWidth = iAdWidth;
        iAddrBitMask = bitMaskOfWidth(iAddrWidth);

        dAddrWidth = dAdWidth;
        dAddrBitMask = bitMaskOfWidth(dAddrWidth);

        int byteCount = (int) Math.pow(2, byteMultiplierWidth); // for making data-width (dWidth) a multiple of 8 bits (1 byte)
        int dWidth = 8 * byteCount;
        dataWidth = dWidth;
        dataBitMask = bitMaskOfWidth(dataWidth);

        /* Initialise core units */
        alu = new BasicALU(dataWidth);

        RAM stackRam = new RAM(dataWidth, stkAdWidth, 0);
        callStack = new Stack(stackRam, 0, (int) Math.pow(2, stkAdWidth));
	  
	    /* Initialise registers */
        dataRegs = new Register[(int) Math.pow(2, dRegAdWidth)]; // for data
        statusReg = new Register(StatusFlags.values().length);
        intEnableReg = new Register(InterruptFlags.values().length);
        intFlagsReg = new Register(InterruptFlags.values().length);

        dataRegs[0] = statusReg;
        dataRegs[1] = intEnableReg;
        dataRegs[2] = intFlagsReg;
        for (int x = 3; x != dataRegs.length; ++x) {
            dataRegs[x] = new Register(dWidth);
        }

        dataAddrRegs = new Register[(int) Math.pow(2, dAdrRegAdWidth)]; // for data addresses
        for (int x = 0; x != dataAddrRegs.length; ++x) {
            dataAddrRegs[x] = new Register(dAdWidth);
        }

        instrAddrRegs = new Register[(int) Math.pow(2, iAdrRegAdWidth)]; // for instruction addresses
        for (int x = 0; x != instrAddrRegs.length; ++x) {
            instrAddrRegs[x] = new Register(iAdWidth);
        }

        int[] array; // array for populating instruction formats
        instructionSet = new ArrayList<InstructionGroup>();
		
		/* Initialise ISA. N.B: BasicCore has a register machine ISA */
        // Instructions with 0 operands
        array = new int[0];
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.NoOperands.mneumonicArr(), BasicScalarEnums.NoOperands.enumName()));

        // Instructions with 1 instruction address literal
        array = new int[1];
        array[0] = iAdWidth; // instruction address literal
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.InstrAddressLiteral.mneumonicArr(), BasicScalarEnums.InstrAddressLiteral.enumName()));

        // Instructions with 1 instruction address register
        array = new int[1];
        array[0] = iAdrRegAdWidth; // register
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.InstrAddressReg.mneumonicArr(), BasicScalarEnums.InstrAddressReg.enumName()));

        // Instructions with 1 data register and 1 data literal
        array = new int[2];
        array[0] = dRegAdWidth; // register
        array[1] = dWidth; // data literal
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.DataAssignLit.mneumonicArr(), BasicScalarEnums.DataAssignLit.enumName()));

        // Instructions with 1 data register and 1 bit-index
        array = new int[2];
        array[0] = dRegAdWidth; // register
        array[1] = bitWidth(dWidth - 1); // bit index
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.RegBitManip.mneumonicArr(), BasicScalarEnums.RegBitManip.enumName()));

        // Instructions with 1 data register and 1 data address register
        array = new int[2];
        array[0] = dRegAdWidth; // data register
        array[1] = dAdrRegAdWidth; // data address register
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.DataMemOps.mneumonicArr(), BasicScalarEnums.DataMemOps.enumName()));

        // Instructions with 1 data register and 1 instruction address register
        array = new int[2];
        array[0] = dRegAdWidth; // data register
        array[1] = iAdrRegAdWidth; // instruction address register
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.InstrAddrConvert.mneumonicArr(), BasicScalarEnums.InstrAddrConvert.enumName()));

        // Instructions with 2 data registers (destination, source)
        array = new int[2];
        array[0] = dRegAdWidth; // data register (destination)
        array[1] = dRegAdWidth; // data register (source)
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.TwoRegOps.mneumonicArr(), BasicScalarEnums.TwoRegOps.enumName()));

        // Instructions with 1 data register, 1 byte-index and 1 byte literal
        array = new int[3];
        array[0] = dRegAdWidth; // data register
        array[1] = bitWidth(byteCount - 1); // byte index
        array[2] = 8; // byte literal
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.ByteLoad.mneumonicArr(), BasicScalarEnums.ByteLoad.enumName()));

        // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
        array = new int[3];
        array[0] = dRegAdWidth; // data register
        array[1] = bitWidth(dWidth - 1); // bit index
        array[2] = iAdWidth; // instruction address literal
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.ConditionalBranchLiteral.mneumonicArr(), BasicScalarEnums.ConditionalBranchLiteral.enumName()));

        // Instructions with 1 data register, 1 bit-index and 1 instruction address register
        array = new int[3];
        array[0] = dRegAdWidth; // data register
        array[1] = bitWidth(dWidth - 1); // bit index
        array[2] = iAdrRegAdWidth; // instruction address register
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.ConditionalBranch.mneumonicArr(), BasicScalarEnums.ConditionalBranch.enumName()));

        // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
        array = new int[3];
        array[0] = dRegAdWidth; // data register (destination)
        array[1] = dRegAdWidth; // data register (source)
        array[2] = bitWidth(dWidth - 1); // bit-shift amount
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.ShiftReg.mneumonicArr(), BasicScalarEnums.ShiftReg.enumName()));

        // Instructions with 3 data registers (result, A, B)
        array = new int[3];
        array[0] = dRegAdWidth; // data register (result)
        array[1] = dRegAdWidth; // data register (A)
        array[2] = dRegAdWidth; // data register (B)
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.AluOps.mneumonicArr(), BasicScalarEnums.AluOps.enumName()));

        // Instructions with 2 data registers (source, destination) and 2 byte-indices
        array = new int[4];
        array[0] = dRegAdWidth; // data register (destination)
        array[1] = bitWidth(byteCount - 1); // byte index
        array[2] = dRegAdWidth; // data register (source)
        array[3] = bitWidth(byteCount - 1); // byte index
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.RegByteManip.mneumonicArr(), BasicScalarEnums.RegByteManip.enumName()));

        // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
        array = new int[4];
        array[0] = dRegAdWidth; // data register (result)
        array[1] = dRegAdWidth; // data register (A)
        array[2] = dRegAdWidth; // data register (B)
        array[3] = byteMultiplierWidth; // byte multiplier
        instructionSet.add(new InstructionGroup(
                array, BasicScalarEnums.MultiWidthAluOps.mneumonicArr(), BasicScalarEnums.MultiWidthAluOps.enumName()));

        instrDecoder = new IsaDecoder(instructionSet);
        instrWidth = instrDecoder.instructionWidth();
        instrBitMask = bitMaskOfWidth(instrWidth);

        nopGroupName = BasicScalarEnums.NoOperands.enumName();
        nopMneumonic = BasicScalarEnums.NoOperands.NOP.name();
    }

    @Override
    protected void fetchOpsExecuteInstr(String groupName, int enumOrdinal, int[] operands, MemoryPort dataMemory) {
        if (groupName.equals(BasicScalarEnums.RegBitManip.enumName())) {
            // Instructions with 1 data register and 1 bit-index
            int regAddr = operands[0];
            int bitIndex = operands[1];

            switch (BasicScalarEnums.RegBitManip.decode(enumOrdinal)) {
                case SETB: // DREG[BITINDEX] <-- true (boolean variable assignment)
                    dataRegs[regAddr].write(setBitAtIndex(bitIndex, dataRegs[regAddr].read()));
                    break;
                case CLRB: // DREG[BITINDEX] <-- false (boolean variable assignment)
                    dataRegs[regAddr].write(clearBitAtIndex(bitIndex, dataRegs[regAddr].read()));
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.DataAssignLit.enumName())) {
            // Instructions with 1 data register and 1 data literal
            switch (BasicScalarEnums.DataAssignLit.decode(enumOrdinal)) {
                case LOAD: // DREG <-- DATA (variable assignment)
                    dataRegs[operands[0]].write(operands[1]);
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.DataMemOps.enumName())) {
            // Instructions with 1 data register and 1 data address register
            int dRegAddr = operands[0];
            int dAddrRegAddr = operands[1];

            switch (BasicScalarEnums.DataMemOps.decode(enumOrdinal)) {
                case LOADM: // DREG <-- MEMORY[DADREG] (dereference pointer and assign value to variable)
                    //dataRegs[dRegAddr].write(dataMemory.read(dAddrRegAddr));
                    dataRegs[dRegAddr].write(dataMemory.read(dataAddrRegs[dAddrRegAddr].read()));
                    /*
                    for(int x=0; x!= vecArraySize; ++x){
                      dataRegs[dRegAddr + x].setOutput( dataMemory.read(dAddrRegAddr + x) ); // vector load!

                      vectorRegs[dRegAddr][x].setOutput( dataMemory.read(dAddrRegAddr + x) ); // alternative vector load!
                    }
                    */
                    break;
                case STOREM: // MEMORY[DADREG] <-- DREG (assign variable to dereferenced pointer)
                    //dataMemory.write(dAddrRegAddr, dataRegs[dRegAddr].read());
                    dataMemory.write(dataAddrRegs[dAddrRegAddr].read(), dataRegs[dRegAddr].read());
                    /*
                    for(int x=0; x!= vecArraySize; ++x){
                      dataMemory.write(dAddrRegAddr + x, dataRegs[dRegAddr + x].output()); // vector store!

                      dataMemory.write(dAddrRegAddr + x, vectorRegs[dRegAddr][x].output()); // alternative vector store!
                    }
                    */
                    break;
                case LOADA: // DREG <-- (int) DADREG (dereference pointer and assign value to variable)
                    dataRegs[dRegAddr].write(dataAddrRegs[dAddrRegAddr].read() & dataBitMask);
                    break;
                case STOREA: // DADREG <-- (data address) DREG (pointer assignment) [OS level operation / result of call to 'new']
                    dataAddrRegs[dAddrRegAddr].write(dataRegs[dRegAddr].read() & dAddrBitMask);
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.ByteLoad.enumName())) {
            // Instructions with 1 data register, 1 byte-index and 1 byte literal
            int regAddr = operands[0];
            int byteIndex = operands[1];
            int byteLiteral = operands[2];

            switch (BasicScalarEnums.ByteLoad.decode(enumOrdinal)) {
                case LOADBYTE: // DREG[BYTEINDEX] <-- BYTE (ASCII/UTF-8 character literal assignment)
                    dataRegs[regAddr].write(setWordIn(dataRegs[regAddr].read(), 8 * byteIndex, byteLiteral, 8));
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.TwoRegOps.enumName())) {
            // Instructions with 2 data registers (destination, source)
            int destRegAddr = operands[0];
            int sourceRegAddr = operands[1];

            updateAluCarry();
            switch (BasicScalarEnums.TwoRegOps.decode(enumOrdinal)) {
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
        } else if (groupName.equals(BasicScalarEnums.ShiftReg.enumName())) {
            // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
            int destRegAddr = operands[0];
            int sourceRegAddr = operands[1];
            int shiftAmount = operands[2];

            switch (BasicScalarEnums.ShiftReg.decode(enumOrdinal)) {
                case SHIFTL: // DESTINATION <-- (SOURCE << SHIFTAMOUNT)
                    dataRegs[destRegAddr].write(alu.result(Mneumonics.SHIFTL, dataRegs[sourceRegAddr].read(), shiftAmount));
                    break;
                case SHIFTR: // DESTINATION <-- (SOURCE >> SHIFTAMOUNT)
                    dataRegs[destRegAddr].write(alu.result(Mneumonics.SHIFTR, dataRegs[sourceRegAddr].read(), shiftAmount));
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.RegByteManip.enumName())) {
            // Instructions with 2 data registers (destination, source) and 2 byte-indices (destination, source)
            int destRegAddr = operands[0];
            int destByteIndex = operands[1];
            int sourceRegAddr = operands[2];
            int sourceByteIndex = operands[3];

            int byteLiteral = getWordFrom(dataRegs[sourceRegAddr].read(), 8, 8 * sourceByteIndex);

            switch (BasicScalarEnums.RegByteManip.decode(enumOrdinal)) {
                case MOVEBYTE: // DESTINATION[DBYTEINDEX] <-- SOURCE[SBYTEINDEX] (ASCII/UTF-8 character move)
                    dataRegs[destRegAddr].write(setWordIn(dataRegs[destRegAddr].read(), byteLiteral, 8, 8 * destByteIndex));
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.AluOps.enumName())) {
            // Instructions with 3 data registers (result, A, B)
            int destRegAddr = operands[0];
            int aRegAddr = operands[1];
            int bRegAddr = operands[2];

            updateAluCarry();
            switch (BasicScalarEnums.AluOps.decode(enumOrdinal)) {
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
        } else if (groupName.equals(BasicScalarEnums.MultiWidthAluOps.enumName())) {
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
            switch (BasicScalarEnums.MultiWidthAluOps.decode(enumOrdinal)) {
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
        }
    }

    @Override
    protected void updateProgramCounter(String groupName, int enumOrdinal, int[] operands, RegisterPort PC) {
        if (groupName.equals(BasicScalarEnums.NoOperands.enumName())) {
            // Instructions with 0 operands
            switch (BasicScalarEnums.NoOperands.decode(enumOrdinal)) {
                case POP: // PC <-- predefinedStack.pop(); updateUnderflowFlag(); ('return' control-flow construct)
                    PC.write(callStack.pop());
                    intFlagsReg.write(setBitValueAtIndex(InterruptFlags.STACKUFLOW.ordinal(), intFlagsReg.read(), callStack.isEmpty()));
                    break;
                case NOP: // do nothing
                default:
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.InstrAddressLiteral.enumName())) {
            // Instructions with 1 instruction address literal
            switch (BasicScalarEnums.InstrAddressLiteral.decode(enumOrdinal)) {
                case JUMP: // PC <-- INSTRLIT ('goto'/'break'/'continue' control-flow construct)
                    PC.write(operands[0]);
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.InstrAddressReg.enumName())) {
            // Instructions with 1 instruction address register
            switch (BasicScalarEnums.InstrAddressReg.decode(enumOrdinal)) {
                case JUMP: // PC <-- IADREG ('goto'/'break'/'continue' control-flow construct)
                    PC.write(instrAddrRegs[operands[0]].read());
                    break;
                case PUSH: // predefStack.push(IADREG); updateOverflowFlag(); (part of 'function-call' control-flow construct)
                    callStack.push(instrAddrRegs[operands[0]].read());

                    intFlagsReg.write(setBitValueAtIndex(InterruptFlags.STACKOFLOW.ordinal(), intFlagsReg.read(), callStack.isFull()));
                    break;
                case STOREPC: // IADREG <-- PC (part of 'switch' statement / look-up table / 'function-call' control-flow construct)
                    instrAddrRegs[operands[0]].write(PC.read());
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.InstrAddrConvert.enumName())) {
            // Instructions with 1 data register and 1 instruction address register
            int dRegAddr = operands[0];
            int iAddrRegAddr = operands[1];

            switch (BasicScalarEnums.InstrAddrConvert.decode(enumOrdinal)) {
                case LOADI: // DREG <-- (int) IADREG (dereference pointer and assign value to variable)
                    dataRegs[dRegAddr].write(instrAddrRegs[iAddrRegAddr].read() & dataBitMask);
                    break;
                case STOREI: // IADREG <-- (instruction address) DREG [OS level operation / load start address of new program]
                    instrAddrRegs[iAddrRegAddr].write(dataRegs[dRegAddr].read() & iAddrBitMask);
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.ConditionalBranchLiteral.enumName())) {
            // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
            int dRegAddr = operands[0];
            int bitIndex = operands[1];
            int iAddrLiteral = operands[2];

            switch (BasicScalarEnums.ConditionalBranchLiteral.decode(enumOrdinal)) {
                case JUMPIFBC: // IF (!DREG[BITINDEX]) PC <-- IADLITERAL ('for'/'while'/'if-else' sourceReg[bitIndex])
                    if (!getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                        PC.write(iAddrLiteral);
                    }
                    break;
                case JUMPIFBS: // IF (DREG[BITINDEX]) PC <-- IADLITERAL ('do-while' sourceReg[bitIndex])
                    if (getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                        PC.write(iAddrLiteral);
                    }
                    break;
            }
        } else if (groupName.equals(BasicScalarEnums.ConditionalBranch.enumName())) {
            // Instructions with 1 data register, 1 bit-index and 1 instruction address register
            int dRegAddr = operands[0];
            int bitIndex = operands[1];
            int iAddrRegValue = instrAddrRegs[operands[2]].read();

            switch (BasicScalarEnums.ConditionalBranch.decode(enumOrdinal)) {
                case JUMPIFBC: // IF (!DREG[BITINDEX]) PC <-- IADREG ('for'/'while'/'if-else' sourceReg[bitIndex])
                    if (!getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                        PC.write(iAddrRegValue);
                    }
                    break;
                case JUMPIFBS: // IF (DREG[BITINDEX]) PC <-- IADREG ('do-while' sourceReg[bitIndex])
                    if (getBitAtIndex(bitIndex, dataRegs[dRegAddr].read())) {
                        PC.write(iAddrRegValue);
                    }
                    break;
            }
        }
    }

    @Override
    public int executionTime(String groupName, int enumOrdinal, MemoryPort dataMemory) {
        if (groupName.equals(BasicScalarEnums.DataMemOps.enumName())) {
            // Instructions with 1 data register and 1 data address register
            switch (BasicScalarEnums.DataMemOps.decode(enumOrdinal)) {
                case LOADM: // DREG <-- MEMORY[DADREG] (dereference pointer and assign value to variable)
                case STOREM: // MEMORY[DADREG] <-- DREG (assign variable to dereferenced pointer)
                    return dataMemory.accessTime();
                default:

            }
        }

        return 1;
    }

    @Override
    protected String insToString(String groupName, int enumOrdinal, int[] operands) {
        if (groupName.equals(BasicScalarEnums.RegBitManip.enumName())) {
            // Instructions with 1 data register and 1 bit-index
            int regAddr = operands[0];
            int bitIndex = operands[1];

            return BasicScalarEnums.RegBitManip.decode(enumOrdinal).name() +
                    " R" + regAddr + ", " + bitIndex;
        } else if (groupName.equals(BasicScalarEnums.DataAssignLit.enumName())) {
            // Instructions with 1 data register and 1 data literal
            int regAddr = operands[0];
            int data = operands[1];

            return BasicScalarEnums.DataAssignLit.decode(enumOrdinal).name() +
                    " R" + regAddr + ", " + formatNumberInHex(data, dataWidth);
        } else if (groupName.equals(BasicScalarEnums.DataMemOps.enumName())) {
            // Instructions with 1 data register and 1 data address register
            int dRegAddr = operands[0];
            int dAddrRegAddr = operands[1];

            return BasicScalarEnums.DataMemOps.decode(enumOrdinal).name() +
                    " R" + dRegAddr + ", A" + dAddrRegAddr;
        } else if (groupName.equals(BasicScalarEnums.ByteLoad.enumName())) {
            // Instructions with 1 data register, 1 byte-index and 1 byte literal
            int regAddr = operands[0];
            int byteIndex = operands[1];
            int byteLiteral = operands[2];

            return BasicScalarEnums.ByteLoad.decode(enumOrdinal).name() +
                    " R" + regAddr + ", " + byteIndex + ", " + formatNumberInHex(byteLiteral, 3);
        } else if (groupName.equals(BasicScalarEnums.TwoRegOps.enumName())) {
            // Instructions with 2 data registers (destination, source)
            int destRegAddr = operands[0];
            int sourceRegAddr = operands[1];

            return BasicScalarEnums.TwoRegOps.decode(enumOrdinal).name() +
                    " R" + destRegAddr + ", R" + sourceRegAddr;
        } else if (groupName.equals(BasicScalarEnums.ShiftReg.enumName())) {
            // Instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
            int destRegAddr = operands[0];
            int sourceRegAddr = operands[1];
            int shiftAmount = operands[2];

            return BasicScalarEnums.ShiftReg.decode(enumOrdinal).name() +
                    " R" + destRegAddr + ", R" + sourceRegAddr + ", " + shiftAmount;
        } else if (groupName.equals(BasicScalarEnums.RegByteManip.enumName())) {
            // Instructions with 2 data registers (destination, source) and 2 byte-indices (destination, source)
            int destRegAddr = operands[0];
            int destByteIndex = operands[1];
            int sourceRegAddr = operands[2];
            int sourceByteIndex = operands[3];

            return BasicScalarEnums.RegByteManip.decode(enumOrdinal).name() +
                    " R" + destRegAddr + ", " + destByteIndex + ", R" + sourceRegAddr + ", " + sourceByteIndex;
        } else if (groupName.equals(BasicScalarEnums.AluOps.enumName())) {
            // Instructions with 3 data registers (result, A, B)
            int destRegAddr = operands[0];
            int aRegAddr = operands[1];
            int bRegAddr = operands[2];

            return BasicScalarEnums.AluOps.decode(enumOrdinal).name() +
                    " R" + destRegAddr + ", R" + aRegAddr + ", R" + bRegAddr;
        } else if (groupName.equals(BasicScalarEnums.MultiWidthAluOps.enumName())) {
            // Instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
            int resultRegAddr = operands[0];
            int A = operands[1];
            int B = operands[2];
            int byteMultiplier = operands[3];

            return BasicScalarEnums.MultiWidthAluOps.decode(enumOrdinal).name() +
                    " R" + resultRegAddr + ", R" + A + ", R" + B + ", " + byteMultiplier;
        } else if (groupName.equals(BasicScalarEnums.NoOperands.enumName())) {
            // Instructions with 0 operands
            return BasicScalarEnums.NoOperands.decode(enumOrdinal).name();
        } else if (groupName.equals(BasicScalarEnums.InstrAddressLiteral.enumName())) {
            // Instructions with 1 instruction address literal
            return BasicScalarEnums.InstrAddressLiteral.decode(enumOrdinal).name() +
                    " " + formatNumberInHex(operands[0], iAddrWidth);
        } else if (groupName.equals(BasicScalarEnums.InstrAddressReg.enumName())) {
            // Instructions with 1 instruction address register
            return BasicScalarEnums.InstrAddressReg.decode(enumOrdinal).name() +
                    " I" + operands[0];
        } else if (groupName.equals(BasicScalarEnums.InstrAddrConvert.enumName())) {
            // Instructions with 1 data register and 1 instruction address register
            int dRegAddr = operands[0];
            int iAddrRegAddr = operands[1];

            return BasicScalarEnums.InstrAddrConvert.decode(enumOrdinal).name() +
                    " R" + dRegAddr + ", I" + iAddrRegAddr;
        } else if (groupName.equals(BasicScalarEnums.ConditionalBranchLiteral.enumName())) {
            // Instructions with 1 data register, 1 bit-index and 1 instruction address literal
            int dRegAddr = operands[0];
            int bitIndex = operands[1];
            int iAddrLiteral = operands[2];

            return BasicScalarEnums.ConditionalBranchLiteral.decode(enumOrdinal).name() +
                    " R" + dRegAddr + ", " + bitIndex + ", " + formatNumberInHex(iAddrLiteral, iAddrWidth);
        } else if (groupName.equals(BasicScalarEnums.ConditionalBranch.enumName())) {
            // Instructions with 1 data register, 1 bit-index and 1 instruction address register
            int dRegAddr = operands[0];
            int bitIndex = operands[1];
            int iAddrRegAddr = operands[2];

            return BasicScalarEnums.ConditionalBranch.decode(enumOrdinal).name() +
                    " R" + dRegAddr + ", " + bitIndex + ", I" + iAddrRegAddr;
        }

        return "";
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
