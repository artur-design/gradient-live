

package com.example.gradient;

import android.content.Intent;                     // запуск SettingsActivity
import android.view.GestureDetector;               // обработка двойного тапа
import android.view.MotionEvent;                  // событие касания
import android.view.GestureDetector.SimpleOnGestureListener;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.service.wallpaper.WallpaperService;
import android.view.Surface;
import android.os.Handler;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.Random;
import android.view.SurfaceHolder;
import android.content.Context;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GradientWallpaperService extends WallpaperService {
    @Override
	public Engine onCreateEngine() {
		return new GradientWallpaperEngine(this);
	}
	
    public class GradientWallpaperEngine extends WallpaperService.Engine {
									private final GestureDetector gestureDetector;
        private Paint paint = new Paint();
private Surface currentSurface;

        private Context context;
private boolean isVisible = true;
        private int color1, color2, targetColor1, targetColor2, targetColor3, frameCount, screenRefreshRate;
        private int h, s, v, r, g, b;
        private String sr, sg, sb, sh, ss, sv, type, md;
        private boolean black = false;
        private float transitionProgress = 1.0f;
        private final Handler handler = new Handler();
        private final Random random = new Random();
        private float transitionStep, gradientLengthCoefficient;
		private long transitionDuration;
		
        public GradientWallpaperEngine(Context context) {
            this.context = context.getApplicationContext(); 
            updatePreferences();

setTouchEventsEnabled(true);

        // 2️⃣ создаём GestureDetector
        gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        openSettings();
                        return true;            // событие обработано
                    }
                });
        }


    /** Запуск SettingsActivity */
    private void openSettings() {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /** Передаём все события в GestureDetector */
    @Override
    public void onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
    }

        public void updatePreferences() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			frameCount = Integer.parseInt(prefs.getString("frameCount", "100"));
			transitionStep = 1.00000f / frameCount;
			md = prefs.getString("movementDirection", "↘");
			screenRefreshRate = Integer.parseInt(prefs.getString("screenRefreshRate", "60"));
			gradientLengthCoefficient = Float.parseFloat(prefs.getString("gradientLengthCoefficient", "1.0"));
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
			prefs.registerOnSharedPreferenceChangeListener(prefListener);
        }

        private final SharedPreferences.OnSharedPreferenceChangeListener prefListener =
        new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                updatePreferences();
            }
        };

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
    		super.onSurfaceCreated(holder);
    		updatePreferences();
