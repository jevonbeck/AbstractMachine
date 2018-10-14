package org.ricts.abstractmachine.devices.compute.interrupt;

import org.ricts.abstractmachine.components.compute.interrupt.InterruptSource;
import org.ricts.abstractmachine.components.compute.interrupt.InterruptTarget;
import org.ricts.abstractmachine.components.interfaces.Bit;
import org.ricts.abstractmachine.components.interfaces.Register;
import org.ricts.abstractmachine.components.storage.RegisterImpl;

/**
 * Created by Jevon on 04/06/2017.
 */

public class PIC16F877ATimer0 extends InterruptSource {
    private static final int DATA_WIDTH = 8;
    private static final int PRESCALAR_DATA_WIDTH = 3;
    private static final int TIMER_MAX = bitMaskOfWidth(DATA_WIDTH);

    private boolean enableSuspend;
    private int preScalarCount, suspendCount;
    private Register dataReg, targetDataReg, preScalarReg;
    private Bit modeSelect, preScalarAssignment;

    public PIC16F877ATimer0(String sourceName, InterruptTarget target,
                            Register counter, Bit modeSel, Register prescale, Bit psa) {
        super(sourceName, target);

        if(counter.dataWidth() != DATA_WIDTH) {
            throw new RuntimeException("PIC16F877ATimer0 - invalid data register width (" +
                    counter.dataWidth() + "). Expected " + DATA_WIDTH);
        }

        if(prescale.dataWidth() != PRESCALAR_DATA_WIDTH) {
            throw new RuntimeException("PIC16F877ATimer0 - invalid pre-scalar register width (" +
                    prescale.dataWidth() + "). Expected " + PRESCALAR_DATA_WIDTH);
        }

        targetDataReg = counter;
        modeSelect = modeSel;
        preScalarAssignment = psa;
        preScalarReg = prescale;

        dataReg = new RegisterImpl(DATA_WIDTH);
        preScalarCount = 0;
        enableSuspend = false;
    }

    @Override
    protected void performMainOperation(InterruptTarget target) {
        if(dataReg.read() != targetDataReg.read()) {
            preScalarCount = 0; // reset pre-scalar count
            enableSuspend = true; // apply 2 instruction cycle sync delay
            suspendCount = -1;
            dataReg.write(targetDataReg.read());
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

        boolean isInTimerMode = !modeSelect.read(); // optionsReg<5> == 0
        boolean isTimerPreScalar = !preScalarAssignment.read(); // optionsReg<3> == 0

        if(isInTimerMode) {
            if(isTimerPreScalar) {
                int preScalarIndex = preScalarReg.read() + 1;
                int preScalarOverflowValue = 1 << preScalarIndex;

                preScalarCount += 1;
                if(preScalarCount >= preScalarOverflowValue) {
                    preScalarCount = 0;
                    incrementCount();
                }
            }
            else {
                incrementCount();
            }
        }
    }

    private void incrementCount() {
        int currentValue = targetDataReg.read();
        if(currentValue == TIMER_MAX) {
            setInterruptTriggered();
        }

        dataReg.write(currentValue + 1);
        targetDataReg.write(dataReg.read());
    }
}
