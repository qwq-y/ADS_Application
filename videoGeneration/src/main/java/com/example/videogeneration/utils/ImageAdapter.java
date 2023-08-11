package com.example.videogeneration.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videogeneration.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final String TAG = "ww";

    private List<String> imageUrls;
    private int imageSize;
    private OnImageClickListener onImageClickListener;
    private int selectedItemPosition = -1;

    public ImageAdapter(List<String> imageUrls, int imageSize) {
        this.imageUrls = imageUrls;
        this.imageSize = imageSize;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        notifyDataSetChanged();
    }

    public void setSelectedItemPosition(int position) {
        selectedItemPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .into(holder.imageView);

        // 设置ImageView的高度等于宽度，以保持图片为正方形
        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.height = imageSize;
        holder.imageView.setLayoutParams(layoutParams);
        holder.overlayImageView.setLayoutParams(layoutParams);

        // 判断是否显示覆盖图片
        if (position == selectedItemPosition) {
            holder.overlayImageView.setVisibility(View.VISIBLE);
        } else {
            holder.overlayImageView.setVisibility(View.GONE);
        }

        // 添加点击监听
        holder.itemView.setOnClickListener(v -> {
            if (onImageClickListener != null) {
                setSelectedItemPosition(position); // 更新被点击的位置
                onImageClickListener.onImageClick(position);
                notifyDataSetChanged(); // 刷新适配器
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView overlayImageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            overlayImageView = itemView.findViewById(R.id.overlayImageView);
        }
    }

    // 定义点击监听器接口
    public interface OnImageClickListener {
        void onImageClick(int position);
    }
}
