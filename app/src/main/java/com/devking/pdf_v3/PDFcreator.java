package com.devking.pdf_v3;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

public class PDFcreator {

    // Convert Bitmap to ByteArray for PDF
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    // Create a Multi-Page PDF with In-Memory Bitmaps
    public void createPDF(Context context, List<Bitmap> bitmaps, String pdfName) {
        // Create a new document
        Document document = new Document(PageSize.A4);

        // Get a writable output stream using MediaStore
        Uri pdfUri = createPdfUri(context, pdfName);

        if (pdfUri != null) {
            try {
                // Open output stream
                ContentResolver contentResolver = context.getContentResolver();
                OutputStream outputStream = contentResolver.openOutputStream(pdfUri);

                // Ensure output stream is valid
                if (outputStream != null) {
                    PdfWriter.getInstance(document, outputStream);
                    document.open();

                    for (Bitmap bitmap : bitmaps) {
                        byte[] imageData = bitmapToByteArray(bitmap);
                        Image img = Image.getInstance(imageData);

                        // Resize the image to fit the page
                        img.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                        img.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_MIDDLE);

                        document.add(img);  // Add the image to the PDF
                        document.newPage(); // Start a new page for each image
                    }

                    // Properly flush and close the output stream
                    document.close();
                    outputStream.flush();
                    outputStream.close();

                    Toast.makeText(context, "PDF created successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error opening output stream!", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Error creating PDF!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Error creating file URI!", Toast.LENGTH_SHORT).show();
        }
    }

    // Create a file URI for the PDF using MediaStore (for API 29 and above)
    private Uri createPdfUri(Context context, String pdfName) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfName + ".pdf");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
        } else {
            // For API levels lower than 29, we use traditional storage
            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + pdfName + ".pdf";
            contentValues.put(MediaStore.MediaColumns.DATA, pdfPath);
            return contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
        }
    }
}
