package io.github.qihuan92.activitystarter.activity;

import android.content.Context;
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

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_USER_ID = "myUserId";
    public static final String EXTRA_TITLE = "title";

    @Extra
    long id;

    @Extra(value = EXTRA_USER_ID, description = "用户ID")
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

    /**
     * Start DetailActivity.
     *
     * @param context context
     * @param id      id
     * @param userId  userID
     * @param title   detail content
     */
    public static void start(Context context, long id, String userId, String title) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_TITLE, title);
        context.startActivity(intent);
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