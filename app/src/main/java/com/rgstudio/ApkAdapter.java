package com.rgstudio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ApkAdapter extends RecyclerView.Adapter<ApkAdapter.ApkViewHolder> {

    private Context context;
    private List<ApkModel> apkList;
    private boolean isAdmin;
    private OnApkClickListener listener;

    public interface OnApkClickListener {
        void onDetailClick(ApkModel apk);
        void onDownloadClick(ApkModel apk);
        void onEditClick(ApkModel apk);
        void onDeleteClick(ApkModel apk);
    }

    public ApkAdapter(Context context, List<ApkModel> apkList, boolean isAdmin,
                      OnApkClickListener listener) {
        this.context = context;
        this.apkList = apkList != null ? apkList : new ArrayList<>();
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_apk, parent, false);
        return new ApkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApkViewHolder holder, int position) {
        ApkModel apk = apkList.get(position);

        holder.tvTitle.setText(apk.getTitle());
        holder.tvVersion.setText("v" + apk.getVersion());
        holder.tvSize.setText(apk.getSize());
        holder.tvDescription.setText(apk.getDescription());

        // Load thumbnail with Glide
        if (apk.getImageUrl() != null && !apk.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(apk.getImageUrl())
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .centerCrop()
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.logo_placeholder);
        }

        // Show admin actions
        if (isAdmin) {
            holder.adminActions.setVisibility(View.VISIBLE);
        } else {
            holder.adminActions.setVisibility(View.GONE);
        }

        // Click listeners
        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) listener.onDetailClick(apk);
        });

        holder.btnDownload.setOnClickListener(v -> {
            if (listener != null) listener.onDownloadClick(apk);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(apk);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(apk);
        });
    }

    @Override
    public int getItemCount() {
        return apkList.size();
    }

    public void updateList(List<ApkModel> newList) {
        this.apkList = newList;
        notifyDataSetChanged();
    }

    public List<ApkModel> getApkList() {
        return apkList;
    }

    public static class ApkViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvVersion, tvSize, tvDescription;
        MaterialButton btnDetail, btnDownload;
        ImageButton btnEdit, btnDelete;
        LinearLayout adminActions;

        public ApkViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvVersion = itemView.findViewById(R.id.tvVersion);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            adminActions = itemView.findViewById(R.id.adminActions);
        }
    }
}
