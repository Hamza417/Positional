package app.simple.positional.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.simple.positional.R;
import app.simple.positional.adapters.ModelAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class ViewModel extends Fragment {
    private ViewPager model;
    private ModelAdapter modelAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_view_model, container, false);
        model = v.findViewById(R.id.view_model);
        return v;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        modelAdapter = new ModelAdapter(getChildFragmentManager());
        modelAdapter.addFrag(new Compass(), "Compass");
        // modelAdapter.addFrag(new Level(), "Level");
        
        model.setAdapter(modelAdapter);
        model.setOffscreenPageLimit(0);
        
        model.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            
            }
            
            @Override
            public void onPageSelected(int position) {
            }
            
            @Override
            public void onPageScrollStateChanged(int state) {
            
            }
        });
    }
}
