package app.simple.positional.decorations.bottombar;

public class BottomBarModel {
    private int icon;
    private String name;
    
    public BottomBarModel(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }
    
    public int getIcon() {
        return icon;
    }
    
    public String getName() {
        return name;
    }
    
    public void setIcon(int icon) {
        this.icon = icon;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
