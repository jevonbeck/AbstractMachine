package org.ricts.abstractmachine.ui.storage;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.utils.CustomDimenRecyclerView;
import org.ricts.abstractmachine.ui.utils.UiUtils;

public class RomView extends DeviceView implements ReadPort {
    protected int dataWidth;
    protected int addressWidth;

    protected MemoryDataAdapter dataAdapter;
    protected ROM rom;

    private CustomDimenRecyclerView ramView;
    private int ramItemLayout;

    protected MemoryPortView memoryPins;

    /** Standard Constructors **/
    public RomView(Context context) {
        this(context, null);
    }

    public RomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ramView = (CustomDimenRecyclerView) mainView;
        memoryPins = (MemoryPortView) pinView;
    }

    @Override
    protected View createPinView(Context context, int pinPosition) {
        return new MemoryPortView(context, getDefaultAttributeSet(context, pinPosition));
    }

    @Override
    protected View createMainView(Context context, int pinPosition) {
        CustomDimenRecyclerView ramView;
        switch (pinPosition) {
            case 2: // top
                ramItemLayout = R.layout.mem_data_vertical;
                ramView = new HorizontalRamDataView(context);
                break;
            case 3: // bottom
                ramItemLayout = R.layout.mem_data_vertical;
                ramView = new HorizontalRamDataView(context);
                break;
            case 0: // left
                ramItemLayout = R.layout.mem_data_horizontal;
                ramView = new VerticalRamDataView(context);
                break;
            case 1: // right
            default:
                ramItemLayout = R.layout.mem_data_horizontal;
                ramView = new VerticalRamDataView(context);
                break;
        }
        return ramView;
    }

    @Override
    protected LayoutParams createMainViewLayoutParams() {
        return new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public int read(int address) {
        return memoryPins.read(address); // initialise read animation & return underlying data
    }

    @Override
    public int accessTime() {
        return rom.accessTime();
    }

    public void initMemory(int dWidth, int aWidth, int accessTime){
        dataWidth = dWidth;
        addressWidth = aWidth;

        // initialise rom (ramView data)
        rom = new ROM(dataWidth, addressWidth, accessTime);

        init();
    }

    public void setDataSource(ROM r){
        dataWidth = rom.dataWidth();
        addressWidth = rom.addressWidth();

        rom = r;
        init();
    }

    protected void init(){
        memoryPins.initParams(dataWidth, addressWidth);
        memoryPins.setSource(rom);

        /*** bind memoryView to their data ***/
        try {
            dataAdapter = new MemoryDataAdapter(getContext(), ramItemLayout,
                    R.id.data, rom.dataArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataAdapter.setMemoryParams(dataWidth, addressWidth);
        ramView.setAdapter(dataAdapter);
    }

    public void setMemoryData(List<Integer> data, int addrOffset){
        rom.setData(data, addrOffset);
    }

    public void setReadResponder(ReadPortView.ReadResponder responder){
        memoryPins.setReadResponder(responder);
    }
}
