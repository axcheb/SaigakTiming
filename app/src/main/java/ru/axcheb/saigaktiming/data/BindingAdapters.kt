package ru.axcheb.saigaktiming.data

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("app:errorMsg")
fun errorMsgTextView(view: TextView, errorMsg: Int?) {
    val str = errorMsg?.let { view.context.getString(it) }
    view.error = str
}

@BindingAdapter("app:errorMsg")
fun errorMsgTextInputLayout(view: TextInputLayout, errorMsg: Int?) {
    val str = errorMsg?.let { view.context.getString(it) }
    view.error = str
}