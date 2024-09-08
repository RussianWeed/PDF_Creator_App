package com.devking.pdf_v3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageDisplayActivity extends AppCompatActivity {

    private static final String TAG = "ImageDisplayActivity";
    private ArrayList<String> imagePaths;
    private EditText pdfNameEditText;
    private ViewPager2 viewPager;
    private List<Bitmap> bitmapList = new ArrayList<>();
    private ImagePagerAdapter adapter;

    private Button removePageButton;
    private Button reorderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        Button createPdfButton = findViewById(R.id.btn_create_pdf);
        Button cropButton = findViewById(R.id.btn_crop);
        pdfNameEditText = findViewById(R.id.pdf_name);
        viewPager = findViewById(R.id.viewPager);

        removePageButton = findViewById(R.id.remove_btn);
        reorderButton = findViewById(R.id.reorder_btn);

        // Get the image paths passed from MainActivity
        imagePaths = getIntent().getStringArrayListExtra("imagePaths");

        createPdfButton.setOnClickListener(v -> createPdf());
        cropButton.setOnClickListener(v -> cropCurrentImage());

        removePageButton.setOnClickListener(v -> removeCurrentPage());
        reorderButton.setOnClickListener(v -> openReorderActivity());

        if (imagePaths != null && !imagePaths.isEmpty()) {
            loadBitmaps();
            setupViewPager();
        } else {
            Toast.makeText(this, "No images available", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBitmaps() {
        bitmapList.clear(); // Clear existing list
        for (String path : imagePaths) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    bitmapList.add(bitmap);
                } else {
                    Log.e(TAG, "Failed to decode bitmap from path: " + path);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading bitmap: " + e.getMessage());
            }
        }
    }

    private void setupViewPager() {
        adapter = new ImagePagerAdapter(this, bitmapList);
        viewPager.setAdapter(adapter);
    }

    private void cropCurrentImage() {
        int currentItem = viewPager.getCurrentItem();
        String imagePath = imagePaths.get(currentItem);
        Uri sourceUri = Uri.fromFile(new File(imagePath));
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));

        // Start uCrop activity
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1) // Set the aspect ratio, can be customized
                .withMaxResultSize(1000, 1000) // Maximum result size, can be customized
                .start(this);
    }

    private void removeCurrentPage() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem >= 0 && currentItem < bitmapList.size()) {
            bitmapList.remove(currentItem);
            imagePaths.remove(currentItem);
            adapter.notifyItemRemoved(currentItem);
            if (bitmapList.isEmpty()) {
                Toast.makeText(this, "No more images left", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openReorderActivity() {
        Intent intent = new Intent(this, ReOrderActivity.class);
        intent.putStringArrayListExtra("imagePaths", imagePaths);  // Pass paths instead of Bitmaps
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                Uri resultUri = UCrop.getOutput(data);
                int currentItem = viewPager.getCurrentItem();
                if (resultUri != null) {
                    String newPath = resultUri.getPath();
                    imagePaths.set(currentItem, newPath); // Update path to cropped image
                    Bitmap croppedBitmap = BitmapFactory.decodeFile(newPath);
                    if (croppedBitmap != null) {
                        bitmapList.set(currentItem, croppedBitmap);
                        adapter.notifyItemChanged(currentItem);
                    } else {
                        Toast.makeText(this, "Failed to load cropped image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Cropping result is null", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Throwable cropError = UCrop.getError(data);
                Toast.makeText(this, "Crop error: " + (cropError != null ? cropError.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            ArrayList<String> reorderedImagePaths = data.getStringArrayListExtra("reorderedImagePaths");
            if (reorderedImagePaths != null) {
                imagePaths.clear();
                imagePaths.addAll(reorderedImagePaths);
                loadBitmaps();  // Reload Bitmaps from new paths
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void createPdf() {
        String pdfName = pdfNameEditText.getText().toString().trim();

        if (pdfName.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bitmapList.isEmpty()) {
            try {
                PDFcreator pdfCreator = new PDFcreator();
                pdfCreator.createPDF(this, bitmapList, pdfName);
                Toast.makeText(this, "PDF created successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error creating PDF: " + e.getMessage());
                Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No valid images to include in the PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
