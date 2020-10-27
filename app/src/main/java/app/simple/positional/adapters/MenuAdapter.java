package app.simple.positional.adapters;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import app.simple.positional.R;
import nl.psdcompany.duonavigationdrawer.views.DuoOptionView;

public class MenuAdapter extends BaseAdapter {
    private ArrayList <String> mOptions;
    private ArrayList <DuoOptionView> mOptionViews = new ArrayList <>();
    
    public MenuAdapter(ArrayList <String> options) {
        mOptions = options;
    }
    
    @Override
    public int getCount() {
        return mOptions.size();
    }
    
    @Override
    public Object getItem(int position) {
        return mOptions.get(position);
    }
    
    public void setViewSelected(int position, boolean selected) {
        // Looping through the options in the menu
        // Selecting the chosen option
        for (int i = 0; i < mOptionViews.size(); i++) {
            if (i == position) {
                mOptionViews.get(i).setSelected(selected);
            }
            else {
                mOptionViews.get(i).setSelected(!selected);
            }
        }
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String option = mOptions.get(position);
        
        // Using the DuoOptionView to easily recreate the demo
        final DuoOptionView optionView;
        if (convertView == null) {
            optionView = new DuoOptionView(parent.getContext());
        }
        else {
            optionView = (DuoOptionView) convertView;
        }
        
        TextView textView = optionView.getRootView().findViewById(R.id.duo_view_option_text);
        textView.setTextColor(Color.BLACK);
        
        ImageView imageView = optionView.getRootView().findViewById(R.id.duo_view_option_selector);
        imageView.setImageResource(R.drawable.ic_circle);
        
        // Using the DuoOptionView's default selectors
        optionView.bind(option, null, null);
        
        // Adding the views to an array list to handle view selection
        mOptionViews.add(optionView);
        
        return optionView;
    }
}
