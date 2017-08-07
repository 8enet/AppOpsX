package com.zzzmode.appopsx.ui.main.group;

import com.zzzmode.appopsx.ui.model.PermissionGroup;
import java.util.List;

/**
 * Created by zl on 2017/7/17.
 */
interface IPermGroupView {
  void changeTitle(int groupPosition,int childPosition,boolean allowed);
  void refreshItem(int groupPosition,int childPosition);
  void showList(List<PermissionGroup> value);
  void showError(Throwable e);
}
