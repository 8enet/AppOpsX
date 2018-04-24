package com.zzzmode.appopsx.ui.main

import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import com.github.promeg.pinyinhelper.Pinyin
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.ResourceObserver
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.regex.Pattern

/**
 * Created by zl on 2017/1/23.
 */
internal class SearchHandler (container: View){

    private lateinit var mBaseData: List<AppInfo>

    private var recyclerView: RecyclerView = container.findViewById<View>(R.id.search_result_recyclerView) as RecyclerView
    private var mAdapter: SearchResultAdapter = SearchResultAdapter()

    fun setBaseData(baseData: List<AppInfo>) {
        this.mBaseData = baseData
    }

    init {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(CommonDivderDecorator(recyclerView.context))
        recyclerView.itemAnimator = RefactoredDefaultItemAnimator()

        recyclerView.adapter = mAdapter
    }


    fun handleWord(text: String?) {
        if (TextUtils.isEmpty(text)) {
            mAdapter.clear()
            return
        }

        search(text!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : ResourceObserver<List<AppInfo>>() {

            override fun onNext(value: List<AppInfo>) {

                mAdapter.kw = text
                mAdapter.showItems(value)
                if (!value.isEmpty()) {
                    recyclerView.scrollToPosition(0)
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })


    }


    private fun search(key: String): Observable<List<AppInfo>> {
        return Observable.create { e ->
            val p = Pattern.compile(".*(?i)($key).*")
            val result = ArrayList<AppInfo>()
            for (info in mBaseData) {
                if (p.matcher(info.appName).matches()) {
                    result.add(info)
                } else {
                    if (info.pinyin == null) {
                        val sb = StringBuilder()
                        info.appName.toCharArray()
                                .map { Pinyin.toPinyin(it) }
                                .filterNot { TextUtils.isEmpty(it) }
                                .forEach { sb.append(it[0]) }

                        info.pinyin = sb.toString()
                    }
                    if (p.matcher(info.pinyin!!).matches()) {
                        result.add(info)
                    }
                }
            }
            e.onNext(result)
        }
    }

    private class SearchResultAdapter : MainListAdapter() {

        override val aEventId: String
            get() = AEvent.C_SEARCH_APP

        internal var kw: String? = null

        private val color by lazy {
            Color.parseColor("#FF4081")
        }

        internal fun clear() {
            appInfos.clear()
            notifyDataSetChanged()
        }

        override fun processText(name: String?): CharSequence? {
            name?.apply {
                return resultHighlight(kw, name, color)
            }
            return super.processText(name)
        }

        private fun resultHighlight(key: String?, text: String, color: Int): CharSequence {
            val phantom = text.toLowerCase()
            val k = key?.toLowerCase()

            if (k != null && phantom.contains(k)) {
                var st = 0
                val pos = ArrayList<Int>(3)
                st = phantom.indexOf(k,st)
                while (st != -1) {
                    pos.add(st)
                    st += key.length

                    st = phantom.indexOf(k,st)
                }
                val stringBuilder = SpannableStringBuilder(text)
                if (!pos.isEmpty()) {
                    for (idx in pos) {
                        stringBuilder.setSpan(ForegroundColorSpan(color), idx, idx + key.length,
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    }
                }
                return stringBuilder
            }
            return text
        }
    }

}
