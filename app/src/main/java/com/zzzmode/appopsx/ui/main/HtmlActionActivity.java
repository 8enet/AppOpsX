package com.zzzmode.appopsx.ui.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;

/**
 * Created by zl on 2017/5/21.
 */

public class HtmlActionActivity extends BaseActivity {

  public static final String EXTRA_URL = "extra.url";

  private WebView webView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    Intent intent = getIntent();
    setTitle(intent.getStringExtra(Intent.EXTRA_TITLE));
    String url = intent.getStringExtra(EXTRA_URL);

    webView = findViewById(R.id.web_view);

    webView.loadUrl(url);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }


  @Override
  protected void onDestroy() {
    try {
      ((ViewGroup) webView.getParent()).removeView(webView);
      webView.removeAllViews();
      webView.destroy();
    } catch (Exception e) {
      e.printStackTrace();
    }
    super.onDestroy();
  }
}
