package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.utils.UiUtils;


/**
 * Created by Jevon on 08/11/14.
 */
public class PinDataAdapter extends RecyclerView.Adapter<PinDataAdapter.ViewHolder>{
    private static final String TAG = "PinDataAdapter";

    private int pinPosition;
    private DevicePin [] pinArray;

    public PinDataAdapter(DevicePin[] data, int position) throws Exception{
        super();
        pinArray = data;
        pinPosition = position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        PinView mainLayout = new PinView(context, UiUtils.makeAttributeSet(context, getResource()));
        mainLayout.setPosition(pinPosition);

        return new ViewHolder(mainLayout);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ((PinView) viewHolder.itemView).setData(pinArray[position]);
    }

    @Override
    public int getItemCount() {
        return pinArray.length;
    }

    public DevicePin getItem(int position){
        return pinArray[position];
    }

    private int getResource() {
        switch(pinPosition) {
            case 2: // top
            case 3: // bottom
                return R.xml.pinview_vertical;
            case 0: // left
            case 1: // right
            default:
                return R.xml.pinview_horizontal;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
}
