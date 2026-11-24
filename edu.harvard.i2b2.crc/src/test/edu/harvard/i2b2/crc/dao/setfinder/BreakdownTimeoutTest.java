package edu.harvard.i2b2.crc.dao.setfinder;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public class BreakdownTimeoutTest {
    @Test
    public void dispatcherPropagatesTimeoutWithoutUpdatingResultStatus() throws Exception {
        DataSourceLookup lookup = new DataSourceLookup();
        lookup.setFullSchema("");
        QueryExecutorHelperDao executor = new QueryExecutorHelperDao(null, lookup, lookup);
        Method runGenerator = QueryExecutorHelperDao.class.getDeclaredMethod(
                "runGenerator", String.class, Map.class, String.class);
        runGenerator.setAccessible(true);

        try {
            // No DAO is supplied. A retryable timeout must bypass the result error update.
            runGenerator.invoke(executor, "TEST_BREAKDOWN", Collections.emptyMap(),
                    TimedOutGenerator.class.getName());
            fail("Expected the breakdown timeout to propagate");
        } catch (InvocationTargetException e) {
            assertSame(TimedOutGenerator.TIMEOUT, e.getCause());
        }
    }

    public static class TimedOutGenerator implements IResultGenerator {
        static final CRCTimeOutException TIMEOUT = new CRCTimeOutException("The query was canceled.");

        @Override
        public void generateResult(Map param) throws CRCTimeOutException {
            throw TIMEOUT;
        }

        @Override
        public String getResults() {
            return "";
        }
    }
}
