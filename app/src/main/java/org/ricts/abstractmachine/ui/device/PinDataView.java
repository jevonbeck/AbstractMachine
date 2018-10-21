package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.utils.CustomDimenRecyclerView;

/**
 * Created by jevon on 01/10/2017.
 */

public abstract class PinDataView extends CustomDimenRecyclerView {
    private int scaleFactor;

    public PinDataView(Context context) {
        this(context, null);
    }

    public PinDataView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinDataView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scaleFactor = (int) context.getResources().getDisplayMetrics().density;
    }

    protected int getPinLengthDimension(int measureSpec) {
        int parentDimension = MeasureSpec.getSize(measureSpec);

        // N.B: Using specific classes to get pinData
        PinDataAdapter adapter = (PinDataAdapter) getAdapter();
        PinDataAdapter.ViewHolder holder =
                adapter.createViewHolder(this, adapter.getItemViewType(0));

        int count = adapter.getItemCount();

        int max_dimension = 0;
        for(int x=0; x!= count; ++x){
            adapter.bindViewHolder(holder, x);

            View item = holder.itemView;

            TextView pin = (TextView) item.findViewById(R.id.PinView_pin_name);

            // Measure the text
            int pinLength = (int) pin.getPaint().measureText((String) pin.getText());

            // find maximum
            int textMax = pinLength + (3*scaleFactor);
            max_dimension = Math.max(max_dimension, textMax);
        }

        return Math.min(max_dimension, parentDimension);
    }

    protected int getPinHeightDimension(int measureSpec) {
        int parentDimension = MeasureSpec.getSize(measureSpec);

        switch(MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                Adapter adapter = getAdapter();
                ViewHolder holder = adapter.createViewHolder(this, adapter.getItemViewType(0));
                adapter.bindViewHolder(holder, 0);

                View item = holder.itemView;
                TextView pin = (TextView) item.findViewById(R.id.PinView_pin_name);

                // Measure the text
                Rect pinBounds = new Rect();
                String text = (String) pin.getText();
                pin.getPaint().getTextBounds(text, 0, text.length(), pinBounds);

                int result = (2 * Math.abs(pinBounds.height()) + (16 * scaleFactor)) *
                        adapter.getItemCount();

                return Math.min(result, parentDimension);
            default:
                return parentDimension;
        }
    }
}
