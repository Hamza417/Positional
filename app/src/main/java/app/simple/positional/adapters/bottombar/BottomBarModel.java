package app.simple.positional.adapters.bottombar;

public class BottomBarModel {
    private int icon;
    private String tag;
    private String name;

    public BottomBarModel(int icon, String tag, String name) {
        this.icon = icon;
        this.tag = tag;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public String getTag() {
        return tag;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
