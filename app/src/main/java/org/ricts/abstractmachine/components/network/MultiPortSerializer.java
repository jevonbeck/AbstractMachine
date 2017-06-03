package org.ricts.abstractmachine.components.network;


import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.observable.ObservableType;

/**
 * Created by Jevon on 14/01/2017.
 */

public abstract class MultiPortSerializer<PortInterface, MultiPortInterface> extends Device {
    protected abstract MultiPortInterface createMultiPortInterface(final PortInterface targetPort);
    protected abstract PortInterface[] createInputs(MultiPortInterface multiPortInterface, int inputCount);
    protected abstract ObservableType<MultiPortInterface> createObservable(MultiPortInterface multiPortInterface);

    protected int portIndexWidth, portIndexMask;

    private PortInterface[] inputs;
    private ObservableType<MultiPortInterface> observable;

    @SuppressWarnings("unchecked")
    public MultiPortSerializer(PortInterface targetPort, int inputCount) {
        portIndexWidth = bitWidth(inputCount);
        portIndexMask = bitMaskOfWidth(portIndexWidth);

        observable = createObservable(createMultiPortInterface(targetPort));
        inputs = createInputs((MultiPortInterface) observable, inputCount);
    }

    public ObservableType<MultiPortInterface> getObservable() {
        return observable;
    }

    public PortInterface[] getInputs() {
        return inputs;
    }
}
