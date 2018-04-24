package com.zzzmode.appopsx.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import com.zzzmode.appopsx.ui.BaseActivity

/**
 * Created by zl on 2017/5/21.
 */

class HtmlActionActivity : BaseActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = intent?.getStringExtra(Intent.EXTRA_TITLE)
        val url = intent?.getStringExtra(EXTRA_URL)

        webView.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initView() {
        val layout = FrameLayout(this)
        webView = WebView(this)
        layout.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT)
        setContentView(layout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
    }

    override fun onDestroy() {
        try {
            (webView.parent as ViewGroup).removeView(webView)
            webView.removeAllViews()
            webView.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onDestroy()
    }

    companion object {

        val EXTRA_URL = "extra.url"
    }
}
