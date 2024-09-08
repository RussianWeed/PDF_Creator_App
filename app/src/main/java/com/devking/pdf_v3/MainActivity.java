package com.devking.pdf_v3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import android.util.Size;


public class MainActivity extends AppCompatActivity {

    private ProcessCameraProvider cameraProvider;


    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private ImageCapture imageCapture;
    private ArrayList<String> capturedImagePaths = new ArrayList<>();
    private PreviewView previewView;

    private ImageView lastImage_btn;
    private TextView picture_number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        ImageView captureButton = findViewById(R.id.captureButton);
        lastImage_btn = findViewById(R.id.lastImage_btn);
        picture_number =findViewById(R.id.picture_num);


        requestCameraPermission();
        startCamera();

        captureButton.setOnClickListener(v -> captureImage());

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
                cameraProvider = cameraProviderFuture.get(); // Assign the cameraProvider instance
                bindPreview(cameraProvider); // Bind the preview and image capture use cases
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        // Set up the Preview with lower resolution
        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(1280, 720))  // Set lower resolution for the preview
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Set up the ImageCapture with lower resolution
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new Size(1280, 720))  // Set lower resolution for image capture
                .build();

        // Choose the back camera as default
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        // Unbind previous use cases and bind with the new ones
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }



    private void captureImage() {
        if (imageCapture != null) {
            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                    Bitmap bitmap = imageProxyToBitmap(imageProxy);
                    if (bitmap != null) {
                        String imagePath = saveBitmap(bitmap, "captured_image_" + System.currentTimeMillis()); // Save image with timestamp
                        if (imagePath != null) {
                            capturedImagePaths.add(imagePath); // Add the file path to the list
                            lastImage_btn.setImageBitmap(bitmap);// Update the small ImageView with the last captured image
                            picture_number.setText(""+capturedImagePaths.size());
                            Toast.makeText(MainActivity.this, "Image Captured", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Error Saving Image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error Capturing Image", Toast.LENGTH_SHORT).show();
                    }
                    imageProxy.close(); // Always close the imageProxy to free resources
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Toast.makeText(MainActivity.this, "Capture Failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        if (image.getFormat() == ImageFormat.YUV_420_888 && image.getPlanes().length == 3) {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new android.graphics.Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);

            byte[] imageBytes = out.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
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
            cameraProvider.unbindAll(); // Unbind all camera use cases to avoid issues when the activity is paused
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll(); // Unbind all camera use cases when the activity is destroyed
        }
    }

}
