# ActivityStarter

Activity 启动器，解决使用 `startActivity()` 传递多个参数使用繁琐问题。

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

   则会生成如下代码：

   ```java
   public final class DetailActivityBuilder {
     public static final String EXTRA_BOOLEAN_VAL = "booleanVal";
   
     public static final String EXTRA_BYTE_VAL = "byteVal";
   
     public static final String EXTRA_CHAR_VAL = "charVal";
   
     public static final String EXTRA_DOUBLE_VAL = "doubleVal";
   
     public static final String EXTRA_FLOAT_VAL = "floatVal";
   
     public static final String EXTRA_ID = "id";
   
     public static final String EXTRA_INT_VAL = "intVal";
   
     public static final String EXTRA_SHORT_VAL = "shortVal";
   
     public static final String EXTRA_TITLE = "title";
   
     private boolean booleanVal;
   
     private byte byteVal;
   
     private char charVal;
   
     private double doubleVal;
   
     private float floatVal;
   
     private Long id;
   
     private int intVal;
   
     private short shortVal;
   
     private String title;
   
     public DetailActivityBuilder booleanVal(boolean booleanVal) {
       this.booleanVal = booleanVal;
       return this;
     }
   
     public DetailActivityBuilder byteVal(byte byteVal) {
       this.byteVal = byteVal;
       return this;
     }
   
     public DetailActivityBuilder charVal(char charVal) {
       this.charVal = charVal;
       return this;
     }
   
     public DetailActivityBuilder doubleVal(double doubleVal) {
       this.doubleVal = doubleVal;
       return this;
     }
   
     public DetailActivityBuilder floatVal(float floatVal) {
       this.floatVal = floatVal;
       return this;
     }
   
     public DetailActivityBuilder intVal(int intVal) {
       this.intVal = intVal;
       return this;
     }
   
     public DetailActivityBuilder shortVal(short shortVal) {
       this.shortVal = shortVal;
       return this;
     }
   
     public DetailActivityBuilder title(String title) {
       this.title = title;
       return this;
     }
   
     public static DetailActivityBuilder builder(Long id) {
       DetailActivityBuilder builder = new DetailActivityBuilder();
       builder.id = id;
       return builder;
     }
   
     public Intent getIntent(Context context) {
       Intent intent = new Intent(context, DetailActivity.class);
       intent.putExtra(EXTRA_BOOLEAN_VAL, booleanVal);
       intent.putExtra(EXTRA_BYTE_VAL, byteVal);
       intent.putExtra(EXTRA_CHAR_VAL, charVal);
       intent.putExtra(EXTRA_DOUBLE_VAL, doubleVal);
       intent.putExtra(EXTRA_FLOAT_VAL, floatVal);
       intent.putExtra(EXTRA_ID, id);
       intent.putExtra(EXTRA_INT_VAL, intVal);
       intent.putExtra(EXTRA_SHORT_VAL, shortVal);
       intent.putExtra(EXTRA_TITLE, title);
       return intent;
     }
   
     public static void inject(Activity instance, Bundle savedInstanceState) {
       if(instance instanceof DetailActivity) {
         DetailActivity typedInstance = (DetailActivity) instance;
         if(savedInstanceState != null) {
           typedInstance.booleanVal = BundleUtils.<Boolean>get(savedInstanceState, "booleanVal", false);
           typedInstance.byteVal = BundleUtils.<Byte>get(savedInstanceState, "byteVal", (byte) 0);
           typedInstance.charVal = BundleUtils.<Character>get(savedInstanceState, "charVal", '0');
           typedInstance.doubleVal = BundleUtils.<Double>get(savedInstanceState, "doubleVal", 0.0);
           typedInstance.floatVal = BundleUtils.<Float>get(savedInstanceState, "floatVal", 0.000000f);
           typedInstance.id = BundleUtils.<Long>get(savedInstanceState, "id", null);
           typedInstance.intVal = BundleUtils.<Integer>get(savedInstanceState, "intVal", 0);
           typedInstance.shortVal = BundleUtils.<Short>get(savedInstanceState, "shortVal", (short) 0);
           typedInstance.title = BundleUtils.<String>get(savedInstanceState, "title", "");
         }
       }
     }
   
     public static void saveState(Activity instance, Bundle outState) {
       if(instance instanceof DetailActivity) {
         DetailActivity typedInstance = (DetailActivity) instance;
         Intent intent = new Intent();
         intent.putExtra("booleanVal", typedInstance.booleanVal);
         intent.putExtra("byteVal", typedInstance.byteVal);
         intent.putExtra("charVal", typedInstance.charVal);
         intent.putExtra("doubleVal", typedInstance.doubleVal);
         intent.putExtra("floatVal", typedInstance.floatVal);
         intent.putExtra("id", typedInstance.id);
         intent.putExtra("intVal", typedInstance.intVal);
         intent.putExtra("shortVal", typedInstance.shortVal);
         intent.putExtra("title", typedInstance.title);
         outState.putAll(intent.getExtras());
       }
     }
   
     public static void processNewIntent(DetailActivity activity, Intent intent) {
       processNewIntent(activity, intent, true);
     }
   
     public static void processNewIntent(DetailActivity activity, Intent intent,
         Boolean updateIntent) {
       if(updateIntent) {
         activity.setIntent(intent);
       }
       if(intent != null) {
         inject(activity, intent.getExtras());
       }
     }
   
     public void start(Context context) {
       Intent intent = getIntent(context);
       if(!(context instanceof Activity)) {
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       }
       context.startActivity(intent);
     }
   
     public void start(Context context, Bundle options) {
       Intent intent = getIntent(context);
       if(!(context instanceof Activity)) {
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       }
       context.startActivity(intent, options);
     }
   
     public void startForResult(Activity activity, int requestCode) {
       Intent intent = getIntent(activity);
       activity.startActivityForResult(intent, requestCode);
     }
   
     public void startForResult(Activity activity, int requestCode, Bundle options) {
       Intent intent = getIntent(activity);
       activity.startActivityForResult(intent, requestCode, options);
     }
   }
   ```

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

## 致谢

- 感谢 [@bennyhuo](https://github.com/bennyhuo) 老师，此项目为学习之后的练习项目

