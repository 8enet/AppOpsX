package com.zzzmode.appopsx.ui.permission;

import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import java.util.List;

/**
 * Created by zl on 2017/5/1.
 */
interface IPermView {

  void showProgress(boolean show);

  void showError(CharSequence text);

  void showPerms(List<OpEntryInfo> opEntryInfos);

  void updateItem(OpEntryInfo info);
}
