package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.network.MultiPortSerializer;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
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

    private ObservableControlUnit cu; // Control Unit
    protected ObservableMultiMemoryPort multiMemoryPort;
    protected MultiPortSerializer<MemoryPort, MultiMemoryPort> serializer;

    public UniMemoryCpuCore(ComputeCoreInterface core, MemoryPort dataMemory){
        serializer = createSerializer(dataMemory, PortIdentifier.values().length);
        multiMemoryPort = (ObservableMultiMemoryPort) serializer.getObservable();

        MemoryPort[] serializerInputs = serializer.getInputs();
        cu = new ObservableControlUnit(createControlUnit(core,
                serializerInputs[PortIdentifier.INSTRUCTION_MEM.ordinal()],
                serializerInputs[PortIdentifier.DATA_MEM.ordinal()]));
    }

    @Override
    public ObservableControlUnit getControlUnit() {
        return cu;
    }

    public ObservableMultiMemoryPort getObservableMultiMemoryPort() {
        return multiMemoryPort;
    }
}
