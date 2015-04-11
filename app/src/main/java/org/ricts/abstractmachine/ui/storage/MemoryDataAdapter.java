package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.Device;

/**
 * Created by Jevon on 20/09/14.
 */
public class MemoryDataAdapter extends RecyclerView.Adapter<MemoryDataAdapter.ViewHolder>{
    private int mViewType; // resourceId
    private int textViewID;

    private int[] memoryArray;
    private int addressWidth;
    private int dataWidth;

    public MemoryDataAdapter(Context c, int resource, int textViewResourceId, int[] data) throws Exception{
        View testView = View.inflate(c, resource, null);
        TextView index = (TextView) testView.findViewById(R.id.address);
        TextView value = (TextView) testView.findViewById(textViewResourceId);

        if(index == null || value == null || textViewResourceId == R.id.address){
            throw new Exception("'resource' must contain a TextView with android:id=\"@+id/address\" and another TextView");
        }
        else {
            mViewType = resource;
            textViewID = textViewResourceId;
            memoryArray = data;
        }
    }


    @Override
    public int getItemViewType(int position){
        return mViewType;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        View convertView = viewHolder.itemView;

        TextView index = (TextView) convertView.findViewById(R.id.address);
        index.setText(Device.formatNumberInHex(position, addressWidth));

        TextView value = (TextView) convertView.findViewById(textViewID);
        value.setText(Device.formatNumberInHex(memoryArray[position], dataWidth));
    }

    @Override
    public int getItemCount() {
        return memoryArray.length;
    }

    public void setMemoryParams(int dWidth, int aWidth){
        dataWidth = dWidth;
        addressWidth = aWidth;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
}
