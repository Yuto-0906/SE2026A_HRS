package domain.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import domain.RepositoryException;
import infrastructure.jdbc.JdbcConnectionManager;

/**
 * JDBCによる利用者DAO。
 */
public class UserSqlDao implements UserDao {

	public HotelUser findById(String userId) throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT USER_ID, NAME, PHONE FROM USERS WHERE USER_ID = ?")) {
				statement.setString(1, userId);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						return null;
					}
					return new HotelUser(
							resultSet.getString("USER_ID"),
							resultSet.getString("NAME"),
							resultSet.getString("PHONE"));
				}
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("利用者の取得に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	public void save(HotelUser user) throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			if (exists(connection, user.getUserId())) {
				try (PreparedStatement statement = connection.prepareStatement(
						"UPDATE USERS SET NAME = ?, PHONE = ? WHERE USER_ID = ?")) {
					statement.setString(1, user.getName());
					statement.setString(2, user.getPhone());
					statement.setString(3, user.getUserId());
					statement.executeUpdate();
				}
			}
			else {
				try (PreparedStatement statement = connection.prepareStatement(
						"INSERT INTO USERS (USER_ID, NAME, PHONE) VALUES (?, ?, ?)")) {
					statement.setString(1, user.getUserId());
					statement.setString(2, user.getName());
					statement.setString(3, user.getPhone());
					statement.executeUpdate();
				}
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("利用者の保存に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	private boolean exists(Connection connection, String userId) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COUNT(*) FROM USERS WHERE USER_ID = ?")) {
			statement.setString(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1) > 0;
			}
		}
	}
}
