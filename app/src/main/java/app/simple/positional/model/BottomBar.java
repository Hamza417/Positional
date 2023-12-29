package app.simple.positional.model;

public class BottomBar {
    private int icon;
    private String tag;
    private String name;
    private int color;

    public BottomBar(int icon, String tag, String name, int color) {
        this.icon = icon;
        this.tag = tag;
        this.name = name;
        this.color = color;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTag() {
        return tag;
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
