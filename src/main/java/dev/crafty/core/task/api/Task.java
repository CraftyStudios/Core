package dev.crafty.core.task.api;

import dev.crafty.core.number.UnitNumber;
import dev.crafty.core.number.units.TimeUnit;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Provides asynchronous task execution and scheduling functionality.
 * <p>
 * This interface allows the execution of tasks asynchronously on a specified target thread
 * and supports optional delayed execution.
 * @since 1.0.0
 */
public interface Task {
    /**
     * Executes a given task on a specified target thread and returns the result.
     *
     * @param <T> The type of the result produced by the task.
     * @param action The supplier representing the task to be executed.
     *               This task will be executed on the specified thread.
     * @param onSuccess The action to be run on success
     * @param targetThread The thread on which the task should be executed.
     */
    <T> void run(Callable<T> action, Consumer<T> onSuccess, TargetThread targetThread);

    /**
     * Executes a given task on a specified target thread and returns the result.
     *
     * @param <T> The type of the result produced by the task.
     * @param action The supplier representing the task to be executed.
     *               This task will be executed on the specified thread.
     * @param onSuccess The action to be run on success
     * @param onFailure The action to be run on failure
     * @param targetThread The thread on which the task should be executed.
     */
    <T> void run(Callable<T> action, Consumer<T> onSuccess, Runnable onFailure, TargetThread targetThread);

    /**
     * Executes a given task on a specified target thread.
     *
     * @param action The runnable task to be executed. This task will be executed
     *               on the specified thread.
     * @param targetThread The thread on which the task should be executed. This
     *                     specifies the execution context for the task.
     */
    void run(Runnable action, TargetThread targetThread);

    /**
     * Schedules a task for execution on a specified target thread after a specified delay
     * and returns the result of the task execution.
     *
     * @param <T> The type of the result produced by the task.
     * @param action The supplier representing the task to be executed.
     *               This task will be executed on the specified thread after the delay.
     * @param onSuccess The action to be run when the task is complete.
     * @param delay The delay before the task is executed.
     * @param targetThread The thread on which the task should be executed.
     */
    <T> void schedule(Callable<T> action, Consumer<T> onSuccess, UnitNumber<TimeUnit> delay, TargetThread targetThread);

    /**
     * Schedules a task for execution on a specified target thread after a specified delay
     * and returns the result of the task execution.
     *
     * @param <T> The type of the result produced by the task.
     * @param action The supplier representing the task to be executed.
     *               This task will be executed on the specified thread after the delay.
     * @param onSuccess The action to be run when the task is complete.
     * @param onFailure The action to be run on failure.
     * @param delay The delay before the task is executed.
     * @param targetThread The thread on which the task should be executed.
     */
    <T> void schedule(Callable<T> action, Consumer<T> onSuccess, Runnable onFailure, UnitNumber<TimeUnit> delay, TargetThread targetThread);


    /**
     * Schedules a task for execution on a specified target thread after a specified delay.
     *
     * @param action The runnable task to be executed. This task will be scheduled to
     *               run on the specified thread after the delay period has elapsed.
     * @param delay The delay before the task is executed.
     * @param targetThread The thread on which the task should be executed. This specifies
     *                     the execution context for the task.
     */
    void schedule(Runnable action, UnitNumber<TimeUnit> delay, TargetThread targetThread);
}
