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

import java.util.ArrayList;
import java.util.List;

public class ImageDisplayActivity extends AppCompatActivity {

    private ArrayList<String> imagePaths;
    private GridView gridView;
    private EditText pdfNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        gridView = findViewById(R.id.grid_images);
        Button createPdfButton = findViewById(R.id.btn_create_pdf);
        pdfNameEditText = findViewById(R.id.pdf_name);

        // Get the image paths passed from MainActivity
        imagePaths = getIntent().getStringArrayListExtra("imagePaths");

        createPdfButton.setOnClickListener(v -> createPdf());

        Log.d("ImagePaths", "Image paths: " + imagePaths);

        // Check if the imagePaths list is null or empty
        if (imagePaths != null && !imagePaths.isEmpty()) {
            // Set the adapter for GridView
            ImageAdapter imageAdapter = new ImageAdapter(this, imagePaths);
            gridView.setAdapter(imageAdapter);
        } else {
            // Handle the case where there are no images
            Toast.makeText(this, "No images available", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPdf() {
        String pdfName = pdfNameEditText.getText().toString().trim();  // Get the name from EditText

        if (pdfName.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the PDF", Toast.LENGTH_SHORT).show();
            return;  // Exit the method if no name is provided
        }

        List<Bitmap> bitmaps = new ArrayList<>();
        for (String path : imagePaths) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                bitmaps.add(bitmap);
            }
        }

        if (!bitmaps.isEmpty()) {
            PDFcreator pdfCreator = new PDFcreator();
            pdfCreator.createPDF(this, bitmaps, pdfName);  // Use the provided name for the PDF
        } else {
            Toast.makeText(this, "No valid images to include in the PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
