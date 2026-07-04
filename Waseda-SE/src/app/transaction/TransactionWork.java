package app.transaction;

/**
 * トランザクション内で実行する処理。
 */
public interface TransactionWork<T> {

	T run() throws Exception;
}
