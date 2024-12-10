package com.ernest.cookify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


public class MainActivity extends AppCompatActivity {

    private TextView tvResep;
    private ImageView ivMakanan;
    private Button btnSubmit;
    private ProgressBar progressBar;

    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        SplashScreen splashscreen = SplashScreen.installSplashScreen(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        registerResult();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            tvResep = findViewById(R.id.tvResep);
            ivMakanan = findViewById(R.id.ivMakanan);
            btnSubmit = findViewById(R.id.btnSubmit);
            progressBar = findViewById(R.id.progressBar);


            return insets;
        });
    }

    public void btnGallery(View view) {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    @SuppressLint("NewApi")
    public void btnSubmit(View view) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        ivMakanan.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(ivMakanan.getDrawingCache());
        ivMakanan.setDrawingCacheEnabled(false);

        Content content = new Content.Builder()
                .addText(getString(R.string.keyword))
                .addImage(bitmap)
                .build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                tvResep.setText(resultText);
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
            }

            @Override
            public void onFailure(Throwable t) {
                tvResep.setText(t.toString());
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
            }
        }, this.getMainExecutor());
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            Uri imageUri = result.getData().getData();
                            ivMakanan.setImageURI(imageUri);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, R.string.no_image, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}