package com.devking.pdf_v3;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class ImageDisplayActivity extends AppCompatActivity {

    private ArrayList<String> imagePaths;
    private EditText pdfNameEditText;
    private ViewPager2 viewPager;
    private List<Bitmap> bitmapList = new ArrayList<>();  // List of bitmaps for the ViewPager2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        Button createPdfButton = findViewById(R.id.btn_create_pdf);
        pdfNameEditText = findViewById(R.id.pdf_name);
        viewPager = findViewById(R.id.viewPager);

        // Get the image paths passed from MainActivity
        imagePaths = getIntent().getStringArrayListExtra("imagePaths");

        createPdfButton.setOnClickListener(v -> createPdf());

        Log.d("ImagePaths", "Image paths: " + imagePaths);

        // Check if the imagePaths list is null or empty
        if (imagePaths != null && !imagePaths.isEmpty()) {
            loadBitmaps();  // Load the bitmaps from the paths
            setupViewPager();  // Set up the ViewPager2 with the bitmaps
        } else {
            // Handle the case where there are no images
            Toast.makeText(this, "No images available", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBitmaps() {
        // Decode image paths into bitmaps and add to bitmapList
        for (String path : imagePaths) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                bitmapList.add(bitmap);
            }
        }
    }

    private void setupViewPager() {
        // Set up the adapter with the bitmap list for the ViewPager2
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, bitmapList);
        viewPager.setAdapter(adapter);
    }

    private void createPdf() {
        String pdfName = pdfNameEditText.getText().toString().trim();  // Get the name from EditText

        if (pdfName.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the PDF", Toast.LENGTH_SHORT).show();
            return;  // Exit the method if no name is provided
        }

        if (!bitmapList.isEmpty()) {
            PDFcreator pdfCreator = new PDFcreator();
            pdfCreator.createPDF(this, bitmapList, pdfName);  // Use the provided name for the PDF
        } else {
            Toast.makeText(this, "No valid images to include in the PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
