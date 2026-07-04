package domain;

/**
 * ドメイン規則に違反した操作を表す例外。
 */
public class DomainException extends Exception {

	public DomainException(String message) {
		super(message);
	}
}
