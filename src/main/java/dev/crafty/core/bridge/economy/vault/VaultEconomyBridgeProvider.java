package dev.crafty.core.bridge.economy.vault;

import dev.crafty.core.bridge.BridgeProvider;
import dev.crafty.core.bridge.economy.EconomyBridge;

/**
 * @since 1.0
 */
public class VaultEconomyBridgeProvider implements BridgeProvider<EconomyBridge> {
    @Override
    public boolean isAvailable() {
        return pluginEnabled("Vault");
    }

    @Override
    public EconomyBridge create() {
        return new VaultEconomyBridge();
    }

    @Override
    public Class<EconomyBridge> getBridgeClass() {
        return EconomyBridge.class;
    }
}
