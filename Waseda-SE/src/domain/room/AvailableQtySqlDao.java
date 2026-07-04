package domain.room;

import java.util.Date;

/**
 * 旧CUIとのコンパイル互換性を保つクラス。
 *
 * 新しいWeb版では空室数を重複保持せず，部屋と予約割当から計算する。
 */
public class AvailableQtySqlDao implements AvailableQtyDao {

	public AvailableQty getAvailableQty(Date date) throws RoomException {
		throw unsupported();
	}

	public void updateAvailableQty(AvailableQty availableQty) throws RoomException {
		throw unsupported();
	}

	public void createAbailableQty(AvailableQty availableQty) throws RoomException {
		throw unsupported();
	}

	private RoomException unsupported() {
		RoomException exception = new RoomException(RoomException.CODE_DB_EXEC_QUERY_ERROR);
		exception.getDetailMessages().add(
				"AvailableQty persistence is not used by the Web version.");
		return exception;
	}
}
