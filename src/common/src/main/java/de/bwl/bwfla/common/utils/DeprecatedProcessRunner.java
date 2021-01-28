/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.bwl.bwfla.conf.CommonSingleton;


/**
 * Native command executor, based on {@link ProcessBuilder}.
 * <p/>
 * 
 * <b>NOTE:</b> This implementation currently only works with Linux-based systems!
 */
@Deprecated
public class DeprecatedProcessRunner
{
	/** Logger instance. */
	private Logger log = Logger.getLogger(DeprecatedProcessRunner.class.getName());

	// Member fields
	private final StringBuilder sbuilder;
	private final Map<String, String> environment;
	private final List<String> command;
	private Process process;
	private ProcessMonitor monitor;
	private ProcessOutput stdout;
	private ProcessOutput stderr;
	private boolean redirectStdErrToStdOut = false;
	private int pid;
	private Path workdir;
	private Path outdir;
	private final AtomicInteger numWaitingCallers;
	private volatile State state;

	/** Internal states */
	private enum State
	{
		INVALID,
		READY,
		STARTED,
		STOPPED
	}

	/** PID, representing an invalid process. */
	private static final int INVALID_PID = -1;

	/** Default capacity of the command-builder. */
	private static final int DEFAULT_CMDBUILDER_CAPACITY = 32;

	/** Exception's message */
	private static final String MESSAGE_IOSTREAM_NOT_AVAILABLE = "IO-stream is not available. Process was not started properly!";

	/**
	 * Regex for checking validity of environment variable names, which must match
	 * <a href="https://pubs.opengroup.org/onlinepubs/9699919799/utilities/V3_chap02.html#tag_18_10_02">ASSIGNMENT_WORD</a>.
	 */
	private static final Predicate<String> ENVIRONMENT_VARNAME_MATCHER = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$")
			.asMatchPredicate();

	// Initialize the constants from property file
	private static final Path PROPERTY_TMPDIR_BASE = Paths.get(CommonSingleton.runnerConf.tmpBaseDir);
	private static final String PROPERTY_TMPDIR_PREFIX = CommonSingleton.runnerConf.tmpdirPrefix;
	private static final String PROPERTY_STDOUT_FILENAME = CommonSingleton.runnerConf.stdoutFilename;
	private static final String PROPERTY_STDERR_FILENAME = CommonSingleton.runnerConf.stderrFilename;


	/** Create a new ProcessRunner. */
	public DeprecatedProcessRunner()
	{
		this(DEFAULT_CMDBUILDER_CAPACITY);
	}

	/** Create a new ProcessRunner. */
	public DeprecatedProcessRunner(int cmdCapacity)
	{
		this.sbuilder = new StringBuilder(1024);
		this.environment = new HashMap<String, String>();
		this.command = new ArrayList<String>(cmdCapacity);
		this.numWaitingCallers = new AtomicInteger(0);

		this.reset(true);
	}

	/** Create a new ProcessRunner with the specified command. */
	public DeprecatedProcessRunner(String cmd)
	{
		this(DEFAULT_CMDBUILDER_CAPACITY);
		this.setCommand(cmd, true);
	}

	/** Creates a new ProcessRunner piping ...runners together using /bin/sh. */
	public static DeprecatedProcessRunner pipe(DeprecatedProcessRunner... runners)
	{
		return DeprecatedProcessRunner.pipe(Arrays.asList(runners));
	}

	/** Creates a new ProcessRunner piping ...runners together using /bin/sh. */
	public static DeprecatedProcessRunner pipe(Collection<DeprecatedProcessRunner> runners)
	{
		final String args = runners.stream()
				.map(DeprecatedProcessRunner::getCommandStringWithEnv)
				.collect(Collectors.joining(" | "));

		return new DeprecatedProcessRunner("/bin/sh")
				.addArgument("-c")
				.addArgument(args);
	}

	/** Set a new logger. */
	public DeprecatedProcessRunner setLogger(Logger log)
	{
		if(log != null)
			this.log = log;
		return this;
	}

	/** Define a new command, resetting this runner. */
	public DeprecatedProcessRunner setCommand(String cmd)
	{
		return this.setCommand(cmd, false);
	}

