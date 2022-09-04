package io.github.qihuan92.activitystarter.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import io.github.qihuan92.activitystarter.annotation.Builder;
import io.github.qihuan92.activitystarter.databinding.ActivityMainBinding;

@Builder
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SELECT_COLOR = 1;
    private ActivityMainBinding binding;
    private String currentColor;

    private final ActivityResultLauncher<ColorSelectActivityBuilder> launcher =
            ColorSelectActivityBuilder.registerForActivityResult(this, result -> {
                if (result.resultCode == RESULT_OK) {
                    String color = result.color;
                    binding.btnSelectColor.setBackgroundColor(Color.parseColor(color));
                    Toast.makeText(this, "选中颜色: " + color, Toast.LENGTH_SHORT).show();
                    currentColor = color;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            currentColor = savedInstanceState.getString("currentColor");
        }

        initView();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentColor", currentColor);
    }

    private void initView() {
        binding.btnDetail.setOnClickListener(view -> {
            DetailActivityBuilder.builder(123456L, "100008")
                    .title("测试标题")
                    .start(this);
        });

        if (!TextUtils.isEmpty(currentColor)) {
            binding.btnSelectColor.setBackgroundColor(Color.parseColor(currentColor));
        }

        binding.btnSelectColor.setOnClickListener(view -> {
            launcher.launch(ColorSelectActivityBuilder.builder(currentColor));
//            ColorSelectActivityBuilder.builder(currentColor)
//                    .start(this, REQUEST_CODE_SELECT_COLOR);
        });

        binding.btnKotlinActivity.setOnClickListener(v -> {
            KotlinActivityBuilder.builder()
                    .start(this);
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        // 处理返回结果
//        if (requestCode == REQUEST_CODE_SELECT_COLOR) {
//            // if (data != null) {
//            //    String color = data.getStringExtra("color");
//            // }
//            ColorSelectActivityBuilder.Result result = ColorSelectActivityBuilder.obtainResult(resultCode, data);
//            String color = result.color;
//            // TODO do something...
//        }
//    }
}