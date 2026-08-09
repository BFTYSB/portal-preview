package com.yiyihehe.portalpreview.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "portal-preview")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public PortalDirection defaultDirection = PortalDirection.NORTH;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 50, max = 255)
    public int previewOpacity = 180;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public HudPosition hudPosition = HudPosition.TOP_LEFT;

    @ConfigEntry.Gui.Tooltip
    public boolean showHudInNether = true;

    public enum PortalDirection {
        NORTH, SOUTH, EAST, WEST
    }

    public enum HudPosition {
        TOP_LEFT("左上角"),
        TOP_RIGHT("右上角"),
        BOTTOM_LEFT("左下角"),
        BOTTOM_RIGHT("右下角"),
        RIGHT_CENTER("靠右居中");

        private final String displayName;
        HudPosition(String name) { this.displayName = name; }
        @Override public String toString() { return displayName; }
    }
}
