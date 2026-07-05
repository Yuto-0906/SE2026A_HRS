package domain;

/**
 * 永続化処理の失敗を表す例外。
 */
public class RepositoryException extends Exception {

	public RepositoryException(String message) {
		super(message);
	}

	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}
}
