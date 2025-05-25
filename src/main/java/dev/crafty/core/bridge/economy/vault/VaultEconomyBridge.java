package dev.crafty.core.bridge.economy.vault;

import dev.crafty.core.bridge.economy.EconomyBridge;

import java.util.UUID;

/**
 * @since 1.0.0
 */
public class VaultEconomyBridge implements EconomyBridge {
    @Override
    public double getBalance(UUID player) {
        return 0;
    }

    @Override
    public void withdraw(UUID player, double amount) {

    }

    @Override
    public void deposit(UUID player, double amount) {

    }

    @Override
    public String getName() {
        return "Vault";
    }
}
