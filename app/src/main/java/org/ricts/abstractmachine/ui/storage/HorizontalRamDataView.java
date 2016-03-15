package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.utils.CustomDimenRecyclerView;
import org.ricts.abstractmachine.ui.utils.DividerItemDecoration;

/**
 * Created by Jevon on 02/11/14.
 */
public class HorizontalRamDataView extends CustomDimenRecyclerView {
    private int dividerThickness;

    public HorizontalRamDataView(Context context) {
        super(context);
    }

    public HorizontalRamDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalRamDataView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(){
        setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));

        DividerItemDecoration decorator = new DividerItemDecoration(getContext(),
                DividerItemDecoration.HORIZONTAL_LIST);
        addItemDecoration(decorator);

        dividerThickness = decorator.getThickness();
    }

    @Override
    protected int findWidth(int widthMeasureSpec){
        int parentWidth = View.MeasureSpec.getSize(widthMeasureSpec);

        switch(View.MeasureSpec.getMode(widthMeasureSpec)){
            case View.MeasureSpec.UNSPECIFIED:
            case View.MeasureSpec.AT_MOST:
                //View item = getAdapter().getView(0, null, this);
                RecyclerView.Adapter adapter = getAdapter();
                if(adapter != null) {
                    RecyclerView.ViewHolder holder = adapter.createViewHolder(this, adapter.getItemViewType(0));
                    adapter.bindViewHolder(holder, 0);

                    View item = holder.itemView;
                    TextView addr = (TextView) item.findViewById(R.id.address);
                    TextView data = (TextView) item.findViewById(R.id.data);

                    // Measure the text
                    Rect addrBounds = new Rect();
                    String text = (String) addr.getText();
                    addr.getPaint().getTextBounds(text, 0, text.length(), addrBounds);

                    Rect dataBounds = new Rect();
                    text = (String) data.getText();
                    data.getPaint().getTextBounds(text, 0, text.length(), dataBounds);

                    float scaleFactor = getContext().getResources().getDisplayMetrics().density;
                    int result = (Math.max(Math.abs(addrBounds.width()), Math.abs(dataBounds.width())) +
                            dividerThickness + (int) (2 * scaleFactor)) * adapter.getItemCount();

                    return Math.min(result, parentWidth);
                }
            default:
                return parentWidth;
        }
    }

    @Override
    protected int findHeight(int heightMeasureSpec){
        int parentHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        switch(View.MeasureSpec.getMode(heightMeasureSpec)){
            case View.MeasureSpec.UNSPECIFIED:
            case View.MeasureSpec.AT_MOST:
                RecyclerView.Adapter adapter = getAdapter();
                if(adapter != null) {
                    RecyclerView.ViewHolder holder = adapter.createViewHolder(this, adapter.getItemViewType(0));
                    adapter.bindViewHolder(holder, 0);

                    View item = holder.itemView;
                    TextView addr = (TextView) item.findViewById(R.id.address);

                    // Measure the text
                    Rect bounds = new Rect();
                    String text = (String) addr.getText();
                    addr.getPaint().getTextBounds(text, 0, text.length(), bounds);

                    float scaleFactor = getContext().getResources().getDisplayMetrics().density;
                    int result = 2 * (Math.abs(bounds.height()) + (int) (10*scaleFactor));

                    return Math.min(result, parentHeight);
                }
            default:
                return parentHeight;
        }
    }
}
