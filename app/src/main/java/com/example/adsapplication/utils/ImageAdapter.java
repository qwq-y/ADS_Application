package com.example.adsapplication.utils;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adsapplication.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<Uri> imageUris;
    private OnAddButtonClickListener addButtonClickListener;

    public ImageAdapter(List<Uri> imageUris, OnAddButtonClickListener addButtonClickListener) {
        this.imageUris = imageUris;
        this.addButtonClickListener = addButtonClickListener;
    }

    public interface OnAddButtonClickListener {
        void onAddButtonClick();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button addButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            addButton = itemView.findViewById(R.id.addButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_with_add_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < imageUris.size()) {
            // 显示图片
            Uri imageUri = imageUris.get(position);
            holder.imageView.setImageURI(imageUri);
            holder.addButton.setVisibility(View.GONE);
        } else {
            // 显示添加按钮
            holder.imageView.setImageResource(R.drawable.placeholder_image); // 设置默认的图片占位符
            holder.addButton.setVisibility(View.VISIBLE);

            // 绑定添加按钮的点击事件
            holder.addButton.setOnClickListener(view -> {
                if (addButtonClickListener != null) {
                    addButtonClickListener.onAddButtonClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        // 加 1 是为了显示添加按钮
        return imageUris.size() + 1;
    }
}


