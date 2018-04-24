/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zzzmode.appopsx.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.support.v7.preference.DialogPreference
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.util.AttributeSet
import android.view.View
import android.widget.NumberPicker
import com.zzzmode.appopsx.R

class NumberPickerPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    var value: Int = 0
        set(value) {
            field = value
            persistInt(this.value)
            summary = String.format(defaultSummary, value)
            notifyChanged()
        }
    private var maxValue: Int = 0
    private var minValue: Int = 0

    private val defaultSummary: String

    init {

        defaultSummary = summary.toString()
        val a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference)

        try {
            maxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue, DEFAULT_maxValue)
            minValue = a.getInt(R.styleable.NumberPickerPreference_minValue, DEFAULT_minValue)
        } finally {
            a.recycle()
        }

        dialogLayoutResource = R.layout.numberpicker_dialog
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)

        dialogIcon = null

    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInt(index, DEFAULT_value)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value = if (restorePersistedValue) getPersistedInt(value) else defaultValue as Int
    }


    class NumberPickerPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
        private var picker: NumberPicker? = null
        private var currentValue = 1

        private val numberPickerPreference: NumberPickerPreference
            get() = this.preference as NumberPickerPreference

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (savedInstanceState == null) {
                currentValue = numberPickerPreference.value
            } else {
                currentValue = savedInstanceState.getInt(SAVE_STATE_VALUE)
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            outState.putInt(SAVE_STATE_VALUE, currentValue)
        }

        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)
            picker = view.findViewById<View>(R.id.numpicker_pref) as NumberPicker
            picker?.maxValue = numberPickerPreference.maxValue
            picker?.minValue = numberPickerPreference.minValue
            picker?.value = currentValue
        }

        override fun onDialogClosed(b: Boolean) {
            if (b) {
                picker!!.clearFocus()
                val value = picker!!.value
                if (preference.callChangeListener(value)) {
                    numberPickerPreference.value = value
                }
            }
        }

        companion object {

            private const val SAVE_STATE_VALUE = "NumberPickerPreferenceDialogFragment.value"

            fun newInstance(key: String): NumberPickerPreferenceDialogFragmentCompat {
                val fragment = NumberPickerPreferenceDialogFragmentCompat()
                val b = Bundle(1)
                b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
                fragment.arguments = b
                return fragment
            }
        }
    }

    companion object {

        private const val DEFAULT_value = 0
        private const val DEFAULT_maxValue = 0
        private const val DEFAULT_minValue = 0
    }
}