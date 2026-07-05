package app.transaction;

import app.AppException;

/**
 * トランザクション境界を提供する。
 */
public interface TransactionManager {

	<T> T execute(TransactionWork<T> work) throws AppException;
}
