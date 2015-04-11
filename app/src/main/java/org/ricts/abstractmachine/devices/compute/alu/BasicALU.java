package org.ricts.abstractmachine.devices.compute.alu;


import org.ricts.abstractmachine.components.compute.AluCore;

public class BasicALU extends AluCore {
    public BasicALU(int dWidth){
        super(dWidth);
    }

    public enum BasicAluDecoder {
        CLRC, SETC, ISZ, SIGN, COMP, RLC, RRC, SHIFTL, SHIFTR,
        INC, DEC, ADD, ADDWC, SUB, SUBWB, AND, OR, XOR, UPDATEWIDTH
    }

    public void result(BasicAluDecoder opcode){
        switch(opcode){
            case CLRC: // clear CARRY Flag
                clearCarryFlag();
                break;
            case SETC: // set CARRY Flag
                setCarryFlag();
                break;
        }
    }

    public int result(BasicAluDecoder opcode, int A){
        switch(opcode){
            case UPDATEWIDTH: // ALU_dataWidth <-- A
                updateDataWidth(A);
                return 0;

            case ISZ: // Test A is zero
                updateZeroFlag(A == 0);
                return 0;

            case SIGN: // Test SIGN of A
                updateSignFlag(sign(A));
                return 0;

            case COMP: // 1's COMPLEMENT OF A
                return not(A);

            case RLC: // ROTATE A LEFT WITH CARRY
                return rotateLeftWithCarry(A);

            case RRC: // ROTATE A RIGHT WITH CARRY
                return rotateRigthWithCarry(A);

            case INC: // INCREMENT A, A + 1
                clearCarryFlag();
                return add(A,1);

            case DEC: // DECREMENT A, A - 1
                clearCarryFlag();
                return sub(A,1);

            default:
                return -1;
        }
    }

    public int result(BasicAluDecoder opcode, int A, int B){
        switch(opcode){
            case SHIFTL: // LOGIC SHIFT A TO THE LEFT BY B BITS
                return logicalShiftLeft(A,B);

            case SHIFTR: // LOGIC SHIFT A TO THE RIGHT BY B BITS
                return logicalShiftRight(A,B);

            case ADD: // A+B, NO CARRY
                clearCarryFlag(); // default carryIn value
                return add(A,B);

            case ADDWC: // A+B WITH CARRY
                return add(A,B);

            case SUB: // A-B, NO BORROW
                clearCarryFlag(); // default borrowIn value
                return sub(A,B);

            case SUBWB: // A-B WITH BORROW
                return sub(A,B);

            case AND: // A.B or A^B
                return and(A,B);

            case OR: // A+B or AvB
                return or(A,B);

            case XOR: // A XOR B
                return xor(A,B);

            default:
                return -1;
        }
    }
}