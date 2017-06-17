package com.zzzmode.appopsx.ui.main.backup;

import com.zzzmode.appopsx.ui.model.PreAppInfo;
import java.util.List;

/**
 * Created by zl on 2017/5/7.
 */

class RestoreModel {

  long createTime;
  int version;
  int size;
  long fileSize;
  String path;
  String fileName;
  List<PreAppInfo> preAppInfos;
}
