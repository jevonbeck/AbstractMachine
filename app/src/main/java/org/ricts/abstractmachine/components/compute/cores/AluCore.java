package org.ricts.abstractmachine.components.compute.cores;


import org.ricts.abstractmachine.components.devicetype.Device;

public class AluCore extends Device {
    public enum Flag{
        CARRY, // CarryIn/BorrowIn
        OVERFLOW, // CarryOut/ActiveLowBorrowOut
        ZERO, // indicates when result is zero
        SIGN // sign of result
    }

    private int dataBitMask;
    private int dataWidth;
    private boolean[] statusBits; // internal array to update status bits appropriately

    public AluCore(int dWidth){
        super();
        statusBits = new boolean[Flag.values().length];

        updateDataWidth(dWidth);
    }

    protected void updateDataWidth(int dWidth){
        dataWidth = dWidth;
        dataBitMask = bitMaskOfWidth(dWidth);
    }

    /* DataDevice interface implementation */
    public int dataWidth(){
        return dataWidth;
    }

    /* ALU Flags */
    public boolean zeroFlag(){
        return statusBits[Flag.ZERO.ordinal()];
    }

    public boolean signFlag(){
        return statusBits[Flag.SIGN.ordinal()];
    }

    public boolean carryFlag(){
        return statusBits[Flag.CARRY.ordinal()];
    }

    public boolean overflowFlag(){
        return statusBits[Flag.OVERFLOW.ordinal()];
    }

    // Alternative implementation of overflow
    protected boolean overflowFlag(boolean carryIn, boolean resultSign){
        return xor(carryIn, resultSign);
    }

    /* ALU Arithmetic Operations */
    protected int add(int A, int B){
        int result = addWithCarry(statusBits[Flag.CARRY.ordinal()], A, B);
        updateSignAndZeroFlags(result);
        updateOverflowFlag(carryOut(statusBits[Flag.CARRY.ordinal()], A, B));
        return result;
    }

    protected int sub(int A, int B){
        int result = subWithBorrow(statusBits[Flag.CARRY.ordinal()], A, B);
        updateSignAndZeroFlags(result);
        updateOverflowFlag( not(borrowOut(statusBits[Flag.CARRY.ordinal()], A, B)) );
        return result;
    }

    protected int rotateLeftWithCarry(int A){
        int result = shiftLeftWithCarry(statusBits[Flag.CARRY.ordinal()], A);
        updateCarryFlag(shiftLeftOut(A));
        return result;
    }

    protected int rotateRigthWithCarry(int A){
        int result = shiftRightWithCarry(statusBits[Flag.CARRY.ordinal()], A);
        updateCarryFlag(shiftRightOut(A));
        return result;
    }

    /* ALU Bitwise Logic Operations */
    protected int logicalShiftLeft(int A, int amount){
        return (A << amount) & dataBitMask;
    }

    protected int logicalShiftRight(int A, int amount){
        return (A >> amount) & dataBitMask;
    }

    protected int not(int A){
        int result = ~A & dataBitMask;
        updateZeroFlag(result != 0);
        return result;
    }

    protected int and(int A, int B){
        int result = (A & B) & dataBitMask;
        updateZeroFlag(result != 0);
        return result;
    }

    protected int or(int A, int B){
        int result = (A | B) & dataBitMask;
        updateZeroFlag(result != 0);
        return result;
    }

    protected int xor(int A, int B){
        int result = (A ^ B) & dataBitMask; // (A+B).not(A.B)
        updateZeroFlag(result != 0);
        return result;
    }

    /* ALU Flag Manipulation */
    protected void setCarryFlag(){
        statusBits[Flag.CARRY.ordinal()] = true; // 1
    }

    protected void clearCarryFlag(){
        statusBits[Flag.CARRY.ordinal()] = false; // 0
    }

    protected void updateZeroFlag(boolean value){
        statusBits[Flag.ZERO.ordinal()] = value;
    }

    protected void updateSignFlag(boolean value){
        statusBits[Flag.SIGN.ordinal()] = value;
    }

    private void updateCarryFlag(boolean value){
        statusBits[Flag.CARRY.ordinal()] = value;
    }

    private void updateOverflowFlag(boolean value){
        statusBits[Flag.OVERFLOW.ordinal()] = value;
    }

    /* Convenience Functions */
    protected boolean sign(int number){
        return getBitAtIndex(dataWidth - 1, number); // due to 2's complement integer representation
    }

    // Alternative method to update all ALU flags at once
    protected void updateFlags(boolean carryIn, int result){
        boolean resultSign = sign(result);

        updateZeroFlag(result != 0);
        updateSignFlag(resultSign);
        overflowFlag(carryIn, resultSign);
    }

    private void updateSignAndZeroFlags(int result){
        updateZeroFlag(result != 0);
        updateSignFlag(sign(result));
    }

    /* Boolean Logic Operations */
    private boolean not(boolean A){
        return !A;
    }

    private boolean and(boolean A, boolean B){
        return A && B;
    }

    private boolean or(boolean A, boolean B){
        return A || B;
    }

    private boolean xor(boolean A, boolean B){
        return or(A,B) && not(and(A,B)); // (A+B).not(A.B)
    }

    /* Arithmetic Operations */
    private int addWithCarry(boolean carryIn, int A, int B){
        int carry = (carryIn)? 1: 0;
        return ((A + B) + carry) & dataBitMask;
    }

    private boolean carryOut(boolean carryIn, int A, int B){
        boolean tempResult = and(sign(A),sign(B));
        return (carryIn)? not(tempResult) : tempResult;
    }

    private int subWithBorrow(boolean borrowIn, int A, int B){
        int borrow = (borrowIn)? 1: 0;
        return ((A - B) + borrow) & dataBitMask;
    }

    private boolean borrowOut(boolean borrowIn, int A, int B){
        boolean tempResult = and(not(sign(A)),sign(B)); //  not(A_sign).B_sign
        return (borrowIn)? not(tempResult) : tempResult;
    }

    private int shiftLeftWithCarry(boolean carryIn, int A){
        return setBitValueAtIndex(0, (A<<1), carryIn) & dataBitMask;
    }

    private boolean shiftLeftOut(int A){
        return sign(A);
    }

    private int shiftRightWithCarry(boolean carryIn, int A){
        return setBitValueAtIndex(dataWidth-1, (A>>1), carryIn) & dataBitMask;
    }

    private boolean shiftRightOut(int A){
        return getBitAtIndex(0, A);
    }
}