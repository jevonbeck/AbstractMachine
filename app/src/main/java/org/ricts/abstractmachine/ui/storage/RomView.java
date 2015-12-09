package org.ricts.abstractmachine.ui.storage;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.ui.CustomDimenRecyclerView;
import org.ricts.abstractmachine.ui.UiUtils;
import org.ricts.abstractmachine.ui.device.MemoryPortView;
import org.ricts.abstractmachine.ui.device.ReadPortView;

public class RomView extends RelativeLayout implements ReadPort {
    protected int dataWidth;
    protected int addressWidth;

    protected MemoryDataAdapter dataAdapter;
    protected ROM rom;

    private CustomDimenRecyclerView ramView;
    private int ramItemLayout;

    protected MemoryPortView pinView;
    private int pinPosition;

    /** Standard Constructors **/
    public RomView(Context context) {
        this(context, null);
    }

    public RomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /*** extract XML attributes ***/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RomView);
        pinPosition = a.getInt(R.styleable.RomView_pinPosition, 1);
        a.recycle();

        /*** create children and determine layouts & positions based on attributes ***/
        LayoutParams lpRamView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        LayoutParams lpPinView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        pinView = new MemoryPortView(context, UiUtils.makeAttributeSet(context, getResourceId()));
        pinView.setId(R.id.romview_pindata);

        switch (pinPosition) {
            case 2: // top
                ramItemLayout = R.layout.mem_data_vertical;

                ramView = new HorizontalRamDataView(context);
                ramView.setId(R.id.romview_romdata);

                lpPinView.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(pinView, lpPinView);

                lpRamView.addRule(RelativeLayout.BELOW, pinView.getId());
                addView(ramView, lpRamView);
                break;
            case 3: // bottom
                ramItemLayout = R.layout.mem_data_vertical;

                ramView = new HorizontalRamDataView(context);
                ramView.setId(R.id.romview_romdata);

                addView(ramView, lpRamView);

                lpPinView.addRule(RelativeLayout.BELOW, ramView.getId());
                lpPinView.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(pinView, lpPinView);
                break;
            case 0: // left
                ramItemLayout = R.layout.mem_data_horizontal;

                ramView = new VerticalRamDataView(context);
                ramView.setId(R.id.romview_romdata);

                lpPinView.addRule(RelativeLayout.CENTER_VERTICAL);
                addView(pinView, lpPinView);

                lpRamView.addRule(RelativeLayout.RIGHT_OF, pinView.getId());
                addView(ramView, lpRamView);
                break;
            case 1: // right
            default:
                ramItemLayout = R.layout.mem_data_horizontal;

                ramView = new VerticalRamDataView(context);
                ramView.setId(R.id.romview_romdata);

                addView(ramView, lpRamView);

                lpPinView.addRule(RelativeLayout.RIGHT_OF, ramView.getId());
                lpPinView.addRule(RelativeLayout.CENTER_VERTICAL);
                addView(pinView, lpPinView);
                break;
        }
    }

    @Override
    public int read(int address) {
        return pinView.read(address); // initialise read animation & return underlying data
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
        pinView.initParams(dataWidth, addressWidth);
        pinView.setSource(rom);

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
        pinView.setReadResponder(responder);
    }

    private int getResourceId(){
        switch (pinPosition){
            case 2: // top
                return R.xml.memoryportview_top;
            case 3: // bottom
                return R.xml.memoryportview_bottom;
            case 0: // left
                return R.xml.memoryportview_left;
            case 1: // right
            default:
                return R.xml.memoryportview_right;
        }
    }
}
