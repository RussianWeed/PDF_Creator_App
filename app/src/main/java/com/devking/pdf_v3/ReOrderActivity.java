package com.devking.pdf_v3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReOrderGridAdapter reOrderGridAdapter;
    private List<Bitmap> bitmapList;
    private ArrayList<String> imagePaths; // Store image paths
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_order);

        recyclerView = findViewById(R.id.recycler_view_reorder);
        doneButton = findViewById(R.id.button_done);

        // Get the image paths passed from ImageDisplayActivity
        imagePaths = getIntent().getStringArrayListExtra("imagePaths");

        // Load bitmaps from image paths
        bitmapList = new ArrayList<>();
        for (String path : imagePaths) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            bitmapList.add(bitmap);
        }

        // Set up RecyclerView with GridLayoutManager
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns
        reOrderGridAdapter = new ReOrderGridAdapter(this, bitmapList, imagePaths);
        recyclerView.setAdapter(reOrderGridAdapter);

        // Set up ItemTouchHelper for drag and drop
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Swap both image paths and bitmaps
                Collections.swap(imagePaths, fromPosition, toPosition);
                Collections.swap(bitmapList, fromPosition, toPosition);

                // Notify adapter of the move
                reOrderGridAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe action
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Handle done button click
        doneButton.setOnClickListener(v -> {
            // Return the reordered image paths to ImageDisplayActivity
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("reorderedImagePaths", imagePaths);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
