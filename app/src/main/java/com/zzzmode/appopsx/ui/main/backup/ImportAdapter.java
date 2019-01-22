package com.zzzmode.appopsx.ui.main.backup;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.util.Formatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl on 2017/5/7.
 */

class ImportAdapter extends RecyclerView.Adapter<ImportAdapter.ViewHolder> implements
    View.OnClickListener {

  private List<RestoreModel> mDatas = new ArrayList<>();
  private OnItemClickListener itemClickListener;

  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ImportAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_import_layout, parent, false));
  }

  void showData(List<RestoreModel> datas) {
    mDatas.clear();
    if (datas != null) {
      mDatas.addAll(datas);
    }
    notifyDataSetChanged();
  }

  void setItemClickListener(OnItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bindData(mDatas.get(position));
    holder.itemView.setTag(holder);
    holder.delete.setTag(holder);

    holder.itemView.setOnClickListener(this);
    holder.delete.setOnClickListener(this);
  }

  @Override
  public int getItemCount() {
    return mDatas.size();
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    Object tag = v.getTag();
    if (id == R.id.fl_delete) {
      //delete
      if (tag instanceof ViewHolder) {
        showDelete(((ViewHolder) tag));
      }
    } else {
      //item
      if (tag instanceof ViewHolder) {
        if (itemClickListener != null) {
          final int position = ((ViewHolder) tag).getAdapterPosition();
          final RestoreModel model = mDatas.get(position);
          itemClickListener.onItemClick(model);
        }

      }

    }
  }


  private void showDelete(ViewHolder holder) {
    final Context context = holder.itemView.getContext();
    final int position = holder.getAdapterPosition();
    final RestoreModel model = mDatas.get(position);
    AlertDialog alertDialog = new AlertDialog.Builder(context)
        .setTitle(R.string.delete)
        .setMessage(context.getString(R.string.dlg_delete_msg, model.path))
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            boolean ret = BFileUtils.deleteBackFile(model.path);
            if (ret) {
              mDatas.remove(position);
              notifyItemRemoved(position);
            } else {
              Toast.makeText(context, "delete error", Toast.LENGTH_LONG).show();
            }
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .create();
    alertDialog.show();
  }


  static class ViewHolder extends RecyclerView.ViewHolder {

    TextView tvName;
    TextView tvTime;
    TextView tvBackCount;
    TextView tvFileSize;
    View delete;

    ViewHolder(View itemView) {
      super(itemView);
      tvName = itemView.findViewById(R.id.title);
      tvTime = itemView.findViewById(R.id.tv_time);
      tvBackCount = itemView.findViewById(R.id.tv_back_count);
      tvFileSize = itemView.findViewById(R.id.tv_file_len);
      delete = itemView.findViewById(R.id.fl_delete);
    }

    void bindData(RestoreModel model) {
      tvName.setText(model.fileName);
      tvTime.setText(Formatter.formatDate(model.createTime));

      tvBackCount.setText(tvBackCount.getResources().getString(R.string.backup_count, model.size));
      tvFileSize.setText(tvFileSize.getResources()
          .getString(R.string.backup_file_size, Formatter.formatFileSize(model.fileSize)));
    }
  }

  interface OnItemClickListener {

    void onItemClick(RestoreModel model);
  }

}
