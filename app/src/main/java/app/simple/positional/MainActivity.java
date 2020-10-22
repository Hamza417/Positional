package app.simple.positional;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import app.simple.positional.ui.ViewModel;

public class MainActivity extends AppCompatActivity {
    
    public static int DEFAULT_PERMISSION_REQUEST_CODE = 123;
    
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
        
        runApp();
        
        checkRunTimePermission();
    }
    
    public void checkRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        
                requestPermissions(new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }
        }
    }
    
    private void runApp() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containers, new ViewModel(), "model")
                .commit();
    }
}