package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.platform.Platform;
import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.modules.regions.RegionModule;

public class SpigotRegionDetail implements StateDetail {
    @Override
    public String title() {
        return "Loaded Regions";
    }

    @Override
    public String value() {
        if (OpenAudioMcSpigot.getInstance().getRegionModule() == null) {
            return Platform.makeColor("RED") + "Feature Disabled";
        } else {

            RegionModule regionModule = OpenAudioMcSpigot.getInstance().getRegionModule();
            int toIgnore = regionModule.getRegionsWithoutWorld().size() * regionModule.getWorldCount();
            int totalWithoutLegacy = regionModule.getRegionCount() - regionModule.getRegionsWithoutWorld().size();
            return "Loaded Audio Regions: " + "\u00A73" + "" + totalWithoutLegacy + " " + "\u00A77" + "(" + regionModule.getWorldCount() + " worlds) " + "\u00A78" + "[" + toIgnore + " legacy regions]";
        }
    }
}
