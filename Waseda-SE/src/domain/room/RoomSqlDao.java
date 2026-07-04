package domain.room;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import domain.RepositoryException;
import infrastructure.jdbc.JdbcConnectionManager;

/**
 * JDBCによる部屋DAO。
 */
public class RoomSqlDao implements RoomDao {

	public List<Room> findAll() throws RepositoryException {
		Connection connection = null;
		List<Room> rooms = new ArrayList<Room>();
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT ROOM_NUMBER FROM ROOMS ORDER BY ROOM_NUMBER");
					ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					rooms.add(new Room(resultSet.getString("ROOM_NUMBER")));
				}
			}
			return rooms;
		}
		catch (SQLException e) {
			throw new RepositoryException("部屋一覧の取得に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	public Room findAvailable(LocalDate checkInDate) throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT R.ROOM_NUMBER FROM ROOMS R "
					+ "WHERE NOT EXISTS ("
					+ "SELECT 1 FROM ROOM_ALLOCATIONS A "
					+ "WHERE A.ROOM_NUMBER = R.ROOM_NUMBER AND A.CHECK_IN_DATE = ?) "
					+ "ORDER BY R.ROOM_NUMBER")) {
				statement.setDate(1, Date.valueOf(checkInDate));
				try (ResultSet resultSet = statement.executeQuery()) {
					return resultSet.next() ? new Room(resultSet.getString("ROOM_NUMBER")) : null;
				}
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("空室の検索に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	public Room findByNumber(String roomNumber) throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT ROOM_NUMBER FROM ROOMS WHERE ROOM_NUMBER = ?")) {
				statement.setString(1, roomNumber);
				try (ResultSet resultSet = statement.executeQuery()) {
					return resultSet.next() ? new Room(resultSet.getString("ROOM_NUMBER")) : null;
				}
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("部屋の取得に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	// 半完成コードとのコンパイル互換性を保つ旧インタフェース。
	public List getRooms() throws RoomException {
		try {
			return findAll();
		}
		catch (RepositoryException e) {
			throw new RoomException(RoomException.CODE_DB_EXEC_QUERY_ERROR, e);
		}
	}

	public List getEmptyRooms() throws RoomException {
		return getRooms();
	}

	public Room getRoom(String roomNumber) throws RoomException {
		try {
			return findByNumber(roomNumber);
		}
		catch (RepositoryException e) {
			throw new RoomException(RoomException.CODE_DB_EXEC_QUERY_ERROR, e);
		}
	}

	public void updateRoom(Room room) throws RoomException {
		// 新設計では部屋の利用状況をROOM_ALLOCATIONSで管理する。
	}
}
