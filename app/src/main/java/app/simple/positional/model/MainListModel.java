package app.simple.positional.model;

public class MainListModel {
    String name;
    int icon;

    public MainListModel() {
    }

    public MainListModel(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
