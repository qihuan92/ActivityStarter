package io.github.qihuan92.activitystarter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

import io.github.qihuan92.activitystarter.R;
import io.github.qihuan92.activitystarter.annotation.Builder;
import io.github.qihuan92.activitystarter.annotation.Extra;

@Builder
public class DetailActivity extends AppCompatActivity {

    @Extra
    long id;

    @Extra(description = "用户ID")
    String userId;

    @Extra(required = false, description = "详情内容")
    String title;

    private Toolbar toolbar;
    private TextView tvId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        bindView();
        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        DetailActivityBuilder.processNewIntent(this, intent);
    }

    private void bindView() {
        toolbar = findViewById(R.id.toolbar);
        tvId = findViewById(R.id.tv_id);
    }

    private void initView() {
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        tvId.setText(String.format(Locale.getDefault(), "ID: %d", id));
    }
}