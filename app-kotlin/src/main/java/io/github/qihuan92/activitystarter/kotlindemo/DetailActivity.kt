package io.github.qihuan92.activitystarter.kotlindemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.qihuan92.activitystarter.annotation.Builder
import io.github.qihuan92.activitystarter.annotation.Extra
import io.github.qihuan92.activitystarter.annotation.ResultField
import io.github.qihuan92.activitystarter.kotlindemo.databinding.ActivityDetailBinding
import java.util.*

@Builder(
    resultFields = [
        ResultField(name = "resultInfo1", type = Int::class),
        ResultField(name = "resultInfo2", type = String::class),
    ]
)
class DetailActivity : AppCompatActivity() {

    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }

    @Extra
    var id: Long = 0

    @Extra(value = EXTRA_USER_ID, description = "用户ID")
    var userId: String? = null

    @Extra(required = false, description = "详情内容")
    var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        DetailActivityBuilder.processNewIntent(this, intent)
    }

    private fun initView() {
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener {
            finish(123123, "测试返回结果")
        }
        binding.tvId.text = String.format(Locale.getDefault(), "ID: %d", id)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_USER_ID = "myUserId"
        const val EXTRA_TITLE = "title"

        /**
         * Start DetailActivity.
         *
         * @param context context
         * @param id      id
         * @param userId  userID
         * @param title   detail content
         */
        fun start(context: Context, id: Long, userId: String?, title: String?) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(EXTRA_ID, id)
            intent.putExtra(EXTRA_USER_ID, userId)
            intent.putExtra(EXTRA_TITLE, title)
            context.startActivity(intent)
        }
    }
}