package wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import model.GPGKey;

/**
 * Thin Java wrapper for GPG command line tool.
 *
 */
public final class GPGWrapper {

	private static final String GPG_PATH = "gpg";

	private static final String ALGO = "rsa4096";

	private static final String USAGE = "default";

	private static final String EXPIRE = "2y";

	private GPGWrapper() {
		//
	}

	/**
	 * Generates a GPG Key according to:
	 * https://serverfault.com/questions/818289/add-second-sub-key-to-unattended-gpg-key#962553
	 * 
	 * @param name
	 * @param email
	 * @return KeyID, not fingerprint
	 * @throws IOException
	 */
	public static String createKeyForGHE(final String name, final String email, final String passphrase)
			throws IOException {

		try (InputStream is =
				runGPG("--no-tty", "--passphrase", passphrase, "--quick-generate-key", name + " <" + email + ">", ALGO,
						USAGE, EXPIRE).getStdErr()) {

			String output = IOUtils.toString(is, Charset.defaultCharset());

			String fpr = StringUtils.substringAfterLast(output, "/");
			fpr = StringUtils.substringBefore(fpr, ".").trim();

			runGPG("--pinentry-mode=loopback", "--passphrase", passphrase, "--quick-add-key", fpr, ALGO, USAGE, EXPIRE);

			String keyId = StringUtils.substringBetween(output, "gpg: key", "marked as ultimately trusted");

			return keyId.trim();
		}
	}

	/**
	 * Loads the newly generated key out of GPG and sends it to Github
	 * 
	 * @param keyFingerprint
	 * @return {@link GPGKey}
	 * @throws IOException
	 */
	public static GPGKey exportKeyforGHE(final String keyFingerprint) throws IOException {

		try (InputStream is = runGPG("-a", "--export", keyFingerprint).getStdOut()) {

			String armoredPublicKey = IOUtils.toString(is, Charset.defaultCharset());

			GPGKey gpgKey = new GPGKey();
			gpgKey.setArmoredPublicKey(armoredPublicKey.trim());

			return gpgKey;
		}
	}

	/**
	 * Run GPG and pipe data to process
	 *
	 * @param arguments
	 * @return GPGOutStreams object holding cached streams of gpg's stdout and stderr output.
	 * @throws IOException
	 */
	private static GPGOutStreams runGPG(String... arguments) throws IOException {

		List<String> command = new ArrayList<>();
		command.add(GPG_PATH);
		command.add("--batch");
		command.addAll(Arrays.asList(arguments));

		ProcessBuilder pb = new ProcessBuilder(command);
		Process process = pb.start();
		try {
			if (process.waitFor() != 0) {
				throw new GPGException(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
			}
		}
		catch (InterruptedException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		return new GPGOutStreams(process.getInputStream(), process.getErrorStream());
	}

	public static class GPGException extends RuntimeException {

		public GPGException(String reason) {
			super(reason);
		}
	}

	private static final class GPGOutStreams {

		private InputStream stdOut;

		private InputStream stdErr;

		public GPGOutStreams(InputStream gpgStdOut, InputStream gpgStdErr) {
			// We want to cache the stream contents for now, so we don't leak open streams from a Process instance.
			try {
				this.stdOut = IOUtils.toBufferedInputStream(gpgStdOut);
				this.stdErr = IOUtils.toBufferedInputStream(gpgStdErr);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public InputStream getStdOut() {
			return stdOut;
		}

		public InputStream getStdErr() {
			return stdErr;
		}
	}
}
