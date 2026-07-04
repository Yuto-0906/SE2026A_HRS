package domain.user;

import domain.RepositoryException;

/**
 * 利用者の永続化インタフェース。
 */
public interface UserDao {

	HotelUser findById(String userId) throws RepositoryException;

	void save(HotelUser user) throws RepositoryException;
}
