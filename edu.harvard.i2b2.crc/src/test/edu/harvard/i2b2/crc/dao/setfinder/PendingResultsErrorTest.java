package edu.harvard.i2b2.crc.dao.setfinder;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

/** Standalone database regression tests; requires H2 on the test runtime classpath. */
public class PendingResultsErrorTest {
    private JdbcTemplate jdbc;
    private QueryResultInstanceSpringDao dao;

    @Before
    public void setUp() {
        DriverManagerDataSource source = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(source);
        jdbc.execute("create schema results");
        jdbc.execute("create table results.qt_query_result_instance ("
                + "result_instance_id integer primary key, query_instance_id integer,"
                + "status_type_id integer, end_date timestamp, message varchar(255),"
                + "set_size integer, real_set_size integer, obfusc_method varchar(20), result_type_id integer, description varchar(255))");
        jdbc.execute("create table results.qt_query_result_type (result_type_id integer, description varchar(255))");
        jdbc.execute("create table results.qt_query_instance (query_instance_id integer, query_master_id integer)");
        jdbc.execute("create table results.qt_query_master (query_master_id integer, name varchar(255))");
        jdbc.execute("insert into results.qt_query_result_type values (1, 'Age breakdown')");
        jdbc.execute("insert into results.qt_query_instance values (10, 100)");
        jdbc.execute("insert into results.qt_query_master values (100, 'Test query')");
        DataSourceLookup lookup = new DataSourceLookup();
        lookup.setFullSchema("results.");
        dao = new QueryResultInstanceSpringDao(source, lookup);
    }

    private void result(int id, int query, int status) {
        jdbc.update("insert into results.qt_query_result_instance values (?, ?, ?, null, ?, 42, 45, 'OBTOTAL', 1, null)",
                id, query, status, "original " + id);
    }

    private Map<String, Object> row(int id) {
        return jdbc.queryForMap("select * from results.qt_query_result_instance where result_instance_id = ?", id);
    }

    @Test
    public void middleFailurePreservesSuccessAndOriginalError() {
        result(1, 10, 3);
        result(2, 10, 4);
        result(3, 10, 1);
        Map<String, Object> success = row(1), error = row(2);
        dao.markPendingResultsError("10", "Execution failed");
        assertEquals(success, row(1));
        assertEquals(error, row(2));
        assertEquals(4, row(3).get("STATUS_TYPE_ID"));
        assertNotNull(row(3).get("END_DATE"));
        assertEquals("Execution failed", row(3).get("MESSAGE"));
        assertEquals(-1, row(3).get("SET_SIZE"));
        assertEquals(-1, row(3).get("REAL_SET_SIZE"));
        assertEquals("", row(3).get("OBFUSC_METHOD"));
        assertEquals("Age breakdown for \"Test query\"", row(3).get("DESCRIPTION"));
    }

    @Test
    public void failureBeforeGeneratorStartsTerminatesAllPendingResults() {
        result(1, 10, 1);
        result(2, 10, 2);
        result(3, 10, 1);
        dao.markPendingResultsError("10", "Execution failed");
        assertEquals(java.util.Arrays.asList(4, 4, 4), jdbc.queryForList(
                "select status_type_id from results.qt_query_result_instance order by result_instance_id", Integer.class));
    }

    @Test
    public void preservesOtherQueriesAndTerminalStates() {
        result(1, 11, 1);
        result(2, 11, 2);
        for (int status : new int[] {3, 4, 5, 6, 9, 10}) {
            result(status + 10, 10, status);
        }
        List<Map<String, Object>> before = jdbc.queryForList("select * from results.qt_query_result_instance order by result_instance_id");
        dao.markPendingResultsError("10", "Execution failed");
        assertEquals(before, jdbc.queryForList("select * from results.qt_query_result_instance order by result_instance_id"));
    }

    @Test
    public void preservesExistingDescriptionAndFillsEmptyDescription() {
        result(1, 10, 2);
        result(2, 10, 1);
        jdbc.update("update results.qt_query_result_instance set description = 'Custom description' where result_instance_id = 1");
        jdbc.update("update results.qt_query_result_instance set description = '' where result_instance_id = 2");
        dao.markPendingResultsError("10", "Execution failed");
        assertEquals("Custom description", row(1).get("DESCRIPTION"));
        assertEquals("Age breakdown for \"Test query\"", row(2).get("DESCRIPTION"));
    }

    @Test
    public void missingMetadataStillMarksResultAsError() {
        result(1, 11, 1);
        jdbc.execute("delete from results.qt_query_result_type");
        dao.markPendingResultsError("11", "Execution failed");
        assertEquals(4, row(1).get("STATUS_TYPE_ID"));
        assertEquals(-1, row(1).get("SET_SIZE"));
        assertEquals("Execution failed", row(1).get("DESCRIPTION"));
    }

    @Test
    public void repeatedCleanupDoesNotOverwriteErrorDetails() {
        result(1, 10, 2);
        dao.markPendingResultsError("10", "Original failure");
        Map<String, Object> failed = row(1);
        dao.markPendingResultsError("10", "Later failure");
        assertEquals(failed, row(1));
    }

    @Test
    public void resultThatFinishesBetweenReadAndUpdateIsPreserved() {
        result(1, 10, 2);
        dao.jdbcTemplate = new JdbcTemplate(jdbc.getDataSource()) {
            @Override
            public int update(String sql, Object[] args, int[] types) {
                jdbc.update("update results.qt_query_result_instance set status_type_id = 3,"
                        + " description = 'Finished result' where result_instance_id = 1");
                return super.update(sql, args, types);
            }
        };
        dao.markPendingResultsError("10", "Execution failed");
        assertEquals(3, row(1).get("STATUS_TYPE_ID"));
        assertEquals(42, row(1).get("SET_SIZE"));
        assertEquals(45, row(1).get("REAL_SET_SIZE"));
        assertEquals("Finished result", row(1).get("DESCRIPTION"));
        assertEquals("original 1", row(1).get("MESSAGE"));
        assertNull(row(1).get("END_DATE"));
    }

    @Test
    public void resultThatFinishesBeforeCleanupIsPreserved() {
        result(1, 10, 2);
        jdbc.update("update results.qt_query_result_instance set status_type_id = 3 where result_instance_id = 1");
        Map<String, Object> finished = row(1);
        dao.markPendingResultsError("10", "Execution failed");
        assertEquals(finished, row(1));
    }
}
