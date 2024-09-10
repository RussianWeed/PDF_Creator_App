package com.devking.pdf_v3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import android.util.Size;


public class MainActivity extends AppCompatActivity {

    private ProcessCameraProvider cameraProvider;


    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_SELECT_IMAGE = 300;
    private ImageCapture imageCapture;
    private ArrayList<String> capturedImagePaths = new ArrayList<>();
    private PreviewView previewView;

    private ImageView lastImage_btn;
    private TextView picture_number;

    private  ImageView gallery_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        ImageView captureButton = findViewById(R.id.captureButton);
        lastImage_btn = findViewById(R.id.lastImage_btn);
        picture_number = findViewById(R.id.picture_num);
        gallery_btn = findViewById(R.id.select_from_gallery_button);

        requestCameraPermission();
        startCamera();

        captureButton.setOnClickListener(v -> captureImage());
        gallery_btn.setOnClickListener(v -> openGallery());

        lastImage_btn.setOnClickListener(v -> {
            if (!capturedImagePaths.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, ImageDisplayActivity.class);
                intent.putStringArrayListExtra("imagePaths", new ArrayList<>(capturedImagePaths));
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "No images captured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to open the gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  // Allow multiple selection
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_IMAGE) {
            handleSelectedImages(data);
        }
    }

    private String getPathFromUri(Uri uri) {
        if (uri == null) return null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } finally {
                cursor.close();
            }
        }
        return null;
    }



    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(1280, 720))
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new Size(1280, 720))
                .build();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.unbindAll();  // Unbind all use cases before binding new ones
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

        Log.d("MainActivity", "Camera successfully bound");
    }




    private void captureImage() {
        if (imageCapture != null) {
            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                    Bitmap bitmap = imageProxyToBitmap(imageProxy);
                    if (bitmap != null) {
                        String imagePath = saveBitmap(bitmap, "captured_image_" + System.currentTimeMillis());
                        addImageToList(imagePath);
                        updateThumbnailAndCount();
                        Toast.makeText(MainActivity.this, "Image Captured", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error Capturing Image", Toast.LENGTH_SHORT).show();
                    }
                    imageProxy.close();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Toast.makeText(MainActivity.this, "Capture Failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addImageToList(String imagePath) {
        if (imagePath != null) {
            Log.d("MainActivity", "Image path added: " + imagePath);
            capturedImagePaths.add(imagePath);
        } else {
            Log.e("MainActivity", "Error adding image path");
        }
    }


    private void updateThumbnailAndCount() {
        if (!capturedImagePaths.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(capturedImagePaths.get(capturedImagePaths.size() - 1));
            lastImage_btn.setImageBitmap(bitmap);
            picture_number.setText("" + capturedImagePaths.size());
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        if (image.getFormat() == ImageFormat.YUV_420_888 && image.getPlanes().length == 3) {
            // Existing conversion logic
        } else {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                Log.e("MainActivity", "Bitmap decoding failed");
            }
            return bitmap;
        }
        return null;
    }


    private void handleSelectedImages(Intent data) {
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                String path = getPathFromUri(imageUri);
                if (path != null) {
                    addImageToList(path);
                } else {
                    Toast.makeText(this, "Error getting image path", Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(this, count + " images selected", Toast.LENGTH_SHORT).show();
        } else if (data.getData() != null) {
            String path = getPathFromUri(data.getData());
            if (path != null) {
                addImageToList(path);
            } else {
                Toast.makeText(this, "Error getting image path", Toast.LENGTH_SHORT).show();
            }
        }
        updateThumbnailAndCount();
    }


    private String saveBitmap(Bitmap bitmap, String filename) {
        try {
            // Define file path for saving the image
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
            if (!directory.exists()) {
                directory.mkdirs(); // Create directory if it doesn't exist
            }

            // Create file within the directory
            File file = new File(directory, filename + ".jpg");

            // Write the bitmap to file
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            // Return the image path to be stored in the array
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if there was an error
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

}