	/**
	 * Define a new command to run in a subprocess.
	 * @param cmd The new command to execute.
	 * @param keepenv If true, then the current environment variables will be reused, else cleared.
	 */
	public DeprecatedProcessRunner setCommand(String cmd, boolean keepenv)
	{
		DeprecatedProcessRunner.ensureNotEmpty(cmd);

		if (state != State.INVALID)
			throw new IllegalStateException("ProcessRunner was not stopped/cleaned correctly!");

		this.reset(keepenv);
		command.add(cmd);

		state = State.READY;
		return this;
	}

	/**
	 * Add a new argument to current command, separated by a space.
	 * @param arg The argument to add.
	 */
	public DeprecatedProcessRunner addArgument(String arg)
	{
		DeprecatedProcessRunner.ensureNotNull(arg);
		this.ensureStateReady();
		command.add(arg);
		return this;
	}

	/**
	 * Compose a new argument from multiple values and add it to current command.
	 * @param values The values to build the argument from.
	 */
	public DeprecatedProcessRunner addArgument(String... values)
	{
		this.ensureStateReady();

		sbuilder.setLength(0);
		for (String value : values) {
			DeprecatedProcessRunner.ensureNotNull(value);
			sbuilder.append(value);
		}

		command.add(sbuilder.toString());
		return this;
	}

	/**
	 * Append a new argument's value to last argument.
	 * @param value The argument's value to add.
	 */
	public DeprecatedProcessRunner addArgValue(String value)
	{
		DeprecatedProcessRunner.ensureNotNull(value);
		this.ensureStateReady();

		final int index = command.size() - 1;
		String argument = command.get(index);
		command.set(index, argument + value);
		return this;
	}

	/**
	 * Append new argument's values to last argument.
	 * @param values The argument's values to add.
	 */
	public DeprecatedProcessRunner addArgValues(String... values)
	{
		this.ensureStateReady();

		sbuilder.setLength(0);
		for (String value : values) {
			DeprecatedProcessRunner.ensureNotNull(value);
			sbuilder.append(value);
		}

		final int index = command.size() - 1;
		String argument = command.get(index);
		argument += sbuilder.toString();
		command.set(index, argument);
		return this;
	}

	/**
	 * Add all arguments from the specified list to current command.
	 * @param args The arguments to add.
	 */
	public DeprecatedProcessRunner addArguments(String... args)
	{
		return this.addArguments(Arrays.asList(args));
	}

	/**
	 * Add all arguments from the specified list to current command.
	 * @param args The arguments to add.
	 */
	public DeprecatedProcessRunner addArguments(List<String> args)
	{
		this.ensureStateReady();

		for (String arg : args) {
			DeprecatedProcessRunner.ensureNotNull(arg);
			command.add(arg);
		}

		return this;
	}

	/**
	 * Add a new environment-variable to current command.
	 * @param var The variable's name.
	 * @param value The variable's value.
	 */
	public DeprecatedProcessRunner addEnvVariable(String var, String value)
	{
		DeprecatedProcessRunner.ensureNotEmpty(var);
		DeprecatedProcessRunner.ensureValidEnvVarName(var);
		DeprecatedProcessRunner.ensureNotNull(value, "Value for environment variable " + var + " is null.");
		environment.put(var, value);
		return this;
	}


	/**
	 * Add all environment-variables to current command.
	 * @param vars The variables to add.
	 */
	public DeprecatedProcessRunner addEnvVariables(Map<String, String> vars)
	{
		vars.forEach(this::addEnvVariable);
		return this;
	}

	/** Returns the environment variables of the current command. */
	public Map<String, String> getEnvVariables()
	{
		return environment;
	}

	/** Returns the current command. */
	public List<String> getCommand()
	{
		return command;
	}

	/** Quotes a single argument for shell use. */
	static private String quoteArg(String arg) {
		return "'" + arg.replace("'", "'\\''") + "'";
	}

	/** Returns the current command as string in shell syntax. */
	public String getCommandString()
	{
		if (command.isEmpty())
			return "";

		sbuilder.setLength(0);
		for (String arg : command) {
			sbuilder.append(quoteArg(arg));
			sbuilder.append(' ');
		}

		int last = sbuilder.length() - 1;
		return sbuilder.substring(0, last);
	}

