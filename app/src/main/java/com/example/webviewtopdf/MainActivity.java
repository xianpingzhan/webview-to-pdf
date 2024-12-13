package com.example.webviewtopdf;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.webviewtopdf.screenshot.ImageToPdf;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(v -> {
            Toast.makeText(this, "开始导出pdf", Toast.LENGTH_SHORT).show();
            ArrayList<String> strings = new ArrayList<>();
            strings.add("file:///android_asset/test/index.html");
            strings.add("file:///android_asset/test/report.html");
            new ImageToPdf(this, findViewById(R.id.main), strings);
        });
    }
}