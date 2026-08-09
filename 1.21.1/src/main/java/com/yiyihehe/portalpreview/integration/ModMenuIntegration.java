package com.yiyihehe.portalpreview.integration;

import com.yiyihehe.portalpreview.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // 1.21.1: Cloth Config 15.x 的 AutoConfig.getConfigScreen(Class, Screen) 返回 Supplier<Screen>
        //（26.1 的 AutoConfigClient.getConfigScreen 在 1.21.1 不存在）
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }
}
