package dev.crafty.core.bridge.placeholders.placeholderapi;

import dev.crafty.core.bridge.BridgeProvider;

public class PlaceholderApiBridgeProvider implements BridgeProvider<PlaceholderApiBridge> {
    @Override
    public boolean isAvailable() {
        return pluginEnabled("PlaceholderAPI");
    }

    @Override
    public PlaceholderApiBridge create() {
        return new PlaceholderApiBridge();
    }

    @Override
    public Class<PlaceholderApiBridge> getBridgeClass() {
        return PlaceholderApiBridge.class;
    }
}
