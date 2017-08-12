package org.ricts.abstractmachine.devices.compute.core;

import android.content.res.Resources;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.isa.InstructionGroup;
import org.ricts.abstractmachine.components.compute.isa.InstructionGroupDecoder;
import org.ricts.abstractmachine.components.compute.isa.IsaDecoder;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.devicetype.InstructionAddressDevice;
import org.ricts.abstractmachine.components.devicetype.InstructionDevice;
import org.ricts.abstractmachine.components.interfaces.DecoderCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jevon on 16/07/2017.
 */

public class BasicScalarDecoder extends Device implements DecoderCore, InstructionDevice, InstructionAddressDevice {

    /*** Start of Instruction Definitions ***/
    public enum Instruction {
        POP(R.string.basic_scalar_pop_format, R.string.basic_scalar_pop_desc),
        NOP(R.string.basic_scalar_nop_format, R.string.basic_scalar_nop_desc),
        HALT(R.string.basic_scalar_halt_format, R.string.basic_scalar_halt_desc),
        RETFIE(R.string.basic_scalar_retfie_format, R.string.basic_scalar_retfie_desc),
        JUMP(R.string.basic_scalar_jump_format, R.string.basic_scalar_jump_desc),
        PUSH(R.string.basic_scalar_push_format, R.string.basic_scalar_push_desc),
        STOREPC(R.string.basic_scalar_storepc_format, R.string.basic_scalar_storepc_desc),
        JUMPL(R.string.basic_scalar_jumpl_format, R.string.basic_scalar_jumpl_desc),
        LOAD(R.string.basic_scalar_load_format, R.string.basic_scalar_load_desc),
        SETB(R.string.basic_scalar_setb_format, R.string.basic_scalar_setb_desc),
        CLRB(R.string.basic_scalar_clrb_format, R.string.basic_scalar_clrb_desc),
        LOADM(R.string.basic_scalar_loadm_format, R.string.basic_scalar_loadm_desc),
        STOREM(R.string.basic_scalar_storem_format, R.string.basic_scalar_storem_desc),
        LOADA(R.string.basic_scalar_loada_format, R.string.basic_scalar_loada_desc),
        STOREA(R.string.basic_scalar_storea_format, R.string.basic_scalar_storea_desc),
        LOADI(R.string.basic_scalar_loadi_format, R.string.basic_scalar_loadi_desc),
        STOREI(R.string.basic_scalar_storei_format, R.string.basic_scalar_storei_desc),
        MOVE(R.string.basic_scalar_move_format, R.string.basic_scalar_move_desc),
        NOT(R.string.basic_scalar_not_format, R.string.basic_scalar_not_desc),
        RLC(R.string.basic_scalar_rlc_format, R.string.basic_scalar_rlc_desc),
        RRC(R.string.basic_scalar_rrc_format, R.string.basic_scalar_rrc_desc),
        INC(R.string.basic_scalar_inc_format, R.string.basic_scalar_inc_desc),
        DEC(R.string.basic_scalar_dec_format, R.string.basic_scalar_dec_desc),
        LOADBYTE(R.string.basic_scalar_loadbyte_format, R.string.basic_scalar_loadbyte_desc),
        JUMPIFBC(R.string.basic_scalar_jumpifbc_format, R.string.basic_scalar_jumpifbc_desc),
        JUMPIFBS(R.string.basic_scalar_jumpifbs_format, R.string.basic_scalar_jumpifbs_desc),
        JUMPIFBCL(R.string.basic_scalar_jumpifbcl_format, R.string.basic_scalar_jumpifbcl_desc),
        JUMPIFBSL(R.string.basic_scalar_jumpifbsl_format, R.string.basic_scalar_jumpifbsl_desc),
        SHIFTL(R.string.basic_scalar_shiftl_format, R.string.basic_scalar_shiftl_desc),
        SHIFTR(R.string.basic_scalar_shiftr_format, R.string.basic_scalar_shiftr_desc),
        ADD(R.string.basic_scalar_add_format, R.string.basic_scalar_add_desc),
        ADDWC(R.string.basic_scalar_addwc_format, R.string.basic_scalar_addwc_desc),
        SUB(R.string.basic_scalar_sub_format, R.string.basic_scalar_sub_desc),
        SUBWB(R.string.basic_scalar_subwb_format, R.string.basic_scalar_subc_desc),
        AND(R.string.basic_scalar_and_format, R.string.basic_scalar_and_desc),
        OR(R.string.basic_scalar_or_format, R.string.basic_scalar_or_desc),
        XOR(R.string.basic_scalar_xor_format, R.string.basic_scalar_xor_desc),
        MOVEBYTE(R.string.basic_scalar_movebyte_format, R.string.basic_scalar_movebyte_desc),
        ADDWIDTH(R.string.basic_scalar_addwidth_format, R.string.basic_scalar_addwidth_desc),
        ADDCWIDTH(R.string.basic_scalar_addcwidth_format, R.string.basic_scalar_addcwidth_desc),
        SUBWIDTH(R.string.basic_scalar_subwidth_format, R.string.basic_scalar_subwidth_desc),
        SUBCWIDTH(R.string.basic_scalar_subcwidth_format, R.string.basic_scalar_subcwidth_desc),
        ANDWIDTH(R.string.basic_scalar_andwidth_format, R.string.basic_scalar_andwidth_desc),
        ORWIDTH(R.string.basic_scalar_orwidth_format, R.string.basic_scalar_orwidth_desc),
        XORWIDTH(R.string.basic_scalar_xorwidth_format, R.string.basic_scalar_xorwidth_desc);

