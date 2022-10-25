package wrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import wrapper.ProcessHandler.ProcessOutStreams;

public final class GitWrapper {

	private static final String GIT_PATH = "git";

	private GitWrapper() {
		// noop
	}

	public static void setGitSettings(final String name, final String email, final String signingkey)
			throws IOException {

		setValueInGit("user.name", name);
		setValueInGit("user.email", email);

		setValueInGit("commit.gpgsign", "true");
		setValueInGit("user.signingkey", signingkey);
	}

	private static ProcessOutStreams setValueInGit(final String key, final String value) throws IOException {
		final List<String> argumentList = Arrays.asList(GIT_PATH, "config", "--global", key, value);
		return ProcessHandler.handle(argumentList);
	}
}