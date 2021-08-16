package io.github.qihuan92.activitystarter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import io.github.qihuan92.activitystarter.R;

public class MainActivity extends AppCompatActivity {

    private Button btnDetail;
    private Button btnSelectColor;

    private ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindView();
        initView();
    }

    private void bindView() {
        btnDetail = findViewById(R.id.btn_detail);
        btnSelectColor = findViewById(R.id.btn_select_color);
    }

    private void initView() {
        btnDetail.setOnClickListener(view -> {
            DetailActivityBuilder.builder(123456L)
                    .title("测试标题")
                    .start(this);
        });

        btnSelectColor.setOnClickListener(view -> {

        });
    }
}