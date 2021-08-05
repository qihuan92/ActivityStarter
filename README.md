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

## 致谢

- 感谢 [@bennyhuo](https://github.com/bennyhuo) 老师，此项目为学习之后的练习项目

