package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;

/**
 * Created by jevon.beckles on 17/08/2017.
 */

public class ControlUnitExecuteState extends ControlUnitState {
    private DecoderUnit decoderCore;
    private ComputeCore computeCore;

    public ControlUnitExecuteState(ComputeCore core, DecoderUnit decoder) {
        super(GenericCUState.EXECUTE);
        decoderCore = decoder;
        computeCore = core;
    }

    @Override
    public void performAction() {
        if(decoderCore.isValidInstruction()) {
            int programCounter = decoderCore.getProgramCounter();
            String mneumonic = decoderCore.getMneumonic();
            int[] operands = decoderCore.getOperands();

            computeCore.executeInstruction(programCounter, mneumonic, operands);
        }
        else {
            computeCore.checkInterrupts();
        }
    }

    @Override
    public int actionDuration() {
        if(decoderCore.isValidInstruction()) {
            return computeCore.instrExecTime(decoderCore.getMneumonic());
        }
        else {
            return 1;
        }
    }
}
