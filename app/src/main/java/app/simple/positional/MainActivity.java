package app.simple.positional;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import app.simple.positional.ui.ViewModel;

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
    
        checkRunTimePermission();
    }
    
    public void checkRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                
                runApp();
                
            }
            else {
                requestPermissions(new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Application will not work without location permission", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runApp();
            }
            else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    finish();
                }
                //code for deny
            }
        }
    }
    
    private void runApp() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containers, new ViewModel(), "model")
                .commit();
    }
}