package dev.crafty.core.task.providers;

import com.google.common.util.concurrent.*;
import dev.crafty.core.number.UnitNumber;
import dev.crafty.core.number.units.TimeUnit;
import dev.crafty.core.task.api.TargetThread;
import dev.crafty.core.task.api.Task;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @since 1.0.0
 */
public class TaskProvider implements Task {
    private final ListeningExecutorService mainThreadService;
    private final ListeningExecutorService physicalThreadService;
    private final ListeningExecutorService virtualThreadService;

    private final Timer timer;

    public TaskProvider() {
        this.mainThreadService = MoreExecutors.listeningDecorator(MoreExecutors.newDirectExecutorService());
        this.physicalThreadService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        this.virtualThreadService = MoreExecutors.listeningDecorator(Executors.newVirtualThreadPerTaskExecutor());
        this.timer = new Timer();
    }

    @Override
    public <T> void run(Callable<T> action, Consumer<T> onSuccess, TargetThread targetThread) {
        run(action, onSuccess, () -> {}, targetThread);
    }

    @Override
    public <T> void run(Callable<T> action, Consumer<T> onSuccess, Runnable onFailure, TargetThread targetThread) {
        schedule(action, onSuccess, onFailure, UnitNumber.of(0, TimeUnit.MILLISECOND), targetThread);
    }

    @Override
    public void run(Runnable action, TargetThread targetThread) {
        schedule(action, UnitNumber.of(0, TimeUnit.MILLISECOND), targetThread);
    }

    @Override
    public <T> void schedule(Callable<T> action, Consumer<T> onSuccess, UnitNumber<TimeUnit> delay, TargetThread targetThread) {
        schedule(action, onSuccess, () -> {}, delay, targetThread);
    }

    @Override
    public <T> void schedule(Callable<T> action, Consumer<T> onSuccess, Runnable onFailure, UnitNumber<TimeUnit> delay, TargetThread targetThread) {
        int msDelay = delay.convertTo(TimeUnit.MILLISECOND).value().intValue();
        var executor = getThreadService(targetThread);

        timer.schedule(newTimerTask(() -> {
            ListenableFuture<T> future = executor.submit(action);

            FutureCallback<T> callback = new FutureCallback<>() {
                @Override
                public void onSuccess(T result) {
                    onSuccess.accept(result);
                }

                @Override
                public void onFailure(@NotNull Throwable t) {

                }
            };

            Futures.addCallback(future, callback, executor);
        }), msDelay);
    }

    @Override
    public void schedule(Runnable action, @NotNull UnitNumber<TimeUnit> delay, TargetThread targetThread) {
        int msDelay = delay.convertTo(TimeUnit.MILLISECOND).value().intValue();
        var executor = getThreadService(targetThread);

        timer.schedule(newTimerTask(() -> executor.submit(action)), msDelay);
    }

    @Contract(pure = true)
    private ListeningExecutorService getThreadService(TargetThread targetThread) {
        return switch (targetThread) {
            case VIRTUAL -> virtualThreadService;
            case PLATFORM -> physicalThreadService;
            case MAIN -> mainThreadService;
        };
    }

    @Contract(value = "_ -> new", pure = true)
    private @NotNull TimerTask newTimerTask(Runnable action) {
        return new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        };
    }
}
