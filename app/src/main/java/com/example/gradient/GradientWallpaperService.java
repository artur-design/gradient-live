

package com.example.gradient;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.service.wallpaper.WallpaperService;
import android.view.Surface;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.Random;
import android.view.SurfaceHolder;
import android.content.Intent;
import android.content.Context;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GradientWallpaperService extends WallpaperService {

    private GradientWallpaperEngine engine;
    private long transitionDuration = 50;
private int frameCount = 100; // Новая переменная для хранения количества кадров
private int screenRefreshRate = 16;

    @Override
    public Engine onCreateEngine() {

       engine = new GradientWallpaperEngine(this);
        return engine;
    }

    public class GradientWallpaperEngine extends WallpaperService.Engine {
        private Paint paint = new Paint();
private Context context;
        private int color1, color2, targetColor1, targetColor2, targetColor3;
int h, s, v, r, g, b;
private String sr, sg, sb, sh, ss, sv, type;
private boolean black = false;

        private float transitionProgress = 1.00000f;
        private final Handler handler = new Handler();
        private final Random random = new Random();
        private float transitionStep;
private String md;
private float gradientLengthCoefficient = 1.00f;



 
        public GradientWallpaperEngine(Context context) {
        this.context = context.getApplicationContext(); 
updatePreferences();

    }

public void updatePreferences() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    loadprefs(prefs); // Загружаем значения из SharedPreferences
}

public void loadprefs(SharedPreferences prefs) {
String frameCountString = prefs.getString("frameCount", "100");
frameCount = Integer.parseInt(frameCountString);
transitionStep = 1.00000f / frameCount;
 md = prefs.getString("movementDirection", "↘");

String RefreshRateString = prefs.getString("screenRefreshRate", "60");
screenRefreshRate = Integer.parseInt(RefreshRateString);
String savedCoefficient = prefs.getString("gradientLengthCoefficient", "1.0");
gradientLengthCoefficient = Float.parseFloat(savedCoefficient);
sr = prefs.getString("myKey" + "_r", "random()");
sg = prefs.getString("myKey" + "_g", "random()");
sb = prefs.getString("myKey" + "_b", "random()");
sh = prefs.getString("myKey" + "_h", "random()");
ss = prefs.getString("myKey" + "_s", "random(150, 255)");
sv = prefs.getString("myKey" + "_v", "random(150, 255)");
type = prefs.getString("myKey" + "_type", "HSV");
black = prefs.getBoolean("black", false);
            transitionDuration = 1000 / screenRefreshRate; // Устанавливаем задержку в миллисекундах 
color1 = Color.BLACK;
color2 = Color.BLACK;

}


@Override
public void onSurfaceCreated(SurfaceHolder holder) {
    super.onSurfaceCreated(holder);
    updatePreferences();
    startPreview(holder.getSurface()); // Передаем Surface
}

@Override
public void onSurfaceDestroyed(SurfaceHolder holder) {
    super.onSurfaceDestroyed(holder);
    stopPreview(); // Останавливаем предварительный просмотр
}

@Override
public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    super.onSurfaceChanged(holder, format, width, height);
    startPreview(holder.getSurface());
}

