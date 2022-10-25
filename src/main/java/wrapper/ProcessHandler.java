package wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;

final class ProcessHandler {

	private ProcessHandler() {
		// noop
	}

	static ProcessOutStreams handle(List<String> argumentList) throws IOException {

		final Process process = new ProcessBuilder(argumentList).start();

		try {
			if (process.waitFor() != 0) {
				throw new ProcessException(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
			}
		}
		catch (InterruptedException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		return new ProcessOutStreams(process.getInputStream(), process.getErrorStream());
	}

	static final class ProcessException extends RuntimeException {

		public ProcessException(String reason) {
			super(reason);
		}
	}

	static final class ProcessOutStreams {

		private InputStream stdOut;

		private InputStream stdErr;

		public ProcessOutStreams(InputStream stdOut, InputStream stdErr) {
			// We want to cache the stream contents for now, so we don't leak open streams from a Process instance.
			try {
				this.stdOut = IOUtils.toBufferedInputStream(stdOut);
				this.stdErr = IOUtils.toBufferedInputStream(stdErr);
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