	/** Returns the current environment variables as string in shell syntax. */
	public String getEnvString()
	{
		StringBuilder builder = new StringBuilder(1024);
		if(environment.entrySet().isEmpty())
			return "";

		for (Map.Entry<String, String> entry : environment.entrySet()) {
			final String key = entry.getKey();
			final String value = entry.getValue();

			DeprecatedProcessRunner.ensureValidEnvVarName(key);

			builder.append(key);
			builder.append("=");
			builder.append(quoteArg(value));
			builder.append(" ");
		}

		int last = builder.length() - 1;
		return builder.substring(0, last);
	}

	/** Returns the current command including environment variables as string in shell syntax. */
	public String getCommandStringWithEnv()
	{
		final String env = this.getEnvString();
		final String command = this.getCommandString();
		return env + (env.length() > 0 ? " " : "") + command;
	}

	/** Returns the monitor for the running subprocess. */
	public ProcessMonitor getProcessMonitor()
	{
		if (state != State.STARTED)
			throw new IllegalStateException("Monitor is not available. Process was not started properly!");

		if (monitor == null) {
			try {
				// Try to create the monitor
				monitor = new ProcessMonitor(pid);
			}
			catch (FileNotFoundException e) {
				// Likely the process was already terminated!
				log.warning("Creating monitor for subprocess " + pid + " failed!");
			}
		}

		return monitor;
	}

	/**
	 * The directory to be used as working directory.
	 * If not set, uses the dir of the current process.
	 * @param dir The new working directory.
	 */
	public DeprecatedProcessRunner setWorkingDirectory(Path dir)
	{
		this.ensureStateReady();
		workdir = dir;
		return this;
	}

	/** Redirect the stderr of the process to stdout. */
	public DeprecatedProcessRunner redirectStdErrToStdOut(boolean redirect)
	{
		this.redirectStdErrToStdOut = redirect;
		return this;
	}

