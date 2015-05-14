package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 08/11/14.
 */
public class PinDataAdapter extends RecyclerView.Adapter<PinDataAdapter.ViewHolder>{
    private static final String TAG = "PinDataAdapter";

    private int mViewType; // resourceId
    private int pinPosition;
    private DevicePin [] pinArray;

    public PinDataAdapter(Context c, int resource, DevicePin[] data, int position) throws Exception{
        super();

        View testView = View.inflate(c, resource, null);
        TextView pin = (TextView) testView.findViewById(R.id.pinName);
        TextView signal = (TextView) testView.findViewById(R.id.signalText);

        if(pin == null || signal == null){
            throw new Exception("'resource' must contain a TextView with android:id=\"@+id/pinName\" and another TextView" +
                    " with android:id=\"@+id/signalText\"");
        }
        else {
            mViewType = resource;
            pinArray = data;
        }

        pinPosition = position;
    }

    @Override
    public int getItemViewType(int position){
        return mViewType;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mainLayout = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        // align pin name according to pin position on device
        TextView pin = (TextView) mainLayout.findViewById(R.id.pinName);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) pin.getLayoutParams();
        switch(pinPosition){
            case 2: // top
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case 3: // bottom
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case 0: // left
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 1: // right
            default:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
        }

        return new ViewHolder(mainLayout);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        View convertView = viewHolder.itemView;

        DevicePin pinItem = pinArray[position];

        TextView pin = (TextView) convertView.findViewById(R.id.pinName);
        pin.setText(pinItem.name);

        TextView signal = (TextView) convertView.findViewById(R.id.signalText);
        signal.setText(pinItem.data);

        // always create new animation, otherwise it will need to be reset
        Animation anim = null;
        switch (pinItem.direction){
            case LEFT:
                anim = AnimationUtils.loadAnimation(convertView.getContext(), R.anim.pin_transition_left);
                break;
            case RIGHT:
                anim = AnimationUtils.loadAnimation(convertView.getContext(), R.anim.pin_transition_right);
                break;
            case UP:
                anim = AnimationUtils.loadAnimation(convertView.getContext(), R.anim.pin_transition_up);
                break;
            case DOWN:
                anim = AnimationUtils.loadAnimation(convertView.getContext(), R.anim.pin_transition_down);
                break;
        }

        if(pinItem.startBehaviour == DevicePin.AnimStartBehaviour.DELAY){
            if(pinItem.animationDelay == -1){
                anim.setStartOffset(anim.getDuration());
            }
            else{
                anim.setStartOffset(pinItem.animationDelay);
            }
        }

        if(pinItem.action == DevicePin.PinAction.STATIONARY){
            anim.setDuration(0);
        }

        if(pinItem.animListener != null){
            anim.setAnimationListener(pinItem.animListener);
        }

        if(anim != null){
            signal.setAnimation(anim);
        }
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
