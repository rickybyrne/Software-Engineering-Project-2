package quax.testutil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;

public class FxTestHelper {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    public static void initToolkit() {
        if (STARTED.compareAndSet(false, true)) {
            try {
                Platform.startup(() -> {
                    // JavaFX toolkit started
                });
            } catch (IllegalStateException ignored) {
                // Already started by another test
            }
        }
    }

    public static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean done = latch.await(10, TimeUnit.SECONDS);
            if (!done) {
                throw new RuntimeException("Timed out waiting for JavaFX thread.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }
}
