package com.devking.pdf_v3;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.splashVideoView);

        // Set the path to the video (from the raw folder)
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pdf);
        videoView.setVideoURI(videoUri);

        // Start the video
        videoView.start();

        // Set a listener to know when the video finishes
        videoView.setOnCompletionListener(mediaPlayer -> {
            // After the video ends, navigate to the MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Finish SplashActivity so the user can't go back to it
        });

        // Optional: Add a listener for any errors during video playback
        videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
            // Handle the error (perhaps load the MainActivity directly)
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        });
    }
}
