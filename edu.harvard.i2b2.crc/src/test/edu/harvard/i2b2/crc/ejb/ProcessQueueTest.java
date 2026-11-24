package edu.harvard.i2b2.crc.ejb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProcessQueueTest {
    @Test
    public void terminalErrorBlocksEscalationWhileWorkerWritesFailureStatuses() {
        ExecRunnable exec = new ExecRunnable();
        exec.setJobErrorFlag(true);

        assertFalse(ProcessQueue.shouldAdvanceQueue(exec, 500, 1000));
        assertFalse(ProcessQueue.shouldAdvanceQueue(exec, 1000, 1000));
    }

    @Test
    public void unfinishedQueryCanEscalateOnceItsWaitExpires() {
        ExecRunnable exec = new ExecRunnable();

        assertFalse(ProcessQueue.shouldAdvanceQueue(exec, 999, 1000));
        assertTrue(ProcessQueue.shouldAdvanceQueue(exec, 1000, 1000));
    }

    @Test
    public void completedQueryDoesNotEscalateAtDeadline() {
        ExecRunnable exec = new ExecRunnable();
        exec.setJobCompleteFlag(true);

        assertFalse(ProcessQueue.shouldAdvanceQueue(exec, 1000, 1000));
    }
}