	/** Returns the stdin of the process, as byte-stream. */
	public OutputStream getStdInStream() throws IOException
	{
		if (!(state == State.STARTED || state == State.STOPPED))
			throw new IllegalStateException();

		DeprecatedProcessRunner.ensureNotNull(process, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return process.getOutputStream();
	}

	/** Returns the stdin of the process, as char-stream. */
	public Writer getStdInWriter() throws IOException
	{
		OutputStream stream = this.getStdInStream();
		return new OutputStreamWriter(stream);
	}

	/**
	 * Write a string to stdin of the subprocess. <p/>
	 * <b>NOTE:</b>
	 *     For writing multiple messages it is more efficient to get the writer
	 *     returned by {@link #getStdInWriter()} once and write to it directly!
	 *
	 * @param message The data to write.
	 */
	public DeprecatedProcessRunner writeToStdIn(String message) throws IOException
	{
		Writer writer = this.getStdInWriter();
		writer.write(message);
		writer.flush();
		return this;
	}

	/** Returns the stdout of the process, as byte-stream. */
	public InputStream getStdOutStream() throws IOException
	{
		DeprecatedProcessRunner.ensureNotNull(stdout, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stdout.stream();
	}

	/** Returns the stdout of the process, as char-stream. */
	public Reader getStdOutReader() throws IOException
	{
		DeprecatedProcessRunner.ensureNotNull(stdout, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stdout.reader();
	}

	/** Returns the stdout of the process, as string. */
	public String getStdOutString() throws IOException
	{
		DeprecatedProcessRunner.ensureNotNull(stdout, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stdout.string();
	}

	/** Returns the path to file containing stdout of the process. */
	public Path getStdOutPath()
	{
		DeprecatedProcessRunner.ensureNotNull(stdout, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stdout.path();
	}

	/** Returns the stderr of the process, as byte-stream. */
	public InputStream getStdErrStream() throws IOException
	{
		DeprecatedProcessRunner.ensureNotNull(stderr, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stderr.stream();
	}

	/** Returns the stderr of the process, as char-stream. */
	public Reader getStdErrReader() throws IOException
	{
		DeprecatedProcessRunner.ensureNotNull(stderr, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stderr.reader();
	}

	/** Returns the stderr of the process, as string. */
	public String getStdErrString() throws IOException
	{
		DeprecatedProcessRunner.ensureNotNull(stderr, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stderr.string();
	}

	/** Returns the path to file containing stderr of the process. */
	public Path getStdErrPath()
	{
		DeprecatedProcessRunner.ensureNotNull(stderr, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stderr.path();
	}

	/** Print the stdout of the process to the log. */
	public DeprecatedProcessRunner printStdOut()
	{
		// Print stdout, if available
		try
		{
			String output = this.getStdOutString();
			this.printStdOut(output);
		}
		catch(IOException error) {
			log.log(Level.WARNING, "Printing process-runner's stdout failed!", error);
		}

		return this;
	}

	/** Print the stderr of the process to the log. */
	public DeprecatedProcessRunner printStdErr()
	{
		try
		{
			// Print stderr, if available
			String output = this.getStdErrString();
			this.printStdErr(output);
		}
		catch(IOException error) {
			log.log(Level.WARNING, "Printing process-runner's stderr failed!", error);
		}

		return this;
	}

	/**
	 * Get the return-code of the process. Can be
	 * called only after the process terminates!
	 */
	public int getReturnCode()
	{
		if (state != State.STOPPED)
			throw new IllegalStateException("Return code is available only after process termination!");

		return process.exitValue();
	}

	/** Get the ID of the process. */
	public int getProcessId()
	{
		return pid;
	}

	/** Returns true when this runner represents a valid process, else false. */
	public boolean isProcessValid()
	{
		return (state == State.STARTED || state == State.STOPPED);
	}

	/** Returns true when the process is in a running state, else false. */
	public boolean isProcessRunning()
	{
		if (state != State.STARTED)
			return false;

		return process.isAlive();
	}

	/** Returns true when the process has finished execution, else false. */
	public boolean isProcessFinished()
	{
		return !this.isProcessRunning();
	}

	/**
	 * Start the process, that is represented by this runner.
	 * @return true when the start was successful, else false.
	 */
	public boolean start()
	{
		if (state != State.READY)
			throw new IllegalStateException("Process not ready to start!");

		// Create the temp-directory for process' output
		try {
			outdir = Files.createTempDirectory(PROPERTY_TMPDIR_BASE, PROPERTY_TMPDIR_PREFIX).toAbsolutePath();
			stdout = new ProcessOutput(outdir.resolve(PROPERTY_STDOUT_FILENAME));
			stderr = new ProcessOutput(outdir.resolve(PROPERTY_STDERR_FILENAME));
		}
		catch (IOException exception) {
			String message = "Could not create a temporary directory for a new subprocess.";
			throw new RuntimeException(message, exception);
		}

		// Prepare the process to run
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.environment().putAll(environment);

		// Set working directory
		if (workdir != null)
			builder.directory(workdir.toFile());

		// Setup stdout + stderr redirection
		builder.redirectOutput(stdout.file());
		builder.redirectError(stderr.file());
		if (redirectStdErrToStdOut)
			builder.redirectErrorStream(true);

		// Finally start the process
		try {
			process = builder.start();
			pid = DeprecatedProcessRunner.lookupUnixPid(process);
			log.info("Subprocess " + pid + " started:  " + this.getCommandString());
		}
		catch (IOException exception) {
			log.log(Level.SEVERE, "Starting new subprocess failed! CMD was: " + this.getCommandString(), exception);
			this.cleanup();
			return false;
		}

		state = State.STARTED;
		return true;
	}

	/**
	 * Block and wait for a running process to finish.
	 * @return The return-code of the terminated process.
	 */
	public int waitUntilFinished()
	{
		if (state != State.STARTED && state != State.STOPPED) {
			String message = "Waiting is not possible. Process was not started/stopped properly!";
			throw new IllegalStateException(message);
		}

		// First waiting caller?
		final boolean isFirstCaller = numWaitingCallers.incrementAndGet() == 1;
		if (isFirstCaller)
			log.info("Waiting for subprocess " + pid + " to finish...");

		// Wait for the process termination
		int retcode = -1;
		try {
			retcode = process.waitFor();
		}
		catch (InterruptedException e) {
			// Ignore it!
		}

		// Fist waiting caller?
		if (isFirstCaller) {
			log.info("Subprocess " + pid + " terminated with code " + retcode);
			state = State.STOPPED;
		}

		return retcode;
	}

	/**
	 * Block and wait for a running process to finish.
	 * @return true if prcess terminated, else false.
	 */
	public boolean waitUntilFinished(long timeout, TimeUnit unit)
	{
		if (state != State.STARTED && state != State.STOPPED) {
			String message = "Waiting is not possible. Process was not started/stopped properly!";
			throw new IllegalStateException(message);
		}

		// First waiting caller?
		final boolean isFirstCaller = numWaitingCallers.incrementAndGet() == 1;
		if (isFirstCaller)
			log.info("Waiting for subprocess " + pid + " to finish...");

		// Wait for the process termination
		boolean exited = false;
		try {
			exited = process.waitFor(timeout, unit);
		}
		catch (InterruptedException e) {
			// Ignore it!
		}

		// Fist waiting caller?
		if (isFirstCaller) {
			log.info("Subprocess " + pid + " terminated!");
			state = State.STOPPED;
		}

		return exited;
	}

	/** Stop the running process and wait for termination. */
	public DeprecatedProcessRunner stop()
	{
		if (state != State.STARTED)
			return this;

		log.info("Stopping subprocess " + pid + "...");
		process.destroy();

		this.waitUntilFinished();
		return this;
	}

	/** Stop the running process and wait for termination. */
	public DeprecatedProcessRunner stop(long timeout, TimeUnit unit)
	{
		if (state != State.STARTED)
			return this;

		log.info("Stopping subprocess " + pid + "...");
		process.destroy();

		this.waitUntilFinished(timeout, unit);
		return this;
	}

	/** Kill the running process. */
	public DeprecatedProcessRunner kill()
	{
		if (state != State.STARTED)
			return this;

		log.info("Killing subprocess " + pid + "...");
		process.destroyForcibly();
		return this;
	}

	/** Perform the cleanup of process' temp-directory. */
	public void cleanup()
	{
		if (this.isProcessRunning())
			throw new IllegalStateException("Attempt to cleanup a running process!");

		try {
			if(monitor != null)
				monitor.stop();
		}
		catch (IOException exception) {
			log.log(Level.WARNING, "Stopping process-monitor failed!", exception);
		}

		// Close all io-streams
		this.cleanup(stdout);
		this.cleanup(stderr);

		try {
			// Delete created files
			if (outdir != null)
				Files.deleteIfExists(outdir);
		}
		catch (IOException exception) {
			log.log(Level.WARNING, "Cleanup of process-output directory failed!", exception);
		}

		state = State.INVALID;
	}


	/* ==================== Helper Methods ==================== */

	/**
	 * Start this process and wait for it to finish.
	 * When process terminates, print stdout/stderr and perform cleanup-operations.
	 * @return true when the return-code of the terminated process is 0, else false.
	 */
	public boolean execute()
	{
		return this.execute(true);
	}

	/**
	 * Start this process and wait for it to finish.
	 * @param verbose If set to true, then print stdout and stderr when process terminates.
	 * @return true when the return-code of the terminated process is 0, else false.
	 */
	public boolean execute(boolean verbose)
	{
		if (!this.start())
			return false;

		final int retcode = this.waitUntilFinished();

		if (verbose) {
			try {
				this.printStdOut();
				this.printStdErr();
			}
			catch (Exception exception) {
				log.log(Level.WARNING, "Printing process-runner's stdout/err failed!", exception);
			}
		}

		this.cleanup();

		return (retcode == 0);
	}

	/** Execute this process and return its stdout + stderr. */
	public Optional<Result> executeWithResult() throws IOException
	{
		return this.executeWithResult(true);
	}

	/**
	 * Execute this process and return its stdout + stderr.
	 * @param verbose If set to true, then additionally log stdout and stderr when process terminates.
	 */
	public Optional<Result> executeWithResult(boolean verbose) throws IOException
	{
		if (!this.start())
			return Optional.empty();

		try {
			final int retcode = this.waitUntilFinished();
			final String stdout = this.getStdOutString();
			final String stderr = this.getStdErrString();
			if (verbose) {
				this.printStdOut(stdout);
				this.printStdErr(stderr);
			}

			return Optional.of(new Result(retcode, stdout, stderr));
		}
		finally {
			this.cleanup();
		}
	}


	/** Result of a process execution */
	public static class Result
	{
		private final int retcode;
		private final String stdout;
		private final String stderr;

		public Result()
		{
			this(-1, null, null);
		}

		public Result(int retcode, String stdout, String stderr)
		{
			this.retcode = retcode;
			this.stdout = stdout;
			this.stderr = stderr;
		}

		public boolean successful()
		{
			return (retcode == 0);
		}

		public int code()
		{
			return retcode;
		}

		public String stdout()
		{
			return stdout;
		}

		public String stderr()
		{
			return stderr;
		}
	}


	/* ==================== Internal Methods ==================== */

	private static int lookupUnixPid(Process process)
	{
		return (int) process.pid();
	}

	private void printStdOut(String output)
	{
		if (output.isEmpty())
			return;

		log.info("Subprocess " + this.getProcessId() + " stdout:\n" + output);
	}

	private void printStdErr(String output)
	{
		if (output.isEmpty())
			return;

		log.info("Subprocess " + this.getProcessId() + " stderr:\n" + output);
	}

	private void reset(boolean keepenv)
	{
		if (!keepenv)
			environment.clear();

		command.clear();

		workdir = null;
		process = null;
		monitor = null;
		outdir = null;
		stdout = null;
		stderr = null;

		pid = INVALID_PID;
		numWaitingCallers.set(0);
		state = State.INVALID;
	}

	private void cleanup(ProcessOutput output)
	{
		if (output == null)
			return;

		try {
			output.close();
		}
		catch (Exception exception) {
			log.log(Level.WARNING, "Closing process-output failed!", exception);
		}

		try {
			output.cleanup();
		}
		catch (Exception exception) {
			log.log(Level.WARNING, "Cleanup of process-output failed!", exception);
		}
	}

	private void ensureStateReady()
	{
		if (state != State.READY)
			throw new IllegalStateException("No command specified!");
	}

	private static void ensureNotNull(Object object, String message)
	{
		if (object == null)
			throw new IllegalStateException(message);
	}

	private static void ensureNotNull(Object object) {
		if (object == null)
			throw new IllegalStateException("Argument is null!");
	}

	private static void ensureNotEmpty(String arg)
	{
		if (arg == null || arg.isEmpty())
			throw new IllegalArgumentException("Argument is null or empty!");
	}

	private static void ensureValidEnvVarName(String name)
	{
		if (!ENVIRONMENT_VARNAME_MATCHER.test(name))
			throw new IllegalStateException("Environment variable name contains invalid characters: " + name);
	}
}


final class ProcessOutput
{
	private final Path outpath;
	private InputStream outstream;

	ProcessOutput(Path path)
	{
		this.outpath = path;
		this.outstream = null;
	}

	public Path path()
	{
		return outpath;
	}

	public File file()
	{
		return outpath.toFile();
	}

	public InputStream stream() throws IOException
	{
		if (outstream == null)
			outstream = Files.newInputStream(outpath);

		return outstream;
	}

	public Reader reader() throws IOException
	{
		InputStream stream = this.stream();
		return new InputStreamReader(stream);
	}

	public String string() throws IOException
	{
		StringBuilder builder = new StringBuilder(1024);
		char[] buffer = new char[512];
		try (Reader reader = this.reader()) {
			while (reader.ready()) {
				int length = reader.read(buffer);
				if (length < 0)
					break;  // End-of-stream

				builder.append(buffer, 0, length);
			}
		}

		return builder.toString();
	}

	public void close() throws IOException
	{
		if (outstream != null)
			outstream.close();
	}

	public void cleanup() throws IOException
	{
		Files.deleteIfExists(outpath);
	}

	public boolean exists()
	{
		return Files.exists(outpath);
	}
}