        private int formatResId, descriptionResId;

        Instruction(int format, int description){
            formatResId = format;
            descriptionResId = description;
        }

        public int getDescription() {
            return descriptionResId;
        }

        public int getFormat() {
            return formatResId;
        }
    }

    public enum InstructionGrouping {
        NoOperands(new Instruction[]{Instruction.POP, Instruction.NOP, Instruction.HALT, Instruction.RETFIE},
                new int[]{}),
        InstrAddressReg(new Instruction[]{Instruction.JUMP, Instruction.PUSH, Instruction.STOREPC},
                new int []{R.string.basic_scalar_iareg_label}),
        InstrAddressLiteral(new Instruction[]{Instruction.JUMPL},
                new int []{R.string.basic_scalar_ialit_label}),
        DataAssignLit(new Instruction[]{Instruction.LOAD},
                new int []{R.string.basic_scalar_dreg_label, R.string.basic_scalar_dlit_label}),
        RegBitManip(new Instruction[]{Instruction.SETB, Instruction.CLRB},
                new int []{R.string.basic_scalar_dreg_label, R.string.basic_scalar_indx_label}),
        DataMemOps(new Instruction[]{Instruction.LOADM, Instruction.STOREM, Instruction.LOADA, Instruction.STOREA},
                new int []{R.string.basic_scalar_dreg_label, R.string.basic_scalar_dareg_label}),
        InstrAddrConvert(new Instruction[]{Instruction.LOADI, Instruction.STOREI},
                new int []{R.string.basic_scalar_dreg_label, R.string.basic_scalar_iareg_label}),
        TwoRegOps(new Instruction[]{Instruction.MOVE, Instruction.NOT, Instruction.RLC,
                Instruction.RRC, Instruction.INC, Instruction.DEC},
                new int []{R.string.basic_scalar_destdreg_label, R.string.basic_scalar_srcdreg_label}),
        ByteLoad(new Instruction[]{Instruction.LOADBYTE},
                new int []{R.string.basic_scalar_dreg_label, R.string.basic_scalar_indx_label,
                        R.string.basic_scalar_dlit_label}),
        ConditionalBranch(new Instruction[]{Instruction.JUMPIFBC, Instruction.JUMPIFBS},
                new int []{R.string.basic_scalar_dreg_label, R.string.basic_scalar_indx_label,
                        R.string.basic_scalar_iareg_label}),
        ConditionalBranchLiteral(new Instruction[]{Instruction.JUMPIFBCL, Instruction.JUMPIFBSL},
                new int []{R.string.basic_scalar_dreg_label, R.string.basic_scalar_indx_label,
                        R.string.basic_scalar_ialit_label}),
        ShiftReg(new Instruction[]{Instruction.SHIFTL, Instruction.SHIFTR},
                new int []{R.string.basic_scalar_destdreg_label, R.string.basic_scalar_srcdreg_label,
                        R.string.basic_scalar_amount_label}),
        AluOps(new Instruction[]{Instruction.ADD, Instruction.ADDWC, Instruction.SUB,
                Instruction.SUBWB, Instruction.AND, Instruction.OR, Instruction.XOR},
                new int []{R.string.basic_scalar_res_label, R.string.basic_scalar_a_label,
                        R.string.basic_scalar_b_label}),
        RegByteManip(new Instruction[]{Instruction.MOVEBYTE},
                new int []{R.string.basic_scalar_destdreg_label, R.string.basic_scalar_dindex_label,
                        R.string.basic_scalar_srcdreg_label, R.string.basic_scalar_sindex_label}),
        MultiWidthAluOps(new Instruction[]{Instruction.ADDWIDTH, Instruction.ADDCWIDTH,
                Instruction.SUBWIDTH, Instruction.SUBCWIDTH, Instruction.ANDWIDTH,
                Instruction.ORWIDTH, Instruction.XORWIDTH},
                new int []{R.string.basic_scalar_res_label, R.string.basic_scalar_a_label,
                        R.string.basic_scalar_b_label, R.string.basic_scalar_byte_multiplier_label});

