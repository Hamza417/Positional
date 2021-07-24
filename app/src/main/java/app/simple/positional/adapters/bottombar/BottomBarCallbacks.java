package app.simple.positional.adapters.bottombar;

import org.jetbrains.annotations.NotNull;

public interface BottomBarCallbacks {
    void onItemClicked(int position, @NotNull String name);
}
