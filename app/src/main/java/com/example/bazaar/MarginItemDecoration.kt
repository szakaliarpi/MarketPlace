package com.example.bazaar

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val spaceSizeHorizontal: Int, private val spaceSizeVertical: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceSizeVertical
            }
            left = spaceSizeHorizontal
            right = spaceSizeHorizontal
            bottom = spaceSizeVertical
        }
    }
}