package app.simple.positional.decorations.popup;

import org.jetbrains.annotations.NotNull;

public interface PopupMenuCallback {
    void onMenuItemClicked(@NotNull String source);
}
