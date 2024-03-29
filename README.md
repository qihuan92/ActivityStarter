# ActivityStarter

![GitHub](https://img.shields.io/github/license/qihuan92/ActivityStarter) ![Maven Central](https://img.shields.io/maven-central/v/io.github.qihuan92.activitystarter/activitystarter-runtime)

Activity 启动器，解决使用 `startActivity()` 传递多个参数使用繁琐问题。

## 接入

```groovy
implementation "io.github.qihuan92.activitystarter:activitystarter-runtime:$latest_version"
// java
annotationProcessor "io.github.qihuan92.activitystarter:activitystarter-compiler:$latest_version"
// kotlin
kapt "io.github.qihuan92.activitystarter:activitystarter-compiler:$latest_version"
```

## 使用

1. 在自定义的 `Application` 的 `onCreate()` 中调用 `ActivityStarter.init(this)`，示例：

   ```java
   public class App extends Application {
   
       @Override
       public void onCreate() {
           super.onCreate();
           ActivityStarter.init(this);
       }
   }
   ```

2. 在目标 Activity 上打上 `@Builder` 注解，在需要传递的参数上使用 `@Extra` 注解，则会生成目标 ActivityBuilder 的类，示例：

   ```java
   @Builder
   public class DetailActivity extends AppCompatActivity {
   
       @Extra
       Long id;
   
       @Extra(required = false)
       String title;
   
       @Extra(required = false)
       char charVal;
   
       @Extra(required = false)
       byte byteVal;
   
       @Extra(required = false)
       short shortVal;
   
       @Extra(required = false)
       int intVal;
   
       @Extra(required = false)
       float floatVal;
   
       @Extra(required = false)
       double doubleVal;
   
       @Extra(required = false)
       boolean booleanVal;
     
       ....
   }
   ```

   则会生成对应的 `DetailActivityBuilder` 类

   注：`@Extra` 注解的参数：
   
   - value：为传递的键，为空则使用字段名
   - required：是否必传，默认为是
   - *Vlaue：可指定各类型默认值
   
3. 调用

   在需要启动目标 Activity 的地方，调用对应生成的 Builder 中的 `start() ` 方法，必传的字段为 `builder()` 的参数，非必传的字段则生成对应的方法传递，示例：

   ```java
   DetailActivityBuilder.builder(123456L)
           .title("测试标题")
           .start(this);
   ```

4. 关于 NewIntent

   如果需要处理在 `onNewIntent()` 中获取参数的这种情况，可通过调用对应 Builder 的 `processNewIntent()` 方法，示例：

   ```java
   @Override
   protected void onNewIntent(Intent intent) {
       super.onNewIntent(intent);
       DetailActivityBuilder.processNewIntent(this, intent);
   }
   ```
   
5. 支持 Result API

   在 `@Builder` 注解中传入需要返回的字段：

   ```java
   @Builder(resultFields = @ResultField(name = "color", type = String.class))
   ```

   则会生成相应的 Result 实体、 XXXActivityResultContract，以及 `obtainResult()` 方法，同时 `start()` 函数支持传入 `launcher` 参数，示例：

   ```java
   // 在 onStart() 之前注册
    private final ActivityResultLauncher<ColorSelectActivityBuilder> launcher =
            ColorSelectActivityBuilder.registerForActivityResult(this, result -> {
                if (result.resultCode == RESULT_OK) {
                    String color = result.color;
                    binding.btnSelectColor.setBackgroundColor(Color.parseColor(color));
                    Toast.makeText(this, "选中颜色: " + color, Toast.LENGTH_SHORT).show();
                    currentColor = color;
                }
            });
   
   btnSelectColor.setOnClickListener(view -> {
       launcher.launch(ColorSelectActivityBuilder.builder(currentColor));
   });
   ```

6. 支持生成 Kotlin 扩展函数
   ```kotlin
   private val launcher = registerForColorSelectActivityResult {
       if (it.resultCode == RESULT_OK) {
           currentColor = it.color
           Toast.makeText(this, it.color, Toast.LENGTH_SHORT).show()
       }
   }

   binding.btnSelectColor.setOnClickListener {
      launcher.launch(currentColor)
   }
   
   binding.btnSelectColor.setOnClickListener {
      startColorSelectActivity(currentColor)
   }
   ```
   

## 许可

> [Apache License 2.0](https://github.com/qihuan92/ActivityStarter/blob/master/LICENSE)
