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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        Button createPdfButton = findViewById(R.id.btn_create_pdf);
        Button cropButton = findViewById(R.id.btn_crop);
        pdfNameEditText = findViewById(R.id.pdf_name);
        viewPager = findViewById(R.id.viewPager);

        // Get the image paths passed from MainActivity
        imagePaths = getIntent().getStringArrayListExtra("imagePaths");

        createPdfButton.setOnClickListener(v -> createPdf());
        cropButton.setOnClickListener(v -> cropCurrentImage());

        if (imagePaths != null && !imagePaths.isEmpty()) {
            loadBitmaps();
            setupViewPager();
        } else {
            Toast.makeText(this, "No images available", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBitmaps() {
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of the uCrop activity
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                Uri resultUri = UCrop.getOutput(data);
                int currentItem = viewPager.getCurrentItem();
                try {
                    Bitmap croppedBitmap = BitmapFactory.decodeFile(resultUri.getPath());
                    if (croppedBitmap != null) {
                        bitmapList.set(currentItem, croppedBitmap);
                        adapter.notifyItemChanged(currentItem);
                    } else {
                        Toast.makeText(this, "Failed to load cropped image", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding cropped image: " + e.getMessage());
                    Toast.makeText(this, "Error loading cropped image", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Throwable cropError = UCrop.getError(data);
                Toast.makeText(this, "Crop error: " + (cropError != null ? cropError.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
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
