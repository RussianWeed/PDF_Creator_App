package com.devking.pdf_v3;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReOrderGridAdapter extends RecyclerView.Adapter<ReOrderGridAdapter.ViewHolder> {

    private Context context;
    private List<Bitmap> bitmapList;
    private List<String> imagePaths;

    public ReOrderGridAdapter(Context context, List<Bitmap> bitmapList, List<String> imagePaths) {
        this.context = context;
        this.bitmapList = bitmapList;
        this.imagePaths = imagePaths;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_reorder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap bitmap = bitmapList.get(position);
        holder.imageView.setImageBitmap(bitmap);
        holder.orderTextView.setText(String.valueOf(position + 1)); // Display order (1-based index)
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView orderTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            orderTextView = itemView.findViewById(R.id.order_text_view);
        }
    }
}
