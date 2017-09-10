package net.ttddyy.dsproxy.proxy;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.NoOpQueryExecutionListener;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.jdk.ResultSetInvocationHandler;
import net.ttddyy.dsproxy.transform.QueryTransformer;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Tadaya Tsuyukubo
 */
public class StatementProxyLogicMockTest {

    private static final String DS_NAME = "myDS";

    @Test
    public void testExecuteUpdate() throws Throwable {
        final String query = "insert into emp (id, name) values (1, 'foo')";

        Statement stat = mock(Statement.class);
        when(stat.executeUpdate(query)).thenReturn(100);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("executeUpdate", String.class);
        Object result = logic.invoke(method, new Object[]{query});

        assertThat(result, is(instanceOf(int.class)));
        assertThat((Integer) result, is(100));
        verify(stat).executeUpdate(query);
        verifyListener(listener, "executeUpdate", query, query);
    }

    @Test
    public void testExecuteUpdateForException() throws Throwable {
        final String query = "insert into emp (id, name) values (1, 'foo')";

        Statement stat = mock(Statement.class);
        when(stat.executeUpdate(query)).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("executeUpdate", String.class);
            logic.invoke(method, new Object[]{query});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).executeUpdate(query);
        verifyListenerForException(listener, "executeUpdate", query, query);
    }

    @Test
    public void testExecuteUpdateWithAutoGeneratedKeys() throws Throwable {
        final String query = "insert into emp (id, name) values (1, 'foo')";

        Statement stat = mock(Statement.class);
        when(stat.executeUpdate(query, Statement.RETURN_GENERATED_KEYS)).thenReturn(100);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("executeUpdate", String.class, int.class);
        Object result = logic.invoke(method, new Object[]{query, Statement.RETURN_GENERATED_KEYS});

        assertThat(result, is(instanceOf(int.class)));
        assertThat((Integer) result, is(100));
        verify(stat).executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
        verifyListener(listener, "executeUpdate", query, query, Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    public void testExecuteUpdateWithAutoGeneratedKeysForException() throws Throwable {
        final String query = "insert into emp (id, name) values (1, 'foo')";

        Statement stat = mock(Statement.class);
        when(stat.executeUpdate(query, Statement.RETURN_GENERATED_KEYS)).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("executeUpdate", String.class, int.class);
            logic.invoke(method, new Object[]{query, Statement.RETURN_GENERATED_KEYS});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
        verifyListenerForException(listener, "executeUpdate", query, query, Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    public void testExecuteUpdateWithColumnIndexes() throws Throwable {
        final String query = "insert into emp (id, name) values (1, 'foo')";
        final int[] columnIndexes = {1, 2, 3};

        Statement stat = mock(Statement.class);
        when(stat.executeUpdate(query, columnIndexes)).thenReturn(100);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("executeUpdate", String.class, int[].class);
        Object result = logic.invoke(method, new Object[]{query, columnIndexes});

        assertThat(result, is(instanceOf(int.class)));
        assertThat((Integer) result, is(100));
        verify(stat).executeUpdate(query, columnIndexes);
        verifyListener(listener, "executeUpdate", query, query, columnIndexes);
    }

    @Test
    public void testExecuteUpdateWithColumnIndexesForException() throws Throwable {
        final String query = "insert into emp (id, name) values (1, 'foo')";
        final int[] columnIndexes = {1, 2, 3};

        Statement stat = mock(Statement.class);
        when(stat.executeUpdate(query, columnIndexes)).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("executeUpdate", String.class, int[].class);
            logic.invoke(method, new Object[]{query, columnIndexes});
            fail();
        } catch (SQLException e) {

        }

        verify(stat).executeUpdate(query, columnIndexes);
        verifyListenerForException(listener, "executeUpdate", query, query, columnIndexes);
    }

    @Test
    public void testExecuteUpdateWithColumnNames() throws Throwable {
        final String query = "insert into emp (id, name) values (1, 'foo')";
        final String[] columnNames = {"foo", "bar", "baz"};

        Statement stat = mock(Statement.class);
        when(stat.executeUpdate(query, columnNames)).thenReturn(100);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("executeUpdate", String.class, String[].class);
        Object result = logic.invoke(method, new Object[]{query, columnNames});

        assertThat(result, is(instanceOf(int.class)));
        assertThat((Integer) result, is(100));
        verify(stat).executeUpdate(query, columnNames);
        verifyListener(listener, "executeUpdate", query, query, columnNames);
    }

    @Test
    public void testExecuteUpdateWithColumnNamesForException() throws Throwable {
        final String query = "insert into emp (id, name) values (1, 'foo')";
        final String[] columnNames = {"foo", "bar", "baz"};

        Statement stat = mock(Statement.class);
        when(stat.executeUpdate(query, columnNames)).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("executeUpdate", String.class, String[].class);
            logic.invoke(method, new Object[]{query, columnNames});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).executeUpdate(query, columnNames);
        verifyListenerForException(listener, "executeUpdate", query, query, columnNames);
    }


    @Test
    public void testExecute() throws Throwable {
        final String query = "select * from emp";

        Statement stat = mock(Statement.class);
        when(stat.execute(query)).thenReturn(true);
        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("execute", String.class);
        Object result = logic.invoke(method, new Object[]{query});

        assertThat(result, is(instanceOf(boolean.class)));
        assertThat((Boolean) result, is(true));
        verify(stat).execute(query);
        verifyListener(listener, "execute", query, query);
    }

    @Test
    public void testExecuteForException() throws Throwable {
        final String query = "select * from emp";

        Statement stat = mock(Statement.class);
        when(stat.execute(query)).thenThrow(new SQLException());
        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("execute", String.class);
            logic.invoke(method, new Object[]{query});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).execute(query);
        verifyListenerForException(listener, "execute", query, query);
    }

    @Test
    public void testExecuteWithAutoGeneratedKeys() throws Throwable {
        final String query = "select * from emp";

        Statement stat = mock(Statement.class);
        when(stat.execute(query, Statement.RETURN_GENERATED_KEYS)).thenReturn(true);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("execute", String.class, int.class);
        Object result = logic.invoke(method, new Object[]{query, Statement.RETURN_GENERATED_KEYS});

        assertThat(result, is(instanceOf(boolean.class)));
        assertThat((Boolean) result, is(true));
        verify(stat).execute(query, Statement.RETURN_GENERATED_KEYS);
        verifyListener(listener, "execute", query, query, Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    public void testExecuteWithAutoGeneratedKeysForException() throws Throwable {
        final String query = "select * from emp";

        Statement stat = mock(Statement.class);
        when(stat.execute(query, Statement.RETURN_GENERATED_KEYS)).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("execute", String.class, int.class);
            logic.invoke(method, new Object[]{query, Statement.RETURN_GENERATED_KEYS});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).execute(query, Statement.RETURN_GENERATED_KEYS);
        verifyListenerForException(listener, "execute", query, query, Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    public void testExecuteWithColumnIndexes() throws Throwable {
        final String query = "select * from emp";
        final int[] columnIndexes = {1, 2, 3};

        Statement stat = mock(Statement.class);
        when(stat.execute(query, columnIndexes)).thenReturn(true);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("execute", String.class, int[].class);
        Object result = logic.invoke(method, new Object[]{query, columnIndexes});

        assertThat(result, is(instanceOf(boolean.class)));
        assertThat((Boolean) result, is(true));
        verify(stat).execute(query, columnIndexes);
        verifyListener(listener, "execute", query, query, columnIndexes);
    }

    @Test
    public void testExecuteWithColumnIndexesForException() throws Throwable {
        final String query = "select * from emp";
        final int[] columnIndexes = {1, 2, 3};

        Statement stat = mock(Statement.class);
        when(stat.execute(query, columnIndexes)).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("execute", String.class, int[].class);
            logic.invoke(method, new Object[]{query, columnIndexes});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).execute(query, columnIndexes);
        verifyListenerForException(listener, "execute", query, query, columnIndexes);
    }

    @Test
    public void testExecuteWithColumnNames() throws Throwable {
        final String query = "select * from emp";
        final String[] columnNames = {"foo", "bar", "baz"};

        Statement stat = mock(Statement.class);
        when(stat.execute(query, columnNames)).thenReturn(true);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("execute", String.class, String[].class);
        Object result = logic.invoke(method, new Object[]{query, columnNames});

        assertThat(result, is(instanceOf(boolean.class)));
        assertThat((Boolean) result, is(true));
        verify(stat).execute(query, columnNames);
        verifyListener(listener, "execute", query, query, columnNames);

    }

    @Test
    public void testExecuteWithColumnNamesForException() throws Throwable {
        final String query = "select * from emp";
        final String[] columnNames = {"foo", "bar", "baz"};

        Statement stat = mock(Statement.class);
        when(stat.execute(query, columnNames)).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("execute", String.class, String[].class);
            logic.invoke(method, new Object[]{query, columnNames});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).execute(query, columnNames);
        verifyListenerForException(listener, "execute", query, query, columnNames);

    }

    @Test
    public void testExecuteQuery() throws Throwable {
        final String query = "select * from emp";

        Statement stat = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);
        when(stat.executeQuery(query)).thenReturn(rs);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("executeQuery", String.class);
        Object result = logic.invoke(method, new Object[]{query});

        assertThat(result, is(instanceOf(ResultSet.class)));
        assertThat((ResultSet) result, is(rs));
        verify(stat).executeQuery(query);
        verifyListener(listener, "executeQuery", query, query);
    }

    @Test
    public void testExecuteQueryWithException() throws Throwable {
        final String query = "select * from emp";

        Statement stat = mock(Statement.class);
        when(stat.executeQuery(query)).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        try {
            Method method = Statement.class.getMethod("executeQuery", String.class);
            logic.invoke(method, new Object[]{query});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).executeQuery(query);
        verifyListenerForException(listener, "executeQuery", query, query);
    }

    @Test
    public void testExecuteLargeUpdate() throws Throwable {
        final String query = "select * from emp";

        Statement stat = mock(Statement.class);
        when(stat.executeLargeUpdate(query)).thenReturn(100L);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("executeLargeUpdate", String.class);
        Object result = logic.invoke(method, new Object[]{query});

        assertThat(result, is(instanceOf(long.class)));
        assertThat((Long) result, is(100L));
        verify(stat).executeLargeUpdate(query);
        verifyListener(listener, "executeLargeUpdate", query, query);
    }


    private StatementProxyLogic getProxyLogic(Statement statement, QueryExecutionListener listener, Connection proxyConnection) {
        return getProxyLogic(statement, listener, proxyConnection, false);
    }

    private StatementProxyLogic getProxyLogic(Statement statement, QueryExecutionListener listener, Connection proxyConnection,
                                              boolean createResultSetProxy) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setDataSourceName(DS_NAME);
        InterceptorHolder interceptorHolder = new InterceptorHolder(listener, QueryTransformer.DEFAULT);

        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .interceptorHolder(interceptorHolder)
                .resultSetProxyLogicFactory(createResultSetProxy ? new SimpleResultSetProxyLogicFactory() : null)
                .build();

        return StatementProxyLogic.Builder.create()
                .statement(statement)
                .connectionInfo(connectionInfo)
                .proxyConnection(proxyConnection)
                .proxyConfig(proxyConfig)
                .build();
    }

    @SuppressWarnings("unchecked")
    private void verifyListener(QueryExecutionListener listener, String methodName, String query, Object... methodArgs) {

        ArgumentCaptor<ExecutionInfo> executionInfoCaptor = ArgumentCaptor.forClass(ExecutionInfo.class);
        ArgumentCaptor<List> queryInfoListCaptor = ArgumentCaptor.forClass(List.class);

        verify(listener).afterQuery(executionInfoCaptor.capture(), queryInfoListCaptor.capture());

        ExecutionInfo execInfo = executionInfoCaptor.getValue();
        assertThat(execInfo.getMethod(), is(notNullValue()));
        assertThat(execInfo.getMethod().getName(), is(methodName));

        assertThat(execInfo.getMethodArgs(), arrayWithSize(methodArgs.length));
        assertThat(execInfo.getMethodArgs(), arrayContaining(methodArgs));
        assertThat(execInfo.getDataSourceName(), is(DS_NAME));
        assertThat(execInfo.getThrowable(), is(nullValue()));
        assertThat(execInfo.isBatch(), is(false));
        assertThat(execInfo.getBatchSize(), is(0));

        List<QueryInfo> queryInfoList = queryInfoListCaptor.getValue();
        assertThat(queryInfoList.size(), is(1));
        QueryInfo queryInfo = queryInfoList.get(0);
        assertThat(queryInfo.getQuery(), is(equalTo(query)));
    }

    @SuppressWarnings("unchecked")
    private void verifyListenerForException(QueryExecutionListener listener, String methodName,
                                            String query, Object... methodArgs) {

        ArgumentCaptor<ExecutionInfo> executionInfoCaptor = ArgumentCaptor.forClass(ExecutionInfo.class);
        ArgumentCaptor<List> queryInfoListCaptor = ArgumentCaptor.forClass(List.class);

        verify(listener).afterQuery(executionInfoCaptor.capture(), queryInfoListCaptor.capture());

        ExecutionInfo execInfo = executionInfoCaptor.getValue();
        assertThat(execInfo.getMethod(), is(notNullValue()));
        assertThat(execInfo.getMethod().getName(), is(methodName));

        assertThat(execInfo.getMethodArgs(), arrayWithSize(methodArgs.length));
        assertThat(execInfo.getMethodArgs(), arrayContaining(methodArgs));
        assertThat(execInfo.getDataSourceName(), is(DS_NAME));
        assertThat(execInfo.getThrowable(), is(instanceOf(SQLException.class)));

        List<QueryInfo> queryInfoList = queryInfoListCaptor.getValue();
        assertThat(queryInfoList.size(), is(1));
        QueryInfo queryInfo = queryInfoList.get(0);
        assertThat(queryInfo.getQuery(), is(equalTo(query)));
    }


    @Test
    public void testAddBatchException() throws Throwable {
        final String queryA = "insert into emp (id, name) values (1, 'foo')";
        final String queryB = "insert into emp (id, name) values (2, 'bar')";

        Statement stat = mock(Statement.class);
        doThrow(new SQLException()).when(stat).addBatch(queryB);

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        Method method = Statement.class.getMethod("addBatch", String.class);
        logic.invoke(method, new Object[]{queryA});

        try {
            logic.invoke(method, new Object[]{queryB});
            fail();
        } catch (SQLException e) {
        }

        verify(stat).addBatch(queryA);

    }

    @Test
    public void testExecuteBatch() throws Throwable {
        final String queryA = "insert into emp (id, name) values (1, 'foo')";
        final String queryB = "insert into emp (id, name) values (2, 'bar')";
        final String queryC = "insert into emp (id, name) values (3, 'baz')";

        Statement stat = mock(Statement.class);
        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        // run
        Method method = Statement.class.getMethod("addBatch", String.class);
        logic.invoke(method, new Object[]{queryA});
        logic.invoke(method, new Object[]{queryB});
        logic.invoke(method, new Object[]{queryC});

        method = Statement.class.getMethod("executeBatch");
        Object result = logic.invoke(method, null);

        assertThat(result, is(nullValue()));
        verify(stat).addBatch(queryA);
        verify(stat).addBatch(queryB);
        verify(stat).addBatch(queryC);
        verify(stat).executeBatch();
        verifyListenerForBatchExecution("executeBatch", listener, queryA, queryB, queryC);

    }

    @Test
    public void testExecuteBatchForException() throws Throwable {
        final String queryA = "insert into emp (id, name) values (1, 'foo')";
        final String queryB = "insert into emp (id, name) values (2, 'bar')";
        final String queryC = "insert into emp (id, name) values (3, 'baz')";

        Statement stat = mock(Statement.class);
        when(stat.executeBatch()).thenThrow(new SQLException());

        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        // run
        Method method = Statement.class.getMethod("addBatch", String.class);
        logic.invoke(method, new Object[]{queryA});
        logic.invoke(method, new Object[]{queryB});
        logic.invoke(method, new Object[]{queryC});

        try {
            method = Statement.class.getMethod("executeBatch");
            logic.invoke(method, null);
            fail();
        } catch (SQLException e) {
        }

        verify(stat).addBatch(queryA);
        verify(stat).addBatch(queryB);
        verify(stat).addBatch(queryC);
        verify(stat).executeBatch();
        verifyListenerForExecuteBatchForException(listener, queryA, queryB, queryC);

    }

    @Test
    public void testExecuteBatchWithClearBatch() throws Throwable {
        final String queryA = "insert into emp (id, name) values (1, 'foo')";
        final String queryB = "insert into emp (id, name) values (2, 'bar')";
        final String queryC = "insert into emp (id, name) values (3, 'baz')";

        Statement stat = mock(Statement.class);
        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        // run
        Method addBatch = Statement.class.getMethod("addBatch", String.class);
        Method clearBatch = Statement.class.getMethod("clearBatch");
        Method executeBatch = Statement.class.getMethod("executeBatch");

        logic.invoke(addBatch, new Object[]{queryA});
        logic.invoke(clearBatch, null);
        logic.invoke(addBatch, new Object[]{queryB});
        logic.invoke(addBatch, new Object[]{queryC});
        Object result = logic.invoke(executeBatch, null);

        assertThat(result, is(nullValue()));
        verify(stat).addBatch(queryA);
        verify(stat).clearBatch();
        verify(stat).addBatch(queryB);
        verify(stat).addBatch(queryC);
        verify(stat).executeBatch();
        verifyListenerForBatchExecution("executeBatch", listener, queryB, queryC);

    }

    @Test
    public void testExecuteLargeBatch() throws Throwable {
        final String queryA = "insert into emp (id, name) values (1, 'foo')";
        final String queryB = "insert into emp (id, name) values (2, 'bar')";
        final String queryC = "insert into emp (id, name) values (3, 'baz')";

        Statement stat = mock(Statement.class);
        QueryExecutionListener listener = mock(QueryExecutionListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, listener, null);

        // run
        Method method = Statement.class.getMethod("addBatch", String.class);
        logic.invoke(method, new Object[]{queryA});
        logic.invoke(method, new Object[]{queryB});
        logic.invoke(method, new Object[]{queryC});

        method = Statement.class.getMethod("executeLargeBatch");
        Object result = logic.invoke(method, null);

        assertThat(result, is(nullValue()));
        verify(stat).addBatch(queryA);
        verify(stat).addBatch(queryB);
        verify(stat).addBatch(queryC);
        verify(stat).executeLargeBatch();
        verifyListenerForBatchExecution("executeLargeBatch", listener, queryA, queryB, queryC);
    }

    @SuppressWarnings("unchecked")
    private void verifyListenerForBatchExecution(String batchMethod, QueryExecutionListener listener, String... queries) {
        ArgumentCaptor<ExecutionInfo> executionInfoCaptor = ArgumentCaptor.forClass(ExecutionInfo.class);
        ArgumentCaptor<List> queryInfoListCaptor = ArgumentCaptor.forClass(List.class);

        verify(listener).afterQuery(executionInfoCaptor.capture(), queryInfoListCaptor.capture());

        ExecutionInfo execInfo = executionInfoCaptor.getValue();
        assertThat(execInfo.getMethod(), is(notNullValue()));
        assertThat(execInfo.getMethod().getName(), is(batchMethod));
        assertThat(execInfo.getDataSourceName(), is(DS_NAME));
        assertThat(execInfo.getMethodArgs(), is(nullValue()));
        assertThat(execInfo.isBatch(), is(true));
        assertThat(execInfo.getBatchSize(), is(queries.length));

        List<QueryInfo> queryInfoList = queryInfoListCaptor.getValue();

        assertThat(queryInfoList, is(notNullValue()));
        assertThat(queryInfoList.size(), is(queries.length));

        for (int i = 0; i < queries.length; i++) {
            String expectedQuery = queries[i];
            QueryInfo queryInfo = queryInfoList.get(i);
            assertThat(queryInfo.getQuery(), is(expectedQuery));
            assertThat(queryInfo.getParametersList(), is(notNullValue()));
            assertThat(queryInfo.getParametersList().size(), is(0));
        }
    }

    @SuppressWarnings("unchecked")
    private void verifyListenerForExecuteBatchForException(QueryExecutionListener listener, String... queries) {
        ArgumentCaptor<ExecutionInfo> executionInfoCaptor = ArgumentCaptor.forClass(ExecutionInfo.class);
        ArgumentCaptor<List> queryInfoListCaptor = ArgumentCaptor.forClass(List.class);

        verify(listener).afterQuery(executionInfoCaptor.capture(), queryInfoListCaptor.capture());

        ExecutionInfo execInfo = executionInfoCaptor.getValue();
        assertThat(execInfo.getMethod(), is(notNullValue()));
        assertThat(execInfo.getMethod().getName(), is("executeBatch"));
        assertThat(execInfo.getDataSourceName(), is(DS_NAME));
        assertThat(execInfo.getMethodArgs(), is(nullValue()));
        assertThat(execInfo.getThrowable(), is(instanceOf(SQLException.class)));
        assertThat(execInfo.isBatch(), is(true));
        assertThat(execInfo.getBatchSize(), is(queries.length));


        List<QueryInfo> queryInfoList = queryInfoListCaptor.getValue();

        assertThat(queryInfoList, is(notNullValue()));
        assertThat(queryInfoList.size(), is(queries.length));

        for (int i = 0; i < queries.length; i++) {
            String expectedQuery = queries[i];
            QueryInfo queryInfo = queryInfoList.get(i);
            assertThat(queryInfo.getQuery(), is(expectedQuery));
            assertThat(queryInfo.getParametersList(), is(notNullValue()));
            assertThat(queryInfo.getParametersList().size(), is(0));
        }

    }

    @Test
    public void testGetTarget() throws Throwable {
        Statement stmt = mock(Statement.class);
        StatementProxyLogic logic = getProxyLogic(stmt, null, null);

        Method method = ProxyJdbcObject.class.getMethod("getTarget");
        Object result = logic.invoke(method, null);

        assertThat(result, notNullValue());
        assertThat(result, is(instanceOf(Statement.class)));

        Statement resultStmt = (Statement) result;
        assertThat(resultStmt, is(sameInstance(stmt)));
    }

    @Test
    public void testUnwrap() throws Throwable {
        Statement stmt = mock(Statement.class);
        when(stmt.unwrap(String.class)).thenReturn("called");

        StatementProxyLogic logic = getProxyLogic(stmt, null, null);

        Method method = Statement.class.getMethod("unwrap", Class.class);
        Object result = logic.invoke(method, new Object[]{String.class});

        verify(stmt).unwrap(String.class);
        assertThat(result, is(instanceOf(String.class)));
        assertThat((String) result, is("called"));
    }

    @Test
    public void testIsWrapperFor() throws Throwable {
        Statement stmt = mock(Statement.class);
        when(stmt.isWrapperFor(String.class)).thenReturn(true);

        StatementProxyLogic logic = getProxyLogic(stmt, null, null);

        Method method = Statement.class.getMethod("isWrapperFor", Class.class);
        Object result = logic.invoke(method, new Object[]{String.class});

        verify(stmt).isWrapperFor(String.class);
        assertThat(result, is(instanceOf(boolean.class)));
        assertThat((Boolean) result, is(true));
    }

    @Test
    public void testGetConnection() throws Throwable {
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);

        when(stmt.getConnection()).thenReturn(conn);
        StatementProxyLogic logic = getProxyLogic(stmt, null, conn);

        Method method = Statement.class.getMethod("getConnection");
        Object result = logic.invoke(method, null);

        assertThat(result, is(instanceOf(Connection.class)));
        assertThat((Connection) result, sameInstance(conn));

    }

    @Test
    public void testToString() throws Throwable {
        Statement stmt = mock(Statement.class);

        when(stmt.toString()).thenReturn("my ds");
        StatementProxyLogic logic = getProxyLogic(stmt, null, null);

        Method method = Object.class.getMethod("toString");
        Object result = logic.invoke(method, null);

        assertThat(result, is(instanceOf(String.class)));
        assertThat((String) result, is(stmt.getClass().getSimpleName() + " [my ds]"));
    }

    @Test
    public void testHashCode() throws Throwable {
        Statement stmt = mock(Statement.class);
        StatementProxyLogic logic = getProxyLogic(stmt, null, null);

        Method method = Object.class.getMethod("hashCode");
        Object result = logic.invoke(method, null);

        assertThat(result, is(instanceOf(Integer.class)));
        assertThat((Integer) result, is(stmt.hashCode()));
    }

    @Test
    public void testEquals() throws Throwable {
        Statement stmt = mock(Statement.class);
        StatementProxyLogic logic = getProxyLogic(stmt, null, null);

        Method method = Object.class.getMethod("equals", Object.class);

        // equals(null)
        Object result = logic.invoke(method, new Object[]{null});
        assertThat(result, is(instanceOf(Boolean.class)));
        assertThat((Boolean) result, is(false));

        // equals(true)
        result = logic.invoke(method, new Object[]{stmt});
        assertThat(result, is(instanceOf(Boolean.class)));
        assertThat((Boolean) result, is(true));
    }

    @Test
    public void proxyResultSet() throws Throwable {

        final AtomicReference<Object> listenerReceivedResult = new AtomicReference<Object>();
        QueryExecutionListener listener = new NoOpQueryExecutionListener() {
            @Override
            public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                listenerReceivedResult.set(execInfo.getResult());
            }
        };


        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getMetaData()).thenReturn(metaData);


        Statement stmt = mock(Statement.class);
        when(stmt.executeQuery(anyString())).thenReturn(resultSet);
        when(stmt.getGeneratedKeys()).thenReturn(resultSet);
        when(stmt.getResultSet()).thenReturn(resultSet);
        StatementProxyLogic logic = getProxyLogic(stmt, listener, null, true);


        // "executeQuery", "getGeneratedKeys", "getResultSet"
        Method executeQueryMethod = Statement.class.getMethod("executeQuery", String.class);
        Method getGeneratedKeysMethod = Statement.class.getMethod("getGeneratedKeys");
        Method getResultSetMethod = Statement.class.getMethod("getResultSet");
        Object result;

        // check "executeQuery"
        result = logic.invoke(executeQueryMethod, new Object[]{"SELECT *"});
        assertThat(result, is(instanceOf(ResultSet.class)));
        assertTrue(Proxy.isProxyClass(result.getClass()));
        assertTrue(Proxy.getInvocationHandler(result).getClass().equals(ResultSetInvocationHandler.class));
        assertThat("listener should receive proxied resultset", listenerReceivedResult.get(), sameInstance(result));

        listenerReceivedResult.set(null);

        // check "getGeneratedKeys"
        result = logic.invoke(getGeneratedKeysMethod, null);
        assertThat(result, is(instanceOf(ResultSet.class)));
        assertTrue(Proxy.isProxyClass(result.getClass()));
        assertTrue(Proxy.getInvocationHandler(result).getClass().equals(ResultSetInvocationHandler.class));
        assertThat("listener should receive proxied resultset", listenerReceivedResult.get(), sameInstance(result));

        listenerReceivedResult.set(null);

        // check "getResultSet"
        result = logic.invoke(getResultSetMethod, null);
        assertThat(result, is(instanceOf(ResultSet.class)));
        assertTrue(Proxy.isProxyClass(result.getClass()));
        assertTrue(Proxy.getInvocationHandler(result).getClass().equals(ResultSetInvocationHandler.class));
        assertThat("listener should receive proxied resultset", listenerReceivedResult.get(), sameInstance(result));

    }
}
