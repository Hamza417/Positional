package app.simple.positional;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;

import app.simple.positional.ui.Compass;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getWindow().setFormat(PixelFormat.RGBA_8888);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        
        //getWindow().setStatusBarColor(AttributeColorKt.resolveAttrColor(getBaseContext(), R.attr.mainBackground));
        getWindow().setStatusBarColor(Color.parseColor("#f6f6f6"));
        getWindow().setNavigationBarColor(Color.parseColor("#f6f6f6"));
        
        //findViewById(R.id.containers).setPadding(0, StatusAndNavigationBarHeight.getStatusBarHeight(getResources()), 0, StatusAndNavigationBarHeight.getNavigationBarHeight(getResources()));
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containers, new Compass(), "model")
                .commit();
    }
}