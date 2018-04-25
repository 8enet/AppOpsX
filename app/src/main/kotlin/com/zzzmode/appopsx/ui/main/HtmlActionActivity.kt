package com.zzzmode.appopsx.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_html.*
import kotlinx.android.synthetic.main.layout_appbar.*

/**
 * Created by zl on 2017/5/21.
 */

class HtmlActionActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html)

        setSupportActionBar(toolbar)
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

        const val EXTRA_URL = "extra.url"
    }
}
