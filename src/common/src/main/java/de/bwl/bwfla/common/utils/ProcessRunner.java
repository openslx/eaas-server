package de.bwl.bwfla.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A wrapper around Process which combines features of ProcessBuilder.
 * 
 * ProcessRunner is a subclass of Process itself and is completely compatible
 * with the Process API. You can read up the Process javadoc to learn how to use
 * a Process. In addition, it implements features of a ProcessBuilder, e.g. it
 * is possible to start the Process from the ProcessRunner class and to add
 * command line arguments.
 * 
 * Due to the combination of ProcessBuilder and Process, some methods can only
 * be called if the process is already started. If these methods are called
 * before the process was started, an IllegalThreadStateException is thrown (in
 * the spirit of Process.exitValue()).
 * 
 * The InputStreams returned by getInputStream() and getErrorStream() are
 * unique, meaning that those methods can be invoked multiple times and all
 * returned streams can be read from independently (even concurrently).
 * 
 * Thread safety: With the exception of the mentioned InputStreams, this class
 * is not more thread-safe than Process or ProcessBuilder. Simultaneous access
 * from two threads without synchronization will probably result in strange
 * behaviour. This is also the case for the process' OutputStream!
 * 
 * Implementation note: When the process is started, its two output streams
 * (stdout and stderr) are redirected to a temporary file which is immediately
 * deleted. As POSIX guarantees that files remain accessible as long as there is
 * an open file handle, two instances of RandomAccessFile are created and stored
 * for the whole lifetime of the ProcessRunner instance. This allows to access
 * the process' output streams even after the process has terminated without
 * storing the complete output in memory.
 * 
 * This also makes it necessary to release this permanent resource. Hence,
 * ProcessRunner also implements AutoClosable interface. You should always call
 * close() when you are finished with the process or, better yet, use a
 * try-with-statement.
 * 
 * @author Thomas Liebetraut
 */
public class ProcessRunner extends Process implements AutoCloseable {
    private final String tempfilePrefix = "eaas-processrunner-";

    Logger log = Logger.getLogger(this.getClass().getName());

    private ProcessBuilder processBuilder = new ProcessBuilder();
    private Process process = null;

    private RandomAccessFile stdout = null;
    private RandomAccessFile stderr = null;

    public ProcessRunner() {
        this(new ArrayList<Object>());
    }

    public ProcessRunner(Object... cmd) {
        this(Arrays.asList(cmd));
    }

    public ProcessRunner(List<Object> cmd) {
        processBuilder.command(cmd.stream().map(Object::toString)
                .collect(Collectors.toList()));
    }

    public List<String> command() {
        return processBuilder.command();
    }

    public ProcessRunner command(Object... cmd) {
        return this.command(Arrays.asList(cmd));
    }

    public ProcessRunner command(List<Object> cmd) {
        processBuilder.command(cmd.stream().map(Object::toString)
                .collect(Collectors.toList()));
        return this;
    }

    public ProcessRunner addArguments(Object... args) {
        return this.addArguments(Arrays.asList(args));
    }


    public ProcessRunner addArguments(List<Object> args) {
        processBuilder.command().addAll(args.stream().map(Object::toString)
                .collect(Collectors.toList()));
        return this;
    }

    public ProcessRunner setEnvironmentVariables(
            Map<String, String> environment) {
        this.processBuilder.environment().putAll(environment);
        return this;
    }

    public ProcessRunner setEnvironmentVariable(String name, String value) {
        this.processBuilder.environment().put(name, value);
        return this;
    }

    public ProcessRunner setWorkingDirectory(Path cwd) {
        processBuilder.directory(cwd.toFile());
        return this;
    }

    /** Returns the current command as string. */
    public String getCommandString()
    {
        final List<String> command = this.command();
        if (command.isEmpty())
            return "";

        final StringBuilder sbuilder = new StringBuilder(512);
        for (String arg : command) {
            sbuilder.append(arg);
            sbuilder.append(' ');
        }

        final int last = sbuilder.length() - 1;
        return sbuilder.substring(0, last);
    }

    /**
     * Get the Unix pid of this process.
     * 
     * @return The process' Unix process id
     * @throws IllegalThreadStateException if the thread is not running
     * @throws RuntimeException if there is a severe error in how the JVM works
     * @throws IllegalArgumentException if we are not running natively on a Unix
     *             system
     */
    public int getProcessId() throws IllegalThreadStateException {
        if (process == null) {
            throw new IllegalThreadStateException("Process is not running");
        }

        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            try {
                Class<? extends Process> proc = process.getClass();
                Field field = proc.getDeclaredField("pid");
                field.setAccessible(true);
                Object pid = field.get(process);
                return (Integer) pid;
            } catch (NoSuchFieldException e) {
                // We already checked the class instance, so this exception
                // can only occur if there is fundamental change in how
                // the JDK works. In this case, this code is obsolete, anyway.
                // Therefore, ignore this exception
            } catch (IllegalAccessException e) {
                // We set the field to visible, so this exception can only
                // occur if there is fundamental change in how the JDK works.
                // In this case, this code is obsolete, anyway.
                // Therefore, ignore this exception
            }
            throw new RuntimeException(
                    "Not able to get the pid of a UNIXProcess. Did you update your JVM? Please re-evaluate how to get the process id of a java.lang.Process.");
        } else {
            throw new IllegalArgumentException(
                    "Process is not a java.lang.UNIXProcess");
        }
    }

    public int run() throws IOException, InterruptedException {
        this.start();
        return this.waitFor();
    }
    
    /**
     * Tries to run() the process, ignoring any client error that might occur.
     * If you need to determine the reason why the process did *not* run
     * successfully, you should use run() instead.
     * 
     * @return the exit code of the process or 1 for any internal error
     */
    public int tryRun() {
        try {
            this.start();
            return this.waitFor();
        } catch (IOException | IllegalThreadStateException ignore) {
        } catch (InterruptedException e) {
            // we were asked to interrupt the current operation altogether
            this.destroyForcibly();
        }
        return 1;
    }

    public void stop() {
        this.process.destroy();
    }

    public void kill() {
        this.process.destroyForcibly();
    }

    public boolean isAlive() throws IllegalThreadStateException {
        if (process == null) {
            throw new IllegalThreadStateException("Process is not created yet");
        }

        return this.process.isAlive();
    }

    public void start() throws NullPointerException, IndexOutOfBoundsException,
            SecurityException, IOException {
        File stdoutFile = null;
        File stderrFile = null;
        try {
            stdoutFile = Files
                    .createTempFile(tempfilePrefix, "-stdout")
                    .toFile();
            stderrFile = Files
                    .createTempFile(tempfilePrefix, "-stderr")
                    .toFile();

            this.processBuilder.redirectOutput(stdoutFile);
            this.processBuilder.redirectError(stderrFile);

            final Logger log = Logger.getLogger(this.getClass().getName());
            log.info("Starting subprocess:  " + this.getCommandString());
            this.process = processBuilder.start();

            this.stdout = new RandomAccessFile(stdoutFile, "r");
            this.stderr = new RandomAccessFile(stderrFile, "r");
        }
        catch (Throwable error) {
            log.log(Level.SEVERE, error.getMessage(), error);
            throw error;
        }
        finally {
            if (stdoutFile != null) {
                Files.delete(stdoutFile.toPath());
            }
            if (stderrFile != null) {
                Files.delete(stderrFile.toPath());
            }
        }
    }

    @Override
    public int exitValue() throws IllegalThreadStateException {
        if (this.process == null) {
            throw new IllegalThreadStateException("Process is not created yet");
        }
        return this.process.exitValue();
    }

    @Override
    public InputStream getErrorStream() {
        return new ProcessInputStream(this.stderr, this.process);
    }

    @Override
    public InputStream getInputStream() {
        return new ProcessInputStream(this.stdout, this.process);
    }

    @Override
    public OutputStream getOutputStream() {
        return this.process.getOutputStream();
    }

    public InputStream getStderrStream() {
        return this.getErrorStream();
    }

    public InputStream getStdoutStream() {
        return this.getInputStream();
    }

    public OutputStream getStdinStream() {
        return this.getOutputStream();
    }

    public String getStdoutString(Charset charset) throws IOException {
        this.stdout.seek(0);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        InputStream is = this.getInputStream();
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(charset.name());
    }

    public String getStdoutString() throws IOException {
        return getStdoutString(StandardCharsets.UTF_8);
    }

    public String getStderrString(Charset charset) throws IOException {
        this.stderr.seek(0);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        InputStream is = this.getErrorStream();
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(charset.name());
    }

    public String getStderrString() throws IOException {
        return getStderrString(StandardCharsets.UTF_8);
    }

    @Override
    public int waitFor()
            throws InterruptedException, IllegalThreadStateException {
        if (process == null) {
            throw new IllegalThreadStateException("Process is not created yet");
        }

        return this.process.waitFor();
    }

    @Override
    public boolean waitFor(long timeout, TimeUnit unit)
            throws InterruptedException, IllegalThreadStateException {
        if (process == null) {
            throw new IllegalThreadStateException("Process is not created yet");
        }

        return this.process.waitFor(timeout, unit);
    }

    public boolean waitFor(Duration duration)
            throws InterruptedException, IllegalThreadStateException {
        return this.waitFor(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Just a quick reminder that this method does NOT release all resources.
     * 
     * @see close()
     */
    @Override
    public void destroy() {
        this.process.destroy();
    }

    @Override
    public Process destroyForcibly() {
        return this.process.destroyForcibly();
    }

    /**
     * Closes this resource, relinquishing any underlying resources. This method
     * is invoked automatically on objects managed by the try-with-resources
     * statement.
     */
    @Override
    public void close() {
        try {
            this.destroyForcibly();
        } catch (Exception e) {
        }
        try {
            this.stdout.close();
        } catch (Exception e) {
        }
        try {
            this.stderr.close();
        } catch (Exception e) {
        }
    }

    private class ProcessInputStream extends InputStream {
        private static final int DEFAULT_DELAY_MILLIS = 100;

        private final RandomAccessFile backingFile;
        private final Process process;
        private int pos = 0;

        public ProcessInputStream(RandomAccessFile backingFile,
                Process process) {
            super();
            this.backingFile = backingFile;
            this.process = process;
        }

        private void waitForData() throws IOException {
            while (process.isAlive() && this.available() == 0) {
                try {
                    Thread.sleep(DEFAULT_DELAY_MILLIS);
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }
        }

        @Override
        public int available() throws IOException {
            try {
                long available = this.backingFile.length() - this.pos;
                if (available <= Integer.MAX_VALUE) {
                    return (int) available;
                } else {
                    throw new ArithmeticException();
                }
            } catch (java.nio.channels.ClosedChannelException e) {
                return 0;
            }
        }

        @Override
        public int read() throws IOException {
            waitForData();

            // waitForData() terminates on two conditions
            // - there is data in the buffer
            // - the process has terminated and there is no more data

            // in both cases we can invoke super.read() because it will return
            // -1 (EOF) on the (empty) stream in the second case
            synchronized (this.backingFile) {
                this.backingFile.seek(this.pos);
                int result = this.backingFile.read();
                if (result >= 0) {
                    this.pos += result;
                }
                return result;
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {

            // This input argument check is taken from JDK's InputStream.java
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            waitForData();

            // waitForData() terminates on two conditions
            // - there is data in the buffer
            // - the process has terminated and there is no more data

            // in both cases we can invoke super.read(byte[], int, int) because
            // it will return -1 (EOF) on the (empty) stream in the second case

            synchronized (this.backingFile) {
                this.backingFile.seek(this.pos);
                int read = this.backingFile.read(b, off, len);
                if (read >= 0) {
                    this.pos += read;
                }
                return read;
            }
        }
    }
    
//  private class ProcessInputStream extends FilterInputStream {
//  private static final int DEFAULT_DELAY_MILLIS = 500;
//
//  private Process process;
//
//  public ProcessInputStream(FileInputStream backingFile, Process process)
//          throws IOException {
//      super(backingFile);
//      this.process = process;
//  }
//
//  /*
//   * @return True if there really is data to be read, false if the process
//   * has terminated without data
//   */
//  private void waitForData() throws IOException {
//      while (this.available() == 0 && process.isAlive()) {
//          try {
//              Thread.sleep(DEFAULT_DELAY_MILLIS);
//          } catch (InterruptedException e) {
//              throw new InterruptedIOException();
//          }
//      }
//  }
//
//  @Override
//  public int read() throws IOException {
//      waitForData();
//
//      // waitForData() terminates on two conditions
//      // - there is data in the buffer
//      // - the process has terminated and there is no more data
//
//      // in both cases we can invoke super.read() because it will return
//      // -1 (EOF) on the (empty) stream in the second case
//      return super.read();
//  }
//
//  @Override
//  public int read(byte[] b, int off, int len) throws IOException {
//      // This input argument check is taken from JDK's InputStream.java
//      if (b == null) {
//          throw new NullPointerException();
//      } else if (off < 0 || len < 0 || len > b.length - off) {
//          throw new IndexOutOfBoundsException();
//      } else if (len == 0) {
//          return 0;
//      }
//
//      waitForData();
//
//      // waitForData() terminates on two conditions
//      // - there is data in the buffer
//      // - the process has terminated and there is no more data
//
//      // in both cases we can invoke super.read(byte[], int, int) because
//      // it will return -1 (EOF) on the (empty) stream in the second case
//
//      return super.read(b, off, len);
//  }
//}
}
