package domain.user;

/**
 * ホテル予約システムの利用者。
 */
public class HotelUser {

	private String userId;

	private String name;

	private String phone;

	public HotelUser(String userId, String name, String phone) {
		this.userId = userId;
		this.name = name;
		this.phone = phone;
	}

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public void updateProfile(String name, String phone) {
		this.name = name;
		this.phone = phone;
	}
}
