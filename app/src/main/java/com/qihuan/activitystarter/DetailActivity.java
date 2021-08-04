package com.qihuan.activitystarter;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.qihuan.activitystarter.annotation.Arg;
import com.qihuan.activitystarter.annotation.Builder;

import java.util.Locale;

@Builder
public class DetailActivity extends AppCompatActivity {

    @Arg
    Long id;

    @Arg(required = false)
    String title;

    @Arg(required = false)
    char charVal;

    @Arg(required = false)
    byte byteVal;

    @Arg(required = false)
    short shortVal;

    @Arg(required = false)
    int intVal;

    @Arg(required = false)
    float floatVal;

    @Arg(required = false)
    double doubleVal;

    @Arg(required = false)
    boolean booleanVal;

    private Toolbar toolbar;
    private TextView tvId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        tvId = findViewById(R.id.tv_id);

        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        tvId.setText(String.format(Locale.getDefault(), "ID: %d", id));
    }
}