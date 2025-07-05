package com.example.gradient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.app.WallpaperManager;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.view.WindowManager;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.graphics.Color;
import android.os.Build;

public class MainActivity extends Activity {
    private GradientWallpaperService.GradientWallpaperEngine previewEngine;
    private TextureView textureView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper); 
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    getWindow().setNavigationBarColor(Color.TRANSPARENT);

}

textureView = findViewById(R.id.wallpaper_preview);

        // Запускаем сервис обоев
        Intent intent = new Intent(this, GradientWallpaperService.class);
        startService(intent);

        Button buttonSettings = findViewById(R.id.button_settings);
        Button buttonApply = findViewById(R.id.button_apply);

        // Настройка предварительного просмотра
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWallpaper();
            }
        });

        // Запускаем предварительный просмотр обоев
        startPreview();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN

);
        }
    }

    private void setWallpaper() {

        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, GradientWallpaperService.class));
        startActivity(intent);
    }
private void startPreview() {
    previewEngine = new GradientWallpaperService().new GradientWallpaperEngine(MainActivity.this);
    textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Surface s = new Surface(surface); // Создаем Surface из SurfaceTexture
            previewEngine.startPreview(s); // Передаем Surface
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            previewEngine.stopPreview();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    });
}

@Override
protected void onRestart() {
super.onRestart();
Intent i = new Intent( this , this.getClass() );
finish();
this.startActivity(i);
}

@Override
    protected void onDestroy() {
        super.onDestroy();
        previewEngine.stopPreview();
    }
}
