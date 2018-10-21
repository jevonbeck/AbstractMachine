package org.ricts.abstractmachine.devices.compute.alu;


import org.ricts.abstractmachine.components.interfaces.ALU;
import org.ricts.abstractmachine.components.interfaces.Register;

public class RegisteredALU {
    private ALU alu;

    public RegisteredALU(ALU a) {
        alu = a;
    }

    public void add(Register A, Register B, Register target) {
        target.write(alu.add(A.read(), B.read()));
    }

    public void sub(Register A, Register B, Register target) {
        target.write(alu.sub(A.read(), B.read()));
    }

    public void increment(Register A, Register target) {
        target.write(alu.increment(A.read()));
    }

    public void decrement(Register A, Register target) {
        target.write(alu.decrement(A.read()));
    }

    public void negate(Register A, Register target) {
        target.write(alu.negate(A.read()));
    }

    public void addWithCarry(Register A, Register B, Register target) {
        target.write(alu.addWithCarry(A.read(), B.read()));
    }

    public void subWithBorrow(Register A, Register B, Register target) {
        target.write(alu.subWithBorrow(A.read(), B.read()));
    }

    public void onesComplement(Register A, Register target) {
        target.write(alu.onesComplement(A.read()));
    }

    public void and(Register A, Register B, Register target) {
        target.write(alu.and(A.read(), B.read()));
    }

    public void or(Register A, Register B, Register target) {
        target.write(alu.or(A.read(), B.read()));
    }

    public void xor(Register A, Register B, Register target) {
        target.write(alu.xor(A.read(), B.read()));
    }

    public void rotateLeftWithCarry(Register A, Register target) {
        target.write(alu.rotateLeftWithCarry(A.read()));
    }

    public void rotateRightWithCarry(Register A, Register target) {
        target.write(alu.rotateRightWithCarry(A.read()));
    }

    public void logicalShiftLeft(Register A, int amount, Register target) {
        target.write(alu.logicalShiftLeft(A.read(),amount));
    }

    public void logicalShiftRight(Register A, int amount, Register target) {
        target.write(alu.logicalShiftRight(A.read(),amount));
    }

    public void updateDataWidth(int width) {
        alu.updateDataWidth(width);
    }

    public String statusString() {
        return alu.statusString();
    }
}