/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * This element is for adding a divider between items in a RecyclerView.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    /**
     * The instance for the divider drawable.
     */
    private final Drawable mDivider;

    /**
     * The constructor for this item decoration.
     * 
     * @param context
     *            The context in which this decoration is attached.
     */
    public DividerItemDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.listDivider });
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    @Override // RecyclerView.ItemDecoration
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawVertical(c, parent);
    }

    /**
     * This method draws a vertical line under an item.
     * 
     * @param canvas
     *            The canvas in which draw the line.
     * @param recyclerView
     *            The RecyclerView that the item comes from.
     */
    private void drawVertical(Canvas canvas, RecyclerView recyclerView) {
        // The extremities for the line between the end and the start (left and right) of the recycler view.
        final int left = recyclerView.getPaddingLeft();
        final int right = recyclerView.getWidth() - recyclerView.getPaddingRight();

        // to draw the line for each child of the recyclerView except the last one.
        final int childCount = recyclerView.getChildCount();
        for (int i = 0; i < childCount-1; i++) {
            final View child = recyclerView.getChildAt(i);
            // we place the divider after the margin of the item - child.
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            // the extremities for the divider between the bottom of the child and the end of the space available for
            // the divider.
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            // we draw the divider line.
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }

    @Override // RecyclerView.ItemDecoration
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (mDivider == null)
            return;

        outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
    }
}