currentSurface = holder.getSurface();
    		startPreview(currentSurface); //передаем Surface
		}

  @Override
  public void onVisibilityChanged(boolean visible) {
          super.onVisibilityChanged(visible);
        if (visible) {
                // возобновить анимацию / отрисовку
if (currentSurface != null) {
                handler.post(runnable(currentSurface));
            }
            } else {
                // приостановить, экономим батарею
handler.removeCallbacksAndMessages(null);
            }
        }

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
    		super.onSurfaceDestroyed(holder);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    		prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
    		stopPreview(); // Останавливаем предварительный просмотр
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    		super.onSurfaceChanged(holder, format, width, height);
currentSurface = holder.getSurface();
    		startPreview(currentSurface);
		}

		public void startPreview(Surface surface) {
    		stopPreview(); // Останавливаем предыдущий предварительный просмотр, если он есть
			if (black) drawWallpaper(surface);
			else handler.post(runnable(surface)); // Запускаем новый предварительный просмотр
		}

        public void stopPreview() {
            handler.removeCallbacksAndMessages(null);
        }

        private Runnable runnable(final Surface surface) {
            return new Runnable() {
                @Override
                public void run() {
if (!isVisible) return;
					transitionProgress += transitionStep;
                    if (transitionProgress >= 1f) {
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
        	if (surface == null) return;
        	Canvas canvas = null;
        	try {
            	canvas = surface.lockCanvas(null);
            	if (canvas == null) return;
				if (black) {
					canvas.drawColor(Color.BLACK);
					return;
				}
                int width = (int) (canvas.getWidth() * gradientLengthCoefficient);
				int height = (int) (canvas.getHeight() * gradientLengthCoefficient);
				if (width == 0 || height == 0) return;
				int[] colors = {color2, targetColor2, color1};
				float[] positions = {0.0f, transitionProgress, 1.0f};
                LinearGradient gradient;
                	switch (md) {
                    	case "↖": gradient = new LinearGradient(width, height, 0, 0, colors, positions, Shader.TileMode.CLAMP); break;
                        case "↑": gradient = new LinearGradient(0, height, 0, 0, colors, positions, Shader.TileMode.CLAMP); break;
                        case "↗": gradient = new LinearGradient(0, height, width, 0, colors, positions, Shader.TileMode.CLAMP); break;
                        case "←": gradient = new LinearGradient(width, 0, 0, 0, colors, positions, Shader.TileMode.CLAMP); break;
                        case "→": gradient = new LinearGradient(0, 0, width, 0, colors, positions, Shader.TileMode.CLAMP); break;
                        case "↙": gradient = new LinearGradient(width, 0, 0, height, colors, positions, Shader.TileMode.CLAMP); break;
                        case "↓": gradient = new LinearGradient(0, 0, 0, height, colors, positions, Shader.TileMode.CLAMP); break;
                        case "↘": gradient = new LinearGradient(0, 0, width, height, colors, positions, Shader.TileMode.CLAMP); break;
                        default:
						gradient = null;
                        canvas.drawColor(color2);
                        return;
                    }
                    paint.setShader(gradient);
                    canvas.drawRect(0, 0, width, height, paint);
Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);         // цвет текста
        textPaint.setTextSize(48f);              // размер шрифта
        textPaint.setAntiAlias(true);

        // 3️⃣ Нарисуйте строку «r = 123» в левом‑верхнем углу
        String text = color2 + " " + targetColor2 + " " + color1;
        canvas.drawText(text, 20f, 60f, textPaint);   // (x, y) – позиция текста
            	} finally {
            		if (canvas != null) surface.unlockCanvasAndPost(canvas);
        		}
			}

  			private int blendColors(int color1, int color2, float ratio) {
    			int r = (int) (Color.red(color1) * (1 - ratio) + Color.red(color2) * ratio);
    			int g = (int) (Color.green(color1) * (1 - ratio) + Color.green(color2) * ratio);
    			int b = (int) (Color.blue(color1) * (1 - ratio) + Color.blue(color2) * ratio);
    			return Color.rgb(r, g, b);
  			}

			private int evaluateExpression(String expression) {
    			try {
        			expression = replaceRandomCalls(expression); // Заменяем вызовы random() на случайные значения
        			Expression exp = new ExpressionBuilder(expression)
                	.variables("r", "g", "b", "h", "s", "v")
                	.build()
                	.setVariable("r", r)
                	.setVariable("g", g)
                	.setVariable("b", b)
                	.setVariable("h", h)
                	.setVariable("s", s)
                	.setVariable("v", v);
				
        			double result = exp.evaluate(); // Получаем результат как double
        			int intResult = (int) result % 256; // Приводим результат к int с учетом переполнения
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
      				} else if (maxStr != null && !maxStr.isEmpty()) max = getValue(maxStr);
        			if (min > max) throw new IllegalArgumentException("min > max"); // Проверка на корректность диапазона
        			matcher.appendReplacement(result, String.valueOf(min + random.nextInt(max - min + 1)));
    			}
    			matcher.appendTail(result);
    			return result.toString();
			}

			private int getValue(String input) {
    			switch (input) { // Проверяем, является ли input переменной или числом
        			case "r": return r;
        			case "g": return g;
        			case "b": return b;
        			case "h": return h;
        			case "s": return s;
        			case "v": return v;
        			default: return Integer.parseInt(input); // Если это число, преобразуем его
    			}
			}
    	}
	}
