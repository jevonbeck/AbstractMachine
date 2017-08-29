package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;

/**
 * Created by jevon.beckles on 17/08/2017.
 */

public class ControlUnitComputeState extends ControlUnitState {
    private DecoderUnit decoderCore;
    private CompCore compCore;

    public ControlUnitComputeState(CompCore core, DecoderUnit decoder) {
        super(GenericCUState.EXECUTE);
        decoderCore = decoder;
        compCore = core;
    }

    @Override
    public void performAction() {
        if(decoderCore.isValidInstruction()) {
            int programCounter = decoderCore.getProgramCounter();
            String groupName = decoderCore.getInstructionGroupName();
            int groupIndex = decoderCore.getInstructionGroupIndex();
            int[] operands = decoderCore.getOperands();

            compCore.executeInstruction(programCounter, groupName, groupIndex, operands);
        }
    }

    @Override
    public int actionDuration() {
        if(decoderCore.isValidInstruction()) {
            String groupName = decoderCore.getInstructionGroupName();
            int groupIndex = decoderCore.getInstructionGroupIndex();

            return compCore.instrExecTime(groupName, groupIndex);
        }
        else {
            return 1;
        }
    }
}
