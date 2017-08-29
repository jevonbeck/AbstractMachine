package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnitAlt;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class HarvardAltCore extends CpuAltCore {

    public HarvardAltCore(CompCore core, ReadPort instructionCache){
        initCpuCore(core, instructionCache);
    }

    @Override
    protected ControlUnitAltCore createControlUnit(CompCore core,
                                                   ReadPort instructionCache) {
        return new PipelinedControlUnitAlt(core, instructionCache);
    }
}
