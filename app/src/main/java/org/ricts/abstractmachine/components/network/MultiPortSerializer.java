package org.ricts.abstractmachine.components.network;


import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.observables.ObservableType;

/**
 * Created by Jevon on 14/01/2017.
 */

public abstract class MultiPortSerializer<PortInterface, SerializerCore> extends Device {
    protected abstract SerializerCore createSerializerCore(final PortInterface targetPort);
    protected abstract PortInterface[] createInputs(SerializerCore serializerCore, int inputCount);
    protected abstract ObservableType<SerializerCore> createObservable(SerializerCore serializerCore);

    protected int portIndexWidth, portIndexMask;

    private PortInterface[] inputs;
    private ObservableType<SerializerCore> observable;

    @SuppressWarnings("unchecked")
    public MultiPortSerializer(PortInterface targetPort, int inputCount) {
        portIndexWidth = bitWidth(inputCount);
        portIndexMask = bitMaskOfWidth(portIndexWidth);

        observable = createObservable(createSerializerCore(targetPort));
        inputs = createInputs((SerializerCore) observable, inputCount);
    }

    public ObservableType<SerializerCore> getObservable() {
        return observable;
    }

    public PortInterface[] getInputs() {
        return inputs;
    }
}
