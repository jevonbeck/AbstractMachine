package org.ricts.abstractmachine.components.compute.core;


import org.ricts.abstractmachine.components.devicetype.DataDevice;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.ALU;
import org.ricts.abstractmachine.components.interfaces.Bit;

public class AluCore extends Device implements DataDevice, ALU {

    private Bit carryBit; // CarryIn/BorrowIn
    private Bit overflowBit; // CarryOut/ActiveLowBorrowOut
    private Bit zeroBit; // indicates when result is zero
    private Bit signBit; // sign of result

    private int dataBitMask;
    private int dataWidth;

    public AluCore(int dWidth, Bit carry, Bit sign, Bit zero, Bit overflow){
        carryBit = carry;
        signBit = sign;
        zeroBit = zero;
        overflowBit = overflow;

        updateDataWidth(dWidth);
    }

    @Override
    public void updateDataWidth(int width){
        dataWidth = width;
        dataBitMask = bitMaskOfWidth(width);
    }

    /* DataDevice interface implementation */
    @Override
    public int dataWidth(){
        return dataWidth;
    }

    @Override
    public String statusString() {
        return  "Ci = " + booleanValueString(carryFlag().read()) + ", " +
                "Co = " + booleanValueString(overflowFlag().read()) + ", " +
                "S = "  + booleanValueString(signFlag().read()) + ", " +
                "Z = "  + booleanValueString(zeroFlag().read());
    }

    /* ALU Arithmetic Operations */
    @Override
    public int add(int A, int B){
        clearCarryFlag(); // default carryIn value
        return addWithCarry(A,B);
    }

    @Override
    public int sub(int A, int B){
        clearCarryFlag(); // default borrowIn value
        return subWithBorrow(A,B);
    }

    @Override
    public int increment(int A) {
        return add(A, 1);
    }

    @Override
    public int decrement(int A) {
        return sub(A, 1);
    }

    @Override
    public int negate(int A) {
        return sub(0, A);
    }

    @Override
    public int addWithCarry(int A, int B){
        boolean carryIn = carryBit.read();
        int result = addwc(carryIn, A, B);
        updateSignAndZeroFlags(result);
        updateOverflowFlag(carryOut(carryIn, A, B));
        return result;
    }

    @Override
    public int subWithBorrow(int A, int B){
        boolean borrowIn = carryBit.read();
        int result = subwb(borrowIn, A, B);
        updateSignAndZeroFlags(result);
        updateOverflowFlag( not(borrowOut(borrowIn, A, B)) );
        return result;
    }

    /* ALU Bitwise Logic Operations */
    @Override
    public int onesComplement(int A){
        int result = ~A & dataBitMask;
        updateZeroFlag(result != 0);
        return result;
    }

    @Override
    public int and(int A, int B){
        int result = (A & B) & dataBitMask;
        updateZeroFlag(result != 0);
        return result;
    }

    @Override
    public int or(int A, int B){
        int result = (A | B) & dataBitMask;
        updateZeroFlag(result != 0);
        return result;
    }

    @Override
    public int xor(int A, int B){
        int result = (A ^ B) & dataBitMask; // (A+B).not(A.B)
        updateZeroFlag(result != 0);
        return result;
    }

    /* ALU Bit Shift Operations */
    @Override
    public int rotateLeftWithCarry(int A){
        int result = shiftLeftWithCarry(carryBit.read(), A);
        updateCarryFlag(shiftLeftOut(A));
        return result;
    }

    @Override
    public int rotateRightWithCarry(int A){
        int result = shiftRightWithCarry(carryBit.read(), A);
        updateCarryFlag(shiftRightOut(A));
        return result;
    }

    @Override
    public int logicalShiftLeft(int A, int amount){
        return (A << amount) & dataBitMask;
    }

    @Override
    public int logicalShiftRight(int A, int amount){
        return (A >> amount) & dataBitMask;
    }

    /* ALU Flag Manipulation */
    @Override
    public void setCarryFlag(){
        carryBit.set();
    }

    @Override
    public void clearCarryFlag(){
        carryBit.clear();
    }

    @Override
    public Bit zeroFlag(){
        return zeroBit;
    }

    @Override
    public Bit signFlag(){
        return signBit;
    }

    @Override
    public Bit carryFlag(){
        return carryBit;
    }

    @Override
    public Bit overflowFlag(){
        return overflowBit;
    }

    // Alternative implementation of overflow
    protected boolean overflowFlag(boolean carryIn, boolean resultSign){
        return xor(carryIn, resultSign);
    }

    private void updateZeroFlag(boolean value){
        zeroBit.write(value);
    }

    private void updateSignFlag(boolean value){
        signBit.write(value);
    }

    private void updateCarryFlag(boolean value){
        carryBit.write(value);
    }

    private void updateOverflowFlag(boolean value){
        overflowBit.write(value);
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
    private int addwc(boolean carryIn, int A, int B){
        int carry = (carryIn)? 1: 0;
        return ((A + B) + carry) & dataBitMask;
    }

    private boolean carryOut(boolean carryIn, int A, int B){
        boolean tempResult = and(sign(A),sign(B));
        return (carryIn)? not(tempResult) : tempResult;
    }

    private int subwb(boolean borrowIn, int A, int B){
        int borrow = (borrowIn)? 1: 0;
        return ((A - B) - borrow) & dataBitMask;
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
        return setBitValueAtIndex(dataWidth - 1, (A>>1), carryIn) & dataBitMask;
    }

    private boolean shiftRightOut(int A){
        return getBitAtIndex(0, A);
    }

    private String booleanValueString(boolean value) {
        return value ? "1" : "0";
    }
}