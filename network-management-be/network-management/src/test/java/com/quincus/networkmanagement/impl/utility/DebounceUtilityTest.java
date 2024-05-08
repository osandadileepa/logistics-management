package com.quincus.networkmanagement.impl.utility;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DebounceUtilityTest {

    private DebounceUtility debounceUtility;

    @BeforeEach
    public void setUp() {
        debounceUtility = new DebounceUtility();
    }

    @AfterEach
    public void tearDown() {
        debounceUtility.shutdown();
    }

    @Test
    void testDebounce() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable mockRunnable = mock(Runnable.class);

        debounceUtility.debounce("NETWORK-TRAINING-001", () -> {
            mockRunnable.run();
            latch.countDown();
        }, 100, TimeUnit.MILLISECONDS);

        assertTrue(latch.await(200, TimeUnit.MILLISECONDS));
        verify(mockRunnable, times(1)).run();
    }

    @Test
    void testDebounceMultipleCalls() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable mockRunnable = mock(Runnable.class);

        for (int i = 0; i < 5; i++) {
            debounceUtility.debounce("NETWORK-TRAINING-001", () -> {
                mockRunnable.run();
                latch.countDown();
            }, 100, TimeUnit.MILLISECONDS);
        }

        assertTrue(latch.await(200, TimeUnit.MILLISECONDS));
        verify(mockRunnable, times(1)).run();
    }

    @Test
    void testDebounceCancellation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable mockRunnable = mock(Runnable.class);

        debounceUtility.debounce("NETWORK-TRAINING-001", () -> {
            mockRunnable.run();
            latch.countDown();
        }, 100, TimeUnit.MILLISECONDS);

        debounceUtility.debounce("NETWORK-TRAINING-001", () -> {
        }, 100, TimeUnit.MILLISECONDS);

        assertFalse(latch.await(200, TimeUnit.MILLISECONDS));
        verify(mockRunnable, never()).run();
    }
}
