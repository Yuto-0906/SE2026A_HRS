package infrastructure.jdbc;

import java.sql.Connection;

import app.AppException;
import app.transaction.TransactionManager;
import app.transaction.TransactionWork;

/**
 * JDBCによるトランザクション実装。
 */
public class JdbcTransactionManager implements TransactionManager {

	public <T> T execute(TransactionWork<T> work) throws AppException {
		JdbcConnectionManager manager = JdbcConnectionManager.getInstance();
		Connection connection = null;
		try {
			connection = manager.openConnection();
			connection.setAutoCommit(false);
			manager.bind(connection);
			T result = work.run();
			connection.commit();
			return result;
		}
		catch (Exception e) {
			rollback(connection);
			if (e instanceof AppException) {
				throw (AppException) e;
			}
			throw new AppException(e.getMessage() == null ? "処理に失敗しました。" : e.getMessage(), e);
		}
		finally {
			manager.unbind();
			manager.closeIfStandalone(connection);
		}
	}

	private void rollback(Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			connection.rollback();
		}
		catch (Exception ignored) {
			// 最初に発生した例外を優先する。
		}
	}
}