public void startPreview(Surface surface) {
    stopPreview(); // Останавливаем предыдущий предварительный просмотр, если он есть
if (black) {

drawWallpaper(surface);
} else {
    handler.post(runnable(surface)); // Запускаем новый предварительный просмотр
}
}

        public void stopPreview() {
            handler.removeCallbacksAndMessages(null);
        }

        private Runnable runnable(final Surface surface) {
            return new Runnable() {
                @Override
                public void run() {
transitionProgress += transitionStep;
                    if (transitionProgress >= 1.00000) {

                        targetColor1 = targetColor2;
                        targetColor2 = targetColor3;
if (type.equals("RGB")) {
r = evaluateExpression(sr);
g = evaluateExpression(sg);
b = evaluateExpression(sb);
targetColor3 = Color.rgb(r, g, b);
} else {
h = evaluateExpression(sh);
s = evaluateExpression(ss);
v = evaluateExpression(sv);
targetColor3 = Color.HSVToColor(new float[] {h, (float) s / 255.0f, (float) v / 255.0f});
}
                        transitionProgress = 0;
                    }
                    color1 = blendColors(targetColor1, targetColor2, transitionProgress);
                    color2 = blendColors(targetColor2, targetColor3, transitionProgress);
                    drawWallpaper(surface);
                    handler.postDelayed(this, transitionDuration);
                    
                }
            };
        }

       private void drawWallpaper(Surface surface) {
    Canvas canvas = null;
    if (surface != null) {
        try {
            canvas = surface.lockCanvas(null);
            if (canvas != null) {
canvas.drawColor(Color.BLACK);
                int width = (int) (canvas.getWidth() * gradientLengthCoefficient);
int height = (int) (canvas.getHeight() * gradientLengthCoefficient);
int[] colors = {color2, targetColor2, color1};
float[] positions = {0.0f, transitionProgress, 1.0f};
                if (width > 0 && height > 0) {
                    LinearGradient gradient;
                    switch (md) {
                        case "↖": // Вверх и влево
                            gradient = new LinearGradient(width, height, 0, 0, colors, positions, Shader.TileMode.CLAMP);
                            break;
                        case "↑": // Вверх
                            gradient = new LinearGradient(0, height, 0, 0, colors, positions, Shader.TileMode.CLAMP);
                            break;
                        case "↗": // Вверх и вправо
gradient = new LinearGradient(0, height, width, 0, colors, positions, Shader.TileMode.CLAMP);
                            break;
                        case "←": // Влево
                            gradient = new LinearGradient(width, 0, 0, 0, colors, positions, Shader.TileMode.CLAMP);
                            break;
                        case "→": // Вправо
                            gradient = new LinearGradient(0, 0, width, 0, colors, positions, Shader.TileMode.CLAMP);
                            break;
                        case "↙": // Вниз и влево
                            gradient = new LinearGradient(width, 0, 0, height, colors, positions, Shader.TileMode.CLAMP);
                            break;
                        case "↓": // Вниз
                            gradient = new LinearGradient(0, 0, 0, height, colors, positions, Shader.TileMode.CLAMP);
                            break;
                        case "↘": // Вниз и вправо
gradient = new LinearGradient(0, 0, width, height, colors, positions, Shader.TileMode.CLAMP);
                            
                            break;
                        default:
gradient = null;
                            paint.setColor(color2);
canvas.drawRect(0, 0, width, height, paint);
                            break;
                    }

                    if (gradient != null) {
                        paint.setShader(gradient);
                        canvas.drawRect(0, 0, width, height, paint);
                    }
                }
            }
        } finally {
            if (canvas != null) {
                surface.unlockCanvasAndPost(canvas);
            }
        }
    }
}


  private int blendColors(int color1, int color2, float ratio) {
    int r = (int) (Color.red(color1) * (1 - ratio) + Color.red(color2) * ratio);
    int g = (int) (Color.green(color1) * (1 - ratio) + Color.green(color2) * ratio);
    int b = (int) (Color.blue(color1) * (1 - ratio) + Color.blue(color2) * ratio);
    return Color.rgb(r, g, b);
  }


  private int getRandomColor() {
    return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }


private int evaluateExpression(String expression) {
    try {
        // Заменяем вызовы random() на случайные значения
        expression = replaceRandomCalls(expression);

        Expression exp = new ExpressionBuilder(expression)
                .variables("r", "g", "b", "h", "s", "v")
                .build()
                .setVariable("r", r)
                .setVariable("g", g)
                .setVariable("b", b)
                .setVariable("h", h)
                .setVariable("s", s)
                .setVariable("v", v);

        // Получаем результат как double
        double result = exp.evaluate();
        // Приводим результат к int с учетом переполнения
        int intResult = (int) result % 256;
        return Math.max(intResult, 0); // Возвращаем 0, если результат отрицательный
    } catch (Exception e) {
        e.printStackTrace();
        return 0; 
    }
}


    private String replaceRandomCalls(String expression) {
    Pattern pattern = Pattern.compile("random\\s*\\(\\s*((\\d+|[a-zA-Z_])\\s*,)?\\s*(\\d+|[a-zA-Z_])?\\s*\\)");
    Matcher matcher = pattern.matcher(expression);
    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      int min = 0;
      int max = 255;
      String minStr = matcher.group(2);
      String maxStr = matcher.group(3);
      if (minStr != null && !minStr.isEmpty()) {
        min = getValue(minStr);
        max = getValue(maxStr);
      } else if (maxStr != null && !maxStr.isEmpty()) {
        max = getValue(maxStr);
      }

        // Проверка на корректность диапазона
        if (min > max) {
            throw new IllegalArgumentException("Минимальное значение не может быть больше максимального.");
        }

        String randomValue = String.valueOf(getRandomValue(min, max));
        matcher.appendReplacement(result, randomValue);
    }
    matcher.appendTail(result);
    return result.toString();
}

private int getValue(String input) {
    // Проверяем, является ли input переменной или числом
    switch (input) {
        case "r":
            return r;
        case "g":
            return g;
        case "b":
            return b;
        case "h":
            return h;
        case "s":
            return s;
        case "v":
            return v;
        default:
            return Integer.parseInt(input); // Если это число, преобразуем его
    }
}

    private int getRandomValue(int min, int max) {
        return (int) (min + (Math.random() * (max - min + 1))); // Генерируем случайное значение в диапазоне
    }


    }
}