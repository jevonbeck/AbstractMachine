package org.ricts.abstractmachine.devices.compute.interrupt;

import org.ricts.abstractmachine.components.compute.interrupt.InterruptSource;
import org.ricts.abstractmachine.components.compute.interrupt.InterruptTarget;
import org.ricts.abstractmachine.components.storage.Register;

/**
 * Created by Jevon on 04/06/2017.
 */

public class PIC16F877ATimer0 extends InterruptSource {
    private static final int TMR0_REG_INDEX = Regs.TMR0.ordinal();
    private static final int OPTION_REG_INDEX = Regs.OPTION_REG.ordinal();
    private static final int DATA_WIDTH = 8;
    private static final int TIMER_MAX = bitMaskOfWidth(DATA_WIDTH);

    public enum Regs {
        TMR0, OPTION_REG
    }

    private boolean enableSuspend;
    private int preScalarCount, suspendCount;
    private Register dataReg;

    public PIC16F877ATimer0(String sourceName, InterruptTarget target) {
        super(sourceName, target);
        dataReg = new Register(DATA_WIDTH);
        preScalarCount = 0;
        enableSuspend = false;
    }

    @Override
    protected void performMainOperation(InterruptTarget target) {
        int [] regValues = target.getRegData(sourceName);
        int lastInternalValue = dataReg.read();

        if(lastInternalValue != regValues[TMR0_REG_INDEX]) {
            preScalarCount = 0; // reset pre-scalar count
            enableSuspend = true; // apply 2 instruction cycle sync delay
            suspendCount = -1;
        }

        if(enableSuspend) {
            suspendCount += 1;

            if(suspendCount <= 2) {
                return;
            }
            else {
                enableSuspend = false;
            }
        }

        int optionsReg = regValues[OPTION_REG_INDEX];
        boolean isInTimerMode = !getBitAtIndex(5, optionsReg); // optionsReg<5> == 0
        boolean isPreScalarEnabled = !getBitAtIndex(3, optionsReg); // optionsReg<3> == 0

        if(isInTimerMode) {
            if(isPreScalarEnabled) {
                int preScalarIndex = getWordFrom(optionsReg, 3, 0) + 1;
                int preScalarOverflowValue = 1 << preScalarIndex;

                preScalarCount += 1;
                if(preScalarCount >= preScalarOverflowValue) {
                    preScalarCount = 0;
                    incrementCount(regValues);
                }
            }
            else {
                incrementCount(regValues);
            }
            target.setRegData(sourceName, regValues);
        }
    }

    private void incrementCount(int [] regValues) {
        if(regValues[TMR0_REG_INDEX] == TIMER_MAX) {
            setInterruptTriggered();
        }

        dataReg.write(regValues[TMR0_REG_INDEX] + 1);
        regValues[TMR0_REG_INDEX] = dataReg.read();
    }
}
