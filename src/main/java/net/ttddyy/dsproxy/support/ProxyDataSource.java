package net.ttddyy.dsproxy.support;

import net.ttddyy.dsproxy.ConnectionIdManager;
import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.InterceptorHolder;
import net.ttddyy.dsproxy.proxy.JdbcProxyFactory;
import net.ttddyy.dsproxy.proxy.ProxyConfig;
import net.ttddyy.dsproxy.transform.QueryTransformer;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * A proxy of {@link javax.sql.DataSource} with {@link net.ttddyy.dsproxy.listener.QueryExecutionListener}.
 *
 * @author Tadaya Tsuyukubo
 */
public class ProxyDataSource implements DataSource, Closeable {

    private DataSource dataSource;
    private ProxyConfig proxyConfig = ProxyConfig.Builder.create().build();  // default

    public ProxyDataSource() {
    }

    public ProxyDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public Connection getConnection() throws SQLException {
        final Connection conn = dataSource.getConnection();
        return getConnectionProxy(conn);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        final Connection conn = dataSource.getConnection(username, password);
        return getConnectionProxy(conn);
    }

    private Connection getConnectionProxy(Connection conn) {
        String dataSourceName = this.proxyConfig.getDataSourceName();
        ConnectionIdManager connectionIdManager = this.proxyConfig.getConnectionIdManager();
        JdbcProxyFactory jdbcProxyFactory = this.proxyConfig.getJdbcProxyFactory();

        long connectionId = connectionIdManager.getId(conn);

        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setConnectionId(connectionId);
        connectionInfo.setDataSourceName(this.proxyConfig.getDataSourceName());

        return jdbcProxyFactory.createConnection(conn, connectionInfo, this.proxyConfig);
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws SQLException {
        dataSource.setLogWriter(printWriter);
    }

    @Override
    public void setLoginTimeout(int i) throws SQLException {
        dataSource.setLoginTimeout(i);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException {
        return dataSource.unwrap(tClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    @IgnoreJRERequirement
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();  // JDBC4.1 (jdk7+)
    }

    @Override
    public void close() throws IOException {
        if (dataSource instanceof Closeable) {
            ((Closeable) dataSource).close();
        }
    }

    /**
     * Set {@link QueryExecutionListener} with default(NoOp) {@link QueryTransformer}.
     *
     * @param listener a lister
     * @deprecated
     */
    public void setListener(QueryExecutionListener listener) {
        this.proxyConfig = ProxyConfig.Builder.from(this.proxyConfig)
                .interceptorHolder(new InterceptorHolder(listener, QueryTransformer.DEFAULT))
                .build();
    }

    public void addListener(QueryExecutionListener listener) {
        this.proxyConfig.getInterceptorHolder().addListener(listener);
    }

    public void setDataSourceName(String dataSourceName) {
        this.proxyConfig = ProxyConfig.Builder.from(this.proxyConfig)
                .dataSourceName(dataSourceName)
                .build();
    }

    public String getDataSourceName() {
        return this.proxyConfig.getDataSourceName();
    }

    /**
     * @since 1.4.2
     */
    public ConnectionIdManager getConnectionIdManager() {
        return this.proxyConfig.getConnectionIdManager();
    }


    /**
     * @since 1.4.2
     */
    public void setConnectionIdManager(ConnectionIdManager connectionIdManager) {
        this.proxyConfig = ProxyConfig.Builder.from(this.proxyConfig)
                .connectionIdManager(connectionIdManager)
                .build();
    }

    /**
     * @since 1.4.3
     */
    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    /**
     * @since 1.4.3
     */
    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

}
