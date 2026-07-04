package infrastructure.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import domain.RepositoryException;

/**
 * JDBC接続とトランザクション中の接続共有を管理する。
 */
public class JdbcConnectionManager {

	private static final JdbcConnectionManager INSTANCE = new JdbcConnectionManager();

	private static final String DEFAULT_URL = "jdbc:hsqldb:hsql://localhost";

	private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<Connection>();

	private JdbcConnectionManager() {
	}

	public static JdbcConnectionManager getInstance() {
		return INSTANCE;
	}

	public Connection openConnection() throws RepositoryException {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			String url = System.getProperty("hrs.db.url", DEFAULT_URL);
			String user = System.getProperty("hrs.db.user", "sa");
			String password = System.getProperty("hrs.db.password", "");
			return DriverManager.getConnection(url, user, password);
		}
		catch (Exception e) {
			throw new RepositoryException("データベースへ接続できません。", e);
		}
	}

	public Connection getConnection() throws RepositoryException {
		Connection connection = transactionConnection.get();
		return connection == null ? openConnection() : connection;
	}

	public void bind(Connection connection) {
		transactionConnection.set(connection);
	}

	public void unbind() {
		transactionConnection.remove();
	}

	public boolean isTransactionConnection(Connection connection) {
		return connection != null && connection == transactionConnection.get();
	}

	public void closeIfStandalone(Connection connection) {
		if (connection == null || isTransactionConnection(connection)) {
			return;
		}
		try {
			connection.close();
		}
		catch (SQLException ignored) {
			// 元の例外を優先するため，単独接続のclose失敗はここでは送出しない。
		}
	}
}
