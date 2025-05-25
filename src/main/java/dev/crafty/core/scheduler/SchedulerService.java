package dev.crafty.core.scheduler;

import dev.crafty.core.storage.ProviderManager;
import dev.crafty.core.storage.StorageProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for scheduling, executing, and managing {@link ScheduledAction} instances.
 * <p>
 * This service allows actions to be scheduled for execution after a specified duration,
 * supports cancellation, and persists scheduling state using a {@link StorageProvider}.
 * </p>
 * @since 1.0.0
 */
public class SchedulerService {

    private final Map<String, ScheduledAction> scheduledActions = new HashMap<>();
    private final Map<String, Long> nextExecutionTimes = new HashMap<>(); // millis
    private final StorageProvider<StoredScheduledAction, String> storageProvider;

    /**
     * Record representing the persisted state of a scheduled action.
     *
     * @param id                  the unique identifier of the action
     * @param nextExecutionMillis the next scheduled execution time in milliseconds
     */
    private record StoredScheduledAction(String id, long nextExecutionMillis) {}

    /**
     * Constructs a new {@code SchedulerService} and initializes the storage provider.
     */
    public SchedulerService() {
        this.storageProvider = ProviderManager.getInstance().getProvider(StoredScheduledAction.class, "scheduled_actions");
    }

    /**
     * Schedules a new action for execution.
     * <p>
     * The action is stored in memory and persisted, with its next execution time calculated from the current time and its duration.
     * </p>
     *
     * @param action the {@link ScheduledAction} to schedule
     */
    public void schedule(ScheduledAction action) {
        scheduledActions.put(action.id(), action);
        nextExecutionTimes.put(action.id(), System.currentTimeMillis() + action.duration().toMillis());
        StoredScheduledAction storedAction = new StoredScheduledAction(action.id(), nextExecutionTimes.get(action.id()));
        storageProvider.save(action.id(), storedAction);
    }

    /**
     * Cancels a scheduled action by its identifier.
     * <p>
     * Removes the action from memory and deletes its persisted state.
     * </p>
     *
     * @param id the unique identifier of the action to cancel
     */
    public void cancel(String id) {
        scheduledActions.remove(id);
        nextExecutionTimes.remove(id);
        storageProvider.delete(id);
    }

    /**
     * Executes all scheduled actions whose next execution time has been reached or passed.
     * <p>
     * After execution, updates the next execution time and persists the new state.
     * </p>
     */
    public void run() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : nextExecutionTimes.entrySet()) {
            String id = entry.getKey();
            long nextExecutionTime = entry.getValue();

            if (currentTime >= nextExecutionTime) {
                ScheduledAction action = scheduledActions.get(id);
                if (action != null) {
                    action.run();
                    nextExecutionTimes.put(id, currentTime + action.duration().toMillis());
                    StoredScheduledAction storedAction = new StoredScheduledAction(action.id(), nextExecutionTimes.get(action.id()));
                    storageProvider.save(action.id(), storedAction);
                }
            }
        }
    }
}