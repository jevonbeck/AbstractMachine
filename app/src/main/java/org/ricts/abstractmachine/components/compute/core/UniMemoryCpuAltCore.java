package org.ricts.abstractmachine.components.compute.core;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.UniMemoryComputeAltCoreInterface;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;
import org.ricts.abstractmachine.components.observable.ObservableMultiMemoryPort;

/**
 * Created by Jevon on 14/08/2016.
 */
public abstract class UniMemoryCpuAltCore extends CpuAltCore {
    public enum SerializerInputId {
        INSTRUCTION_MEM, DATA_MEM
    }

    protected abstract MultiPortSerializer<MemoryPort, MultiMemoryPort> createSerializer(
            MemoryPort memory, int inputCount);

    protected ObservableMultiMemoryPort multiMemoryPort;
    protected MultiPortSerializer<MemoryPort, MultiMemoryPort> serializer;

    public UniMemoryCpuAltCore(UniMemoryComputeAltCoreInterface core, MemoryPort dataMemory){
        serializer = createSerializer(dataMemory, SerializerInputId.values().length);
        multiMemoryPort = (ObservableMultiMemoryPort) serializer.getObservable();

        MemoryPort[] serializerInputs = serializer.getInputs();
        initCpuCore(core, serializerInputs[SerializerInputId.INSTRUCTION_MEM.ordinal()]);
        core.setDataMemory(serializerInputs[SerializerInputId.DATA_MEM.ordinal()]);
    }

    public ObservableMultiMemoryPort getObservableMultiMemoryPort() {
        return multiMemoryPort;
    }
}
