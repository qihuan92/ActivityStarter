package io.github.qihuan92.activitystarter.kotlindemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.qihuan92.activitystarter.annotation.Builder
import io.github.qihuan92.activitystarter.kotlindemo.databinding.ActivityMainBinding

@Builder
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val detailLauncher = registerForDetailActivityResult {
        Toast.makeText(
            this,
            "返回结果: resultInfo1=${it.resultInfo1}, resultInfo2=${it.resultInfo2}",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnDetail.setOnClickListener {
            detailLauncher.launch(123456L, "user_789", "测试标题")
        }
        binding.btnDetail.setOnLongClickListener {
            startDetailActivity(123456L, "user_789", "测试标题")
            true
        }
    }
}