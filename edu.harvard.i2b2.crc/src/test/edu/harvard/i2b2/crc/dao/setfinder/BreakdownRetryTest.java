package edu.harvard.i2b2.crc.dao.setfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;

public class BreakdownRetryTest {
    @Test
    public void retryRunsOnlyUnfinishedBreakdowns() throws Exception {
        DataSourceLookup lookup = new DataSourceLookup();
        lookup.setFullSchema("");
        QueryExecutorHelperDao executor = new QueryExecutorHelperDao(null, lookup, lookup);

        List<String> executed = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("executed", executed);

        // A finished output needs neither its generator configuration nor a status write.
        assertFalse(executor.runUnfinishedResultGenerator(null, "FINISHED",
                result("1", QueryStatusTypeId.STATUSTYPE_ID_FINISHED), parameters));
        assertFalse(executor.runUnfinishedResultGenerator(null, "COMPLETED",
                result("2", QueryStatusTypeId.STATUSTYPE_ID_COMPLETED), parameters));

        SetFinderDAOFactory factory = recordingGeneratorFactory();
        assertTrue(executor.runUnfinishedResultGenerator(factory, "PROCESSING",
                result("3", QueryStatusTypeId.STATUSTYPE_ID_PROCESSING), parameters));
        assertTrue(executor.runUnfinishedResultGenerator(factory, "QUEUED",
                result("4", QueryStatusTypeId.STATUSTYPE_ID_QUEUED), parameters));
        assertEquals(Arrays.asList("3", "4"), executed);
    }

    private static QtQueryResultInstance result(String id, int statusId) {
        QtQueryStatusType status = new QtQueryStatusType();
        status.setStatusTypeId(statusId);
        QtQueryResultInstance result = new QtQueryResultInstance();
        result.setResultInstanceId(id);
        result.setQtQueryStatusType(status);
        return result;
    }

    private static SetFinderDAOFactory recordingGeneratorFactory() {
        QtQueryBreakdownType breakdown = new QtQueryBreakdownType();
        breakdown.setClassname(RecordingGenerator.class.getName());
        IQueryBreakdownTypeDao dao = new IQueryBreakdownTypeDao() {
            public QtQueryBreakdownType getBreakdownTypeByName(String name) {
                return breakdown;
            }

            public QtQueryBreakdownType getBreakdownTypeByName(String name, String group) {
                throw new AssertionError("Unexpected group lookup");
            }
        };
        return (SetFinderDAOFactory) Proxy.newProxyInstance(
                SetFinderDAOFactory.class.getClassLoader(), new Class<?>[] { SetFinderDAOFactory.class },
                (proxy, method, args) -> {
                    if ("getQueryBreakdownTypeDao".equals(method.getName())) {
                        return dao;
                    }
                    throw new AssertionError("Unexpected DAO call: " + method.getName());
                });
    }

    public static class RecordingGenerator implements IResultGenerator {
        @Override
        @SuppressWarnings("unchecked")
        public void generateResult(Map parameters) {
            ((List<String>) parameters.get("executed")).add((String) parameters.get("ResultInstanceId"));
        }

        @Override
        public String getResults() {
            return "";
        }
    }
}
