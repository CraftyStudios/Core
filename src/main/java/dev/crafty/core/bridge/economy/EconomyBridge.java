package dev.crafty.core.bridge.economy;

import dev.crafty.core.bridge.Bridge;

import java.util.UUID;

/**
 * Bridge for economy plugins.
 * @since 1.0.0
 */
public interface EconomyBridge extends Bridge {
    /**
     * Retrieves the balance of the specified player.
     *
     * @param player the UUID of the player whose balance is to be retrieved
     * @return the balance of the player
     */
    double getBalance(UUID player);

    /**
     * Withdraws a specified amount from the player's balance.
     *
     * @param player the UUID of the player
     * @param amount the amount to withdraw
     */
    void withdraw(UUID player, double amount);

    /**
     * Deposits a specified amount to the player's balance.
     *
     * @param player the UUID of the player
     * @param amount the amount to deposit
     */
    void deposit(UUID player, double amount);

    /**
     * {@inheritDoc}
     */
    @Override
    default String bridgeName() {
        return "Economy";
    }
}