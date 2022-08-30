package wrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

public final class GitWrapper {

	private static final String GIT_PATH = "git";

	private GitWrapper() {
		// 
	}

	public static void setGitSettings(final String name, final String email, final String signingkey)
			throws IOException {

		setValueInGit("user.name", name);
		setValueInGit("user.email", email);

		setValueInGit("commit.gpgsign", "true");
		setValueInGit("user.signingkey", signingkey);
	}

	private static void setValueInGit(final String key, final String value) throws IOException {
		List<String> commands = Arrays.asList(GIT_PATH, "config", "--global", key, value);

		Process process = new ProcessBuilder(commands).start();
		try {
			if (process.waitFor() != 0) {
				throw new GITException(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
			}
		}
		catch (InterruptedException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	public static class GITException extends RuntimeException {

		public GITException(String reason) {
			super(reason);
		}
	}
}