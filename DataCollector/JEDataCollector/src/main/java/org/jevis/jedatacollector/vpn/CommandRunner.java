package org.jevis.jedatacollector.vpn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Runs an external command with a timeout.
 */
public interface CommandRunner {

    Result run(List<String> command, long timeoutMillis);

    class Result {
        public static final int TIMEOUT = -1;
        public static final int START_FAILED = -2;

        private final int exitCode;
        private final String output;

        public Result(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }

        @Override
        public String toString() {
            return "exit=" + exitCode + (output.isEmpty() ? "" : ", output=" + output.trim());
        }
    }

    /**
     * Default implementation based on {@link ProcessBuilder}. It keeps waiting for the command even if
     * the calling thread is interrupted, so cleanup commands also complete on cancelled jobs.
     */
    class ProcessCommandRunner implements CommandRunner {

        private static void copy(InputStream in, ByteArrayOutputStream out) {
            byte[] buffer = new byte[4096];
            int read;
            try {
                while ((read = in.read(buffer)) != -1) {
                    synchronized (out) {
                        out.write(buffer, 0, read);
                    }
                }
            } catch (IOException ignored) {
            }
        }

        @Override
        public Result run(List<String> command, long timeoutMillis) {
            Process process;
            try {
                process = new ProcessBuilder(command).redirectErrorStream(true).start();
            } catch (IOException e) {
                return new Result(Result.START_FAILED, e.getMessage() == null ? e.toString() : e.getMessage());
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thread reader = new Thread(() -> copy(process.getInputStream(), output), "vpn-cmd-output");
            reader.setDaemon(true);
            reader.start();

            boolean interrupted = false;
            boolean finished = false;
            long deadline = System.currentTimeMillis() + timeoutMillis;
            try {
                while (!finished) {
                    long remaining = deadline - System.currentTimeMillis();
                    if (remaining <= 0) break;
                    try {
                        finished = process.waitFor(remaining, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                if (!finished) {
                    process.destroyForcibly();
                    return new Result(Result.TIMEOUT, "timeout after " + timeoutMillis + " ms");
                }
                try {
                    reader.join(1000);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
                synchronized (output) {
                    return new Result(process.exitValue(), new String(output.toByteArray(), StandardCharsets.UTF_8));
                }
            } finally {
                if (interrupted) Thread.currentThread().interrupt();
            }
        }
    }
}
