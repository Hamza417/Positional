package app.simple.positional.adapters;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ModelAdapter extends FragmentPagerAdapter {
    
    private final List <Fragment> fragmentList = new ArrayList <>();
    private final List <String> titleList = new ArrayList <>();
    
    public ModelAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }
    
    public ModelAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }
    
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }
    
    @Override
    public int getCount() {
        return fragmentList.size();
    }
    
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
    
    public void addFrag(Fragment fragment) {
        fragmentList.add(fragment);
        titleList.add("");
    }
    
    public void addFrag(Fragment fragment, String title) {
        fragmentList.add(fragment);
        titleList.add(title);
    }
}
