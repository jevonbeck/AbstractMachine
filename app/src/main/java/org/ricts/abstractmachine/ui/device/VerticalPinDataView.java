package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.utils.CustomDimenRecyclerView;

public class VerticalPinDataView extends CustomDimenRecyclerView {

	public VerticalPinDataView(Context context) {
		super(context);
	}
	
	public VerticalPinDataView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalPinDataView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
	protected void init(){
        setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
	}

    @Override
	protected int findWidth(int widthMeasureSpec){
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
				
		switch(MeasureSpec.getMode(widthMeasureSpec)){
		case MeasureSpec.UNSPECIFIED:
		case MeasureSpec.AT_MOST:
            // N.B: Using specific classes to get pinData
            PinDataAdapter adapter = (PinDataAdapter) getAdapter();
            PinDataAdapter.ViewHolder holder =
                    adapter.createViewHolder(this, adapter.getItemViewType(0));

            int count = adapter.getItemCount();
            float scaleFactor = getContext().getResources().getDisplayMetrics().density;

            int max_width = 0;
			for(int x=0; x!= count; ++x){
                adapter.bindViewHolder(holder, x);

                View item = holder.itemView;

				TextView pin = (TextView) item.findViewById(R.id.PinView_pin_name);

				// Measure the text
				int pinWidth = (int) pin.getPaint().measureText((String) pin.getText());

				// find maximum
				int textMax = pinWidth + (int) (10*scaleFactor);
				max_width = Math.max(max_width, textMax);
			}

            return Math.min(max_width, parentWidth);
		default:
			return parentWidth;
		}		
	}

    @Override
    protected int findHeight(int heightMeasureSpec){
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        switch(MeasureSpec.getMode(heightMeasureSpec)){
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

                float scaleFactor = getContext().getResources().getDisplayMetrics().density;
                int result = (2*Math.abs(pinBounds.height()) + (int) (16*scaleFactor)) *
                        adapter.getItemCount();

                return Math.min(result, parentHeight);
            default:
                return parentHeight;
        }
    }
}
