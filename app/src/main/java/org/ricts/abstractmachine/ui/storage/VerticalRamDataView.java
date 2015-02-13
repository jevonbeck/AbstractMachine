package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.CustomDimenRecyclerView;
import org.ricts.abstractmachine.ui.DividerItemDecoration;


public class VerticalRamDataView extends CustomDimenRecyclerView {
    private int dividerThickness;

    public VerticalRamDataView(Context context) {
        super(context);
    }

    public VerticalRamDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalRamDataView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(){
        setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));

        DividerItemDecoration decorator = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST);
        addItemDecoration(decorator);

        dividerThickness = decorator.getThickness();
    }

    @Override
    protected int findWidth(int widthMeasureSpec){
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);

        switch(MeasureSpec.getMode(widthMeasureSpec)){
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                Adapter adapter = getAdapter();
                ViewHolder holder = adapter.createViewHolder(this, adapter.getItemViewType(0));
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

                int result = Math.abs(addrBounds.width()) + Math.abs(dataBounds.width()) + 6;

                return Math.min(result, parentWidth);
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
                TextView addr = (TextView) item.findViewById(R.id.address);

                // Measure the text
                Rect bounds = new Rect();
                String text = (String) addr.getText();
                addr.getPaint().getTextBounds(text, 0, text.length(), bounds);

                int result = adapter.getItemCount() * (Math.abs(bounds.height()) + 14 + dividerThickness);

                return Math.min(result, parentHeight);
            default:
                return parentHeight;
        }
    }
}
