package ru.axcheb.saigaktiming.ui

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.addDivider(@DrawableRes dividerId: Int) {
    val divider = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
    ContextCompat.getDrawable(this.context, dividerId)
        ?.let { divider.setDrawable(it) }
    this.addItemDecoration(divider)
}