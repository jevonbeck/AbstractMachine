package org.ricts.abstractmachine.ui.storage;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.ui.device.DeviceView;
import org.ricts.abstractmachine.ui.device.RelativePosition;
import org.ricts.abstractmachine.ui.utils.CustomDimenRecyclerView;

public class RomView extends DeviceView implements Observer {
    protected MemoryPortView memoryPins;
    protected boolean updatePins;

    private CustomDimenRecyclerView ramView;
    private MemoryDataAdapter dataAdapter;
    private int ramItemLayout;

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
        updatePins = true;
    }

    @Override
    protected View createPinView(Context context, RelativePosition pinPosition) {
        return new MemoryPortView(context, getDefaultAttributeSet(context, pinPosition));
    }

    @Override
    protected View createMainView(Context context, RelativePosition pinPosition) {
        CustomDimenRecyclerView ramView;
        switch (pinPosition) {
            case TOP:
                ramItemLayout = R.layout.mem_data_vertical;
                ramView = new HorizontalRamDataView(context);
                break;
            case BOTTOM:
                ramItemLayout = R.layout.mem_data_vertical;
                ramView = new HorizontalRamDataView(context);
                break;
            case LEFT:
                ramItemLayout = R.layout.mem_data_horizontal;
                ramView = new VerticalRamDataView(context);
                break;
            case RIGHT:
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
    public void update(Observable observable, Object o) {
        if(updatePins)
            memoryPins.update(observable, o); // initialise animation
    }

    public void setDataSource(ROM rom){
        /*** bind memoryView to their data ***/
        try {
            dataAdapter = new MemoryDataAdapter(getContext(), ramItemLayout,
                    R.id.data, rom);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ramView.setAdapter(dataAdapter);
    }

    public void setReadResponder(ReadPortView.ReadResponder responder){
        memoryPins.setReadResponder(responder);
    }

    public void setAnimatePins(boolean animate){
        updatePins = animate;
    }

    protected void updateRomUI(){
        dataAdapter.notifyDataSetChanged();
    }
}
