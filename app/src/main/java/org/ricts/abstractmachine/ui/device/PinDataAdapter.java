package org.ricts.abstractmachine.ui.device;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;


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
        PinView mainLayout = new PinView(parent.getContext());

        ViewGroup.LayoutParams lpMainlayout;
        switch(pinPosition) {
            case 2: // top
            case 3: // bottom
                lpMainlayout = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                break;
            case 0: // left
            case 1: // right
            default:
                lpMainlayout = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                break;
        }
        mainLayout.setLayoutParams(lpMainlayout);
        mainLayout.setPosition(pinPosition);

        return new ViewHolder(mainLayout);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ((PinView) viewHolder.itemView).setPinData(pinArray[position]);
    }

    @Override
    public int getItemCount() {
        return pinArray.length;
    }

    public DevicePin getItem(int position){
        return pinArray[position];
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
}
