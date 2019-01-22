package com.zzzmode.appopsx.ui.permission;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OtherOp;
import com.zzzmode.appopsx.ui.core.SpHelper;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl on 2016/11/18.
 */
class AppPermissionAdapter extends RecyclerView.Adapter<AppPermissionAdapter.ViewHolder> implements
    View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  private List<OpEntryInfo> datas = new ArrayList<>();

  private OnSwitchItemClickListener listener;

  private boolean showPermDesc;
  private boolean showOpName;
  private boolean showPermTime;

  void setShowConfig(boolean showPermDesc, boolean showOpName, boolean showPermTime) {
    this.showPermDesc = showPermDesc;
    this.showOpName = showOpName;
    this.showPermTime = showPermTime;
  }

  void setDatas(List<OpEntryInfo> datas) {
    this.datas = datas;
  }

  List<OpEntryInfo> getDatas() {
    return datas;
  }

  void updateItem(OpEntryInfo info) {
    if (datas != null && info != null) {
      int i = datas.indexOf(info);
      if (i != -1 && i < datas.size()) {
        notifyItemChanged(i);
      }
    }
  }

  public void setListener(OnSwitchItemClickListener listener) {
    this.listener = listener;
  }


  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_permission_item, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    OpEntryInfo opEntryInfo = datas.get(position);
    holder.itemView.setOnClickListener(this);
    holder.itemView.setTag(holder);

    holder.switchCompat.setTag(opEntryInfo);
    if (opEntryInfo != null) {
      holder.icon.setImageResource(opEntryInfo.icon);
      if (opEntryInfo.opPermsLab != null) {
        holder.title.setText(opEntryInfo.opPermsLab);
      } else {
        holder.title.setText(opEntryInfo.opName);
      }

      if (showOpName && opEntryInfo.opName != null) {
        holder.summary.setVisibility(View.VISIBLE);
        holder.summary.setText(opEntryInfo.opName);
      } else {
        holder.summary.setVisibility(View.GONE);
      }

      if (showPermDesc && opEntryInfo.opPermsDesc != null) {
        holder.summary.setVisibility(View.VISIBLE);
        holder.summary.setText(opEntryInfo.opPermsDesc);
      } else {
        if (!showOpName) {
          holder.summary.setVisibility(View.GONE);
        }
      }

      if (showPermTime ) {
        long time = opEntryInfo.opEntry.getTime();
        if(time > 0){
          holder.lastTime.setText(DateUtils
              .getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                  DateUtils.FORMAT_ABBREV_TIME));
        }else {
          holder.lastTime.setText(R.string.never_used);
        }
        holder.lastTime.setVisibility(View.VISIBLE);

      } else {
        holder.lastTime.setVisibility(View.GONE);
      }

      holder.switchCompat.setOnCheckedChangeListener(null);
      holder.switchCompat.setChecked(opEntryInfo.isAllowed());
      holder.switchCompat.setOnCheckedChangeListener(this);
    }
  }

  @Override
  public int getItemCount() {
    return datas.size();
  }

  @Override
  public void onClick(final View v) {
    if (v.getTag() instanceof ViewHolder) {
      int position = ((ViewHolder) v.getTag()).getAdapterPosition();
      OpEntryInfo opEntryInfo = datas.get(position);
      if(OtherOp.isOtherOp(opEntryInfo.opEntry.getOp()) && !SpHelper.isIgnoredNetOps(v.getContext(),opEntryInfo.opEntry.getOp())){


        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            ((ViewHolder) v.getTag()).switchCompat.toggle();
          }
        };
        showWarning(v.getContext(),opEntryInfo,runnable);

      }else {
        ((ViewHolder) v.getTag()).switchCompat.toggle();
      }

    }
  }

  private void showWarning(final Context context,final OpEntryInfo opEntryInfo,final Runnable runnable){
    AlertDialog alertDialog=new AlertDialog.Builder(context)
        .setTitle(android.R.string.dialog_alert_title)
        .setMessage(R.string.other_op_hint)
        .setPositiveButton(android.R.string.ok, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            runnable.run();
          }
        })
        .setNegativeButton(android.R.string.cancel,null)
        .setNeutralButton(R.string.other_op_stop_show, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            SpHelper.ignoredNetOps(context,opEntryInfo.opEntry.getOp());
          }
        })
        .create();
    alertDialog.show();
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (buttonView.getTag() instanceof OpEntryInfo && listener != null) {
      listener.onSwitch(((OpEntryInfo) buttonView.getTag()), isChecked);
    }
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    ImageView icon;
    TextView title;
    TextView summary;
    TextView lastTime;
    SwitchCompat switchCompat;

    ViewHolder(View itemView) {
      super(itemView);
      icon = (ImageView) itemView.findViewById(R.id.img_group);
      title = (TextView) itemView.findViewById(android.R.id.title);
      summary = (TextView) itemView.findViewById(android.R.id.summary);
      lastTime = (TextView) itemView.findViewById(R.id.last_time);
      switchCompat = (SwitchCompat) itemView.findViewById(R.id.switch_compat);

    }
  }

  public interface OnSwitchItemClickListener {

    void onSwitch(OpEntryInfo info, boolean v);
  }

}
