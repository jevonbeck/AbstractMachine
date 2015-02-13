package org.ricts.abstractmachine.devices.compute.core;

public class BasicScalarEnums {
    //Enum for instructions with 0 operands
    public enum NoOperands {
        POP, NOP;

        private static NoOperands[] opcodeArr;
        private static String enumName = "NoOperands";
        private static String[] mneumonicArr;

        public static NoOperands decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = NoOperands.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                NoOperands[] temp = NoOperands.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 1 instruction address register
    public enum InstrAddressReg {
        JUMP, PUSH, STOREPC;

        private static InstrAddressReg[] opcodeArr;
        private static String enumName = "InstrAddressReg";
        private static String[] mneumonicArr;

        public static InstrAddressReg decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = InstrAddressReg.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                InstrAddressReg[] temp = InstrAddressReg.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 1 instruction address literal
    public enum InstrAddressLiteral {
        JUMP;

        private static InstrAddressLiteral[] opcodeArr;
        private static String enumName = "InstrAddressLiteral";
        private static String[] mneumonicArr;

        public static InstrAddressLiteral decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = InstrAddressLiteral.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                InstrAddressLiteral[] temp = InstrAddressLiteral.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions 1 data register and 1 data literal
    public enum DataAssignLit {
        LOAD;

        private static DataAssignLit[] opcodeArr;
        private static String enumName = "DataAssignLit";
        private static String[] mneumonicArr;

        public static DataAssignLit decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = DataAssignLit.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                DataAssignLit[] temp = DataAssignLit.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 1 data register and 1 bit-index
    public enum RegBitManip {
        SETB, CLRB;

        private static RegBitManip[] opcodeArr;
        private static String enumName = "RegBitManip";
        private static String[] mneumonicArr;

        public static RegBitManip decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = RegBitManip.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                RegBitManip[] temp = RegBitManip.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 1 data register and 1 data address register
    public enum DataMemOps {
        LOADM, STOREM, LOADA, STOREA;

        private static DataMemOps[] opcodeArr;
        private static String enumName = "DataMemOps";
        private static String[] mneumonicArr;

        public static DataMemOps decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = DataMemOps.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                DataMemOps[] temp = DataMemOps.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 1 instruction address register and 1 data register
    public enum InstrAddrConvert {
        LOADI, STOREI;

        private static InstrAddrConvert[] opcodeArr;
        private static String enumName = "InstrAddrConvert";
        private static String[] mneumonicArr;

        public static InstrAddrConvert decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = InstrAddrConvert.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                InstrAddrConvert[] temp = InstrAddrConvert.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    // Enum for instructions with 2 data registers (destination, source)
    public enum TwoRegOps {
        MOVE, NOT, RLC, RRC, INC, DEC;

        private static TwoRegOps[] opcodeArr;
        private static String enumName = "TwoRegOps";
        private static String[] mneumonicArr;

        public static TwoRegOps decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = TwoRegOps.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                TwoRegOps[] temp = TwoRegOps.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    // Enum for instructions with 1 data register, 1 byte-index and 1 byte literal
    public enum ByteLoad {
        LOADBYTE;

        private static ByteLoad[] opcodeArr;
        private static String enumName = "ByteLoad";
        private static String[] mneumonicArr;

        public static ByteLoad decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = ByteLoad.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                ByteLoad[] temp = ByteLoad.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 1 data register, 1 bit-index and 1 instruction address register
    public enum ConditionalBranch {
        JUMPIFBC, JUMPIFBS;

        private static ConditionalBranch[] opcodeArr;
        private static String enumName = "ConditionalBranch";
        private static String[] mneumonicArr;

        public static ConditionalBranch decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = ConditionalBranch.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                ConditionalBranch[] temp = ConditionalBranch.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 1 data register, 1 bit-index and 1 instruction address literal
    public enum ConditionalBranchLiteral {
        JUMPIFBC, JUMPIFBS;

        private static ConditionalBranchLiteral[] opcodeArr;
        private static String enumName = "ConditionalBranchLiteral";
        private static String[] mneumonicArr;

        public static ConditionalBranchLiteral decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = ConditionalBranchLiteral.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                ConditionalBranchLiteral[] temp = ConditionalBranchLiteral.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    // Enum for instructions with 2 data registers (destination, source) and 1 bit-shift amount (source)
    public enum ShiftReg {
        SHIFTL, SHIFTR;

        private static ShiftReg[] opcodeArr;
        private static String enumName = "ShiftReg";
        private static String[] mneumonicArr;

        public static ShiftReg decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = ShiftReg.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                ShiftReg[] temp = ShiftReg.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 2 data registers (high, low) and 1 data address register
    public enum DataAddrConvert {
        LOADA, STOREA;

        private static DataAddrConvert[] opcodeArr;
        private static String enumName = "DataAddrConvert";
        private static String[] mneumonicArr;

        public static DataAddrConvert decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = DataAddrConvert.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                DataAddrConvert[] temp = DataAddrConvert.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 2 data registers (high, low) and 1 instruction address register
    public enum InstrAddrConvert2 {
        LOADI, STOREI;

        private static InstrAddrConvert2[] opcodeArr;
        private static String enumName = "InstrAddrConvert2";
        private static String[] mneumonicArr;

        public static InstrAddrConvert2 decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = InstrAddrConvert2.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                InstrAddrConvert2[] temp = InstrAddrConvert2.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    // Enum for instructions with 3 data registers (result, A, B)
    public enum AluOps {
        ADD, ADDWC, SUB, SUBWB, AND, OR, XOR;

        private static AluOps[] opcodeArr;
        private static String enumName = "AluOps";
        private static String[] mneumonicArr;

        public static AluOps decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = AluOps.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                AluOps[] temp = AluOps.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    //Enum for instructions with 2 data registers (source, destination) and 2 byte-indices
    public enum RegByteManip {
        MOVEBYTE;

        private static RegByteManip[] opcodeArr;
        private static String enumName = "RegByteManip";
        private static String[] mneumonicArr;

        public static RegByteManip decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = RegByteManip.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                RegByteManip[] temp = RegByteManip.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }

    // Enum for instructions with 3 data registers (result, A, B) and 1 byte multiplier literal
    public enum MultiWidthAluOps {
        ADDWIDTH, ADDCWIDTH, SUBWIDTH, SUBCWIDTH, ANDWIDTH, ORWIDTH, XORWIDTH;

        private static MultiWidthAluOps[] opcodeArr;
        private static String enumName = "MultiWidthAluOps";
        private static String[] mneumonicArr;

        public static MultiWidthAluOps decode(int opcode) {
            if (opcodeArr == null) {
                opcodeArr = MultiWidthAluOps.values();
            }
            return opcodeArr[opcode];
        }

        public static String[] mneumonicArr() {
            if (mneumonicArr == null) {
                MultiWidthAluOps[] temp = MultiWidthAluOps.values();
                mneumonicArr = new String[temp.length];
                for(int x=0; x!= temp.length; ++x){
                    mneumonicArr[x] = temp[x].name();
                }
            }
            return mneumonicArr;
        }

        public static String enumName(){
            return enumName;
        }
    }
}