        private Instruction[] instructionSet;
        private String[] mneumonicArr;
        private int [] operandResIds;

        InstructionGrouping(Instruction[] set, int [] resIds){
            instructionSet = set;
            operandResIds = resIds;

            mneumonicArr = new String[instructionSet.length];
            for(int x=0; x!= instructionSet.length; ++x){
                mneumonicArr[x] = instructionSet[x].name();
            }
        }

        public String[] getMneumonicArr() {
            return mneumonicArr;
        }

        public int[] getOperandResIdArr() {
            return operandResIds;
        }

        public Instruction decode(int opcode){
            return instructionSet[opcode];
        }

        public int getOperandCount(){
            return operandResIds.length;
        }
    }
    /*** End of Instruction Definitions ***/

    private boolean useTempStorage;
    private boolean currentInstructionValid;
    private String groupName, tempGroupName;
    private int groupIndex, tempGroupIndex;
    private int[] operands, tempOperands;

    private int storedPC;
    
    private IsaDecoder instrDecoder;

    private int instrWidth;
    private int instrBitMask;
    private int iAddrWidth;
    private int iAddrBitMask;
    private int dAddrWidth;
    private int dataWidth;
    
    private Resources resources;
    
    /* Mneumonic-Value and Instruction Group mappings */
    private OperandInfo dataOpInfo;
    private OperandInfo iAddrOpInfo;
    private OperandInfo dAddrOpInfo;
    private OperandInfo dataRegAddrInfo;
    private OperandInfo byteInfo;
    private OperandInfo dAddrRegAddrInfo;
    private OperandInfo iAddrRegAddrInfo;

    private Map<String, String> mneumonicToGroupMap;
    private Map<String, OperandInfo[]> mneumonicToOperandInfoMap;
    private String[] mneumonicList;
    
    private int stackAddrWidth, dataRegAddrWidth, dAddrRegAddrWidth, iAddrRegAddrWidth;

    public BasicScalarDecoder(Resources res, int byteMultiplierWidth, int dAdWidth, int iAdWidth, int stkAdWidth,
                              int dRegAdWidth, int dAdrRegAdWidth, int iAdrRegAdWidth) {
        resources = res;
        // byteMultiplierWidth - for making number of bytes in dataWidth a power of 2
        // dRegAdWidth - for accessing data registers.
        // dAdrRegAdWidth - for accessing data address registers.
        // iAdrRegAdWidth - for accessing instruction address registers. iAdrRegAdWidth >= 1 (PC must be one of them)

		/* Initialise important widths */
        iAddrWidth = iAdWidth;
        iAddrBitMask = bitMaskOfWidth(iAddrWidth);

        dAddrWidth = dAdWidth;

        int byteCount = 1 << byteMultiplierWidth; // for making data-width (dWidth) a multiple of BYTE_WIDTH bits (1 byte)
        dataWidth = BYTE_WIDTH * byteCount;

        int minDataRegAddrWidth = 4;
        int minInstrRegAddrWidth = 1;
        dataRegAddrWidth = dRegAdWidth >= minDataRegAddrWidth ? dRegAdWidth : minDataRegAddrWidth;
        dAddrRegAddrWidth = dAdrRegAdWidth;
        iAddrRegAddrWidth = iAdrRegAdWidth >= minInstrRegAddrWidth ? iAdrRegAdWidth : minInstrRegAddrWidth;
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

        int dataRegAddrCount = 0;
        dataRegAddrInfo.addMappingWithoutReplacement(resources.getString(R.string.basic_scalar_status_reg), dataRegAddrCount++);
        dataRegAddrInfo.addMappingWithoutReplacement(resources.getString(R.string.basic_scalar_intenable_reg), dataRegAddrCount++);
        dataRegAddrInfo.addMappingWithoutReplacement(resources.getString(R.string.basic_scalar_intflags_reg), dataRegAddrCount++);
        dataRegAddrInfo.addMappingWithoutReplacement(resources.getString(R.string.basic_scalar_intsrcenable_reg), dataRegAddrCount++);
        dataRegAddrInfo.addMappingWithoutReplacement(resources.getString(R.string.basic_scalar_control_reg), dataRegAddrCount++);
        dataRegAddrInfo.addMappingWithoutReplacement(resources.getString(R.string.basic_scalar_options_reg), dataRegAddrCount++);
        dataRegAddrInfo.addMappingWithoutReplacement(resources.getString(R.string.basic_scalar_tmr0_reg), dataRegAddrCount++);
        int maxAddress = (1 << dataRegAddrWidth) - dataRegAddrCount;
        for(int x=0; x < maxAddress; ++x) {
            dataRegAddrInfo.addMappingWithoutReplacement("R" + x, dataRegAddrCount + x);
        }

        dAddrRegAddrInfo = new OperandInfo(dAddrRegAddrWidth, true, true);
        maxAddress = 1 << dAddrRegAddrWidth;
        for(int x=0; x < maxAddress; ++x) {
            dAddrRegAddrInfo.addMappingWithoutReplacement("A" + x, x);
        }

        int instrRegAddrCount = 0;
        iAddrRegAddrInfo = new OperandInfo(iAddrRegAddrWidth, true, true);
        iAddrRegAddrInfo.addMappingWithoutReplacement(resources.getString(R.string.basic_scalar_pc_reg), instrRegAddrCount++);
        maxAddress = (1 << iAddrRegAddrWidth) - instrRegAddrCount;
        for(int x=0; x < maxAddress; ++x) {
            iAddrRegAddrInfo.addMappingWithoutReplacement("I" + x, instrRegAddrCount + x);
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
        instrWidth = instrDecoder.instrWidth();
        instrBitMask = bitMaskOfWidth(instrWidth);
    }

    @Override
    public void decode(int programCounter, int instruction) {
        storedPC = programCounter & iAddrBitMask;

        int instruct = instruction & instrBitMask;
        currentInstructionValid = instrDecoder.isValidInstruction(instruct);
        if(currentInstructionValid) {
            InstructionGroupDecoder decoder = instrDecoder.getDecoderForInstruction(instruct);
            if(useTempStorage){
                tempGroupName = decoder.groupName();
                tempGroupIndex = decoder.decode(instruct);
                tempOperands = new int [decoder.operandCount()];
                for(int x=0; x != tempOperands.length; ++x){
                    tempOperands[x] = decoder.getOperand(x, instruct);
                }
            }
            else {
                groupName = decoder.groupName();
                groupIndex = decoder.decode(instruct);
                operands = new int [decoder.operandCount()];
                for(int x=0; x != operands.length; ++x){
                    operands[x] = decoder.getOperand(x, instruct);
                }
            }
        }
    }
    
    @Override
    public void updateValues() {
        if(useTempStorage) {
            groupName = tempGroupName;
            groupIndex = tempGroupIndex;
            operands = tempOperands;
        }
    }

    @Override
    public boolean hasTempStorage() {
        return useTempStorage;
    }

    @Override
    public boolean isValidInstruction() {
        return currentInstructionValid;
    }

    @Override
    public String getInstructionGroupName() {
        return groupName;
    }

    @Override
    public int getInstructionGroupIndex() {
        return groupIndex;
    }

    @Override
    public int[] getOperands() {
        return operands;
    }

    @Override
    public int getProgramCounter() {
        return storedPC;
    }

    @Override
    public int instrWidth() {
        return instrDecoder.instrWidth();
    }

    @Override
    public int iAddrWidth() {
        return iAddrWidth;
    }
    
    public String insToString() {
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

    public boolean isDataMemInstr() {
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
    
    public boolean isHaltInstr() {
        InstructionGrouping grouping = Enum.valueOf(InstructionGrouping.class, groupName);
        return grouping == InstructionGrouping.NoOperands && grouping.decode(groupIndex) == Instruction.HALT;
    }
    
    public String getGroupName(String mneumonic) {
        return mneumonicToGroupMap.containsKey(mneumonic) ?
                mneumonicToGroupMap.get(mneumonic) : "";
    }

    public String instrValueString(int instruction) {
        return formatNumberInHex(instruction, instrWidth);
    }

    public String instrAddrValueString(int address) {
        return formatNumberInHex(address, iAddrWidth);
    }

    public String dataValueString(int data) {
        return formatNumberInHex(data, dataWidth);
    }

    public String dataAddrValueString(int address) {
        return formatNumberInHex(address, dAddrWidth);
    }

    public int encodeInstruction(String iMneumonic, int [] operands) {
        return instrDecoder.encode(getGroupName(iMneumonic), iMneumonic, operands);
    }
    
    public String nopMneumonic() {
        return Instruction.NOP.name();
    }

    public OperandInfo getDataOperandInfo() {
        return dataOpInfo;
    }

    public OperandInfo getDataRegOperandInfo() {
        return dataRegAddrInfo;
    }

    public OperandInfo getDataAddrOperandInfo() {
        return dAddrOpInfo;
    }

    public OperandInfo getInstrAddrOperandInfo() {
        return iAddrOpInfo;
    }
}
