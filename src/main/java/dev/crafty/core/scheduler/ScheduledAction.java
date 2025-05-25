package dev.crafty.core.scheduler;

import java.time.Duration;

/**
 * Represents an action that can be scheduled for execution.
 * <p>
 * Implementations define a unique identifier, a duration (interval or delay),
 * and the logic to be executed.
 * </p>
 * @since 1.0.0
 */
public interface ScheduledAction {
    /**
     * Returns the unique identifier for this scheduled action.
     *
     * @return the action's unique id
     */
    String id();

    /**
     * Returns the time between executions.
     *
     * @return the interval duration
     */
    Duration duration();

    /**
     * Executes the scheduled action's logic.
     */
    void run();
}