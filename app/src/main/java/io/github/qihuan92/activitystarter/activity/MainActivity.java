package io.github.qihuan92.activitystarter.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import io.github.qihuan92.activitystarter.R;

public class MainActivity extends AppCompatActivity {

    private Button btnDetail;
    private Button btnSelectColor;

    private String currentColor;

    private final ActivityResultLauncher<ColorSelectActivityBuilder> launcher =
            ColorSelectActivityBuilder.registerForActivityResult(this, result -> {
                if (result.resultCode == RESULT_OK) {
                    String color = result.color;
                    btnSelectColor.setBackgroundColor(Color.parseColor(color));
                    Toast.makeText(this, "选中颜色: " + color, Toast.LENGTH_SHORT).show();
                    currentColor = color;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            currentColor = savedInstanceState.getString("currentColor");
        }

        bindView();
        initView();

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("id", 123456L);
        intent.putExtra("userId", "100008");
        intent.putExtra("title", "测试");
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentColor", currentColor);
    }

    private void bindView() {
        btnDetail = findViewById(R.id.btn_detail);
        btnSelectColor = findViewById(R.id.btn_select_color);
    }

    private void initView() {
        btnDetail.setOnClickListener(view -> {
            DetailActivityBuilder.builder(123456L, "100008")
                    .title("测试标题")
                    .start(this);
        });

        if (!TextUtils.isEmpty(currentColor)) {
            btnSelectColor.setBackgroundColor(Color.parseColor(currentColor));
        }
        btnSelectColor.setOnClickListener(view -> {
            launcher.launch(ColorSelectActivityBuilder.builder(currentColor));
        });
    }
}