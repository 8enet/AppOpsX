package com.zzzmode.appopsx.ui.main.backup

import kotlinx.android.synthetic.main.activity_backup.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.util.SparseArrayCompat
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.BaseActivity
import kotlinx.android.synthetic.main.layout_appbar.*

/**
 * Created by zl on 2017/5/7.
 */

class BackupActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.menu_backup)
        initView()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initView() {

        val titles = arrayOf<CharSequence>(getString(R.string.perm_export), getString(R.string.perm_restore))

        viewpager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {

            private val mFragments = SparseArrayCompat<Fragment>()

            override fun getItem(position: Int): Fragment? {
                var fragment: Fragment? = mFragments.get(position)
                if (fragment == null) {
                    when (position) {
                        0 -> {
                            fragment = ExportFragment()
                            fragment.arguments = Bundle(intent.extras)
                        }
                        1 -> fragment = ImportFragment()
                    }
                    if (fragment != null) {
                        mFragments.put(position, fragment)
                    }
                }
                return fragment
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return titles[position]
            }
        }

        tabs.setupWithViewPager(viewpager)
    }

    companion object {

        const val EXTRA_APPS = "extra.list.app"
    }
}
