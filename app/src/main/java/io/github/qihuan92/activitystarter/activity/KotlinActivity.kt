package io.github.qihuan92.activitystarter.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.qihuan92.activitystarter.annotation.Builder
import io.github.qihuan92.activitystarter.annotation.ResultField
import io.github.qihuan92.activitystarter.databinding.ActivityKotlinBinding

/**
 * @author qihuan
 * @date 2022/8/24
 */
@Builder(
    resultFields = [
        ResultField(name = "testResult", type = String::class)
    ]
)
class KotlinActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityKotlinBinding.inflate(layoutInflater)
    }

    private val launcher = registerForColorSelectActivityResult {
        if (it.resultCode == RESULT_OK) {
            currentColor = it.color
            Toast.makeText(this, it.color, Toast.LENGTH_SHORT).show()
        }
    }

    private var currentColor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSelectColor.setOnClickListener {
            // startDetailActivity(123456L, "999999", "测试标题")
            launcher.launch(currentColor)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish("success")
        }
    }
}