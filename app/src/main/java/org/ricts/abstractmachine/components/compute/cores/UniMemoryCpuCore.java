package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPortMuxCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableType;

/**
 * Created by Jevon on 14/08/2016.
 */
public abstract class UniMemoryCpuCore extends CpuCore {
    private enum PortIdentifier {
        INSTRUCTION_MEM, DATA_MEM
    }

    protected abstract MultiPortSerializer<MemoryPort, MemoryPortMuxCore> createSerializer(
            MemoryPort memory, int inputCount);

    private ObservableControlUnit cu; // Control Unit
    private ObservableType<MemoryPortMuxCore> serializer;

    public UniMemoryCpuCore(ComputeCoreInterface core, MemoryPort dataMemory){
        MultiPortSerializer<MemoryPort, MemoryPortMuxCore> serial =
                createSerializer(dataMemory, PortIdentifier.values().length);
        serializer = serial.getObservable();

        MemoryPort[] muxPorts = serial.getInputs();
        cu = new ObservableControlUnit(createControlUnit(core,
                muxPorts[PortIdentifier.INSTRUCTION_MEM.ordinal()],
                muxPorts[PortIdentifier.DATA_MEM.ordinal()]));
    }

    @Override
    public ObservableControlUnit getControlUnit() {
        return cu;
    }

    public ObservableType<MemoryPortMuxCore> getSerializer() {
        return serializer;
    }
}
