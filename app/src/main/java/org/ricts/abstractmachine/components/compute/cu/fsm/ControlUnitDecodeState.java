package org.ricts.abstractmachine.components.compute.cu.fsm;

import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.interfaces.FetchCore;

/**
 * Created by Jevon on 15/07/2017.
 */

public class ControlUnitDecodeState extends ControlUnitState {
    private DecoderUnit decoderCore;
    private FetchCore fetchCore;

    public ControlUnitDecodeState(DecoderUnit decoder, FetchCore fetcher) {
        super(GenericCUState.DECODE);
        decoderCore = decoder;
        fetchCore = fetcher;
    }

    @Override
    public void performAction() {
        decoderCore.decode(fetchCore.getInstructionPC(), fetchCore.getIR());
    }

    @Override
    public int actionDuration() {
        return 1;
    }
}
