package app.simple.positional.dialogs.gps;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.positional.R;
import app.simple.positional.views.CustomBottomSheetDialog;
import app.simple.positional.views.Speedometer;

public class MovementExpansion extends CustomBottomSheetDialog {
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Speedometer speedometer;
    
    public static MovementExpansion newInstance() {
        Bundle args = new Bundle();
        MovementExpansion fragment = new MovementExpansion();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        super.onCreate(savedInstanceState);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expansion_dialog_movement, container, false);
        
        speedometer = view.findViewById(R.id.speedometer);
        handler.post(runnable);
        
        return view;
    }
    
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            speedometer.setSpeedValue(new Random().nextFloat() * (600F - 0F));
            handler.postDelayed(this, 3000L);
        }
    };
    
    @Override
    public void onDestroy() {
        speedometer.clear();
        super.onDestroy();
    }
}
