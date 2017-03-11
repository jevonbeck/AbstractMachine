package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeCoreInterface;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;
import org.ricts.abstractmachine.components.observables.ObservableMultiMemoryPort;

/**
 * Created by Jevon on 14/08/2016.
 */
public abstract class UniMemoryCpuCore extends CpuCore {
    public enum PortIdentifier {
        INSTRUCTION_MEM, DATA_MEM
    }

    protected abstract MultiPortSerializer<MemoryPort, MultiMemoryPort> createSerializer(
            MemoryPort memory, int inputCount);

    protected ObservableMultiMemoryPort multiMemoryPort;
    protected MultiPortSerializer<MemoryPort, MultiMemoryPort> serializer;

    public UniMemoryCpuCore(UniMemoryComputeCoreInterface core, MemoryPort dataMemory){
        serializer = createSerializer(dataMemory, PortIdentifier.values().length);
        multiMemoryPort = (ObservableMultiMemoryPort) serializer.getObservable();

        MemoryPort[] serializerInputs = serializer.getInputs();
        createObservableControlUnit(core, serializerInputs[PortIdentifier.INSTRUCTION_MEM.ordinal()]);
        core.setDataMemory(serializerInputs[PortIdentifier.DATA_MEM.ordinal()]);
    }

    public ObservableMultiMemoryPort getObservableMultiMemoryPort() {
        return multiMemoryPort;
    }
}
