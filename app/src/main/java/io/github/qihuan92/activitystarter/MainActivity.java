package io.github.qihuan92.activitystarter;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        btnDetail = findViewById(R.id.btn_detail);

        btnDetail.setOnClickListener(view -> {
            DetailActivityBuilder.builder(123456L)
                    .title("测试标题")
                    .start(this);
        });
    }
}