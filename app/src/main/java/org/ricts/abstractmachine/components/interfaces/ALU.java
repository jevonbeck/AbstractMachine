package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by jevon.beckles on 19/08/2017.
 */

public interface ALU {
    /* ALU Arithmetic Operations */
    int add(int A, int B);
    int sub(int A, int B);
    int increment(int A);
    int decrement(int A);
    int negate(int A);
    int addWithCarry(int A, int B);
    int subWithBorrow(int A, int B);

    /* ALU Bitwise Logic Operations */
    int onesComplement(int A);
    int and(int A, int B);
    int or(int A, int B);
    int xor(int A, int B);

    /* ALU Bit Shift Operations */
    int rotateLeftWithCarry(int A);
    int rotateRightWithCarry(int A);
    int logicalShiftLeft(int A, int amount);
    int logicalShiftRight(int A, int amount);

    /* ALU Flag Manipulation */
    void setCarryFlag();
    void clearCarryFlag();

    Bit zeroFlag();
    Bit signFlag();
    Bit carryFlag();
    Bit overflowFlag();

    void updateDataWidth(int width);
    String statusString();
}
