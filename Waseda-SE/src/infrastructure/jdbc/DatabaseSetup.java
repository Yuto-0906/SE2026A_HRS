package infrastructure.jdbc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

/**
 * setup.sqlを実行して開発用DBを初期化する。
 */
public class DatabaseSetup {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("setup.sqlのパスを指定してください。");
		}
		String sql = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "");
		JdbcConnectionManager manager = JdbcConnectionManager.getInstance();
		try (Connection connection = manager.openConnection();
				Statement statement = connection.createStatement()) {
			for (String command : sql.split(";")) {
				if (command.trim().length() > 0) {
					statement.execute(command.trim());
				}
			}
		}
		System.out.println("Database setup completed.");
	}
}
