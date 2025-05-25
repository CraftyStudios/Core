package dev.crafty.core.task.api;

/**
 * An enumeration representing the target execution thread for asynchronous tasks.
 * <p>
 * This enum is primarily used in conjunction with methods provided by the {@link Async} interface
 * to specify the thread on which a task should be executed.
 * <p>
 * The available thread targets are: <p>
 * - {@link TargetThread#VIRTUAL}: Indicates that the task should run on a virtual thread. <p>
 * - {@link TargetThread#PLATFORM}: Indicates that the task should run on a platform-specific thread. <p>
 * - {@link TargetThread#MAIN}: Indicates that the task should run on the main thread. <p>
 * @since 1.0.0
 */
public enum TargetThread {
    /**
     * Represents a virtual thread as the target execution context for a task.
     * <p>
     * When this option is used, the task will execute on a virtual thread, which is
     * lightweight and managed by the Java runtime, allowing for highly concurrent
     * and scalable task execution.
     */
    VIRTUAL,
    /**
     * Represents a platform-specific thread as the target execution context for a task.
     * This option indicates that the task should execute on a thread determined by the platform,
     * which may depend on the underlying operating system or runtime configuration.
     * <p>
     * This is used for more CPU-intensive workloads. For normal-intensity workloads and IO calls,
     * use {@link TargetThread#VIRTUAL}
     */
    PLATFORM,

    /**
     * Represents the main thread as the target execution context for a task.
     * This option specifies that the task should execute on the main thread.
     */
    MAIN
}
