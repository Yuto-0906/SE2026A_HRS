package domain.reservation;

/**
 * 予約の状態。
 */
public enum ReservationStatus {
	RESERVED("予約済み"),
	CHECKED_IN("チェックイン済み"),
	CHECKED_OUT("チェックアウト済み"),
	CANCELED("キャンセル済み");

	private final String displayName;

	ReservationStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
