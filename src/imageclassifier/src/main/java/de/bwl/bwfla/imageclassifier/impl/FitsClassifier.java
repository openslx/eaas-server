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

package de.bwl.bwfla.imageclassifier.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.imageclassifier.datatypes.Classifier;
import de.bwl.bwfla.imageclassifier.datatypes.IdentificationOutputIndex;
import edu.harvard.hul.ois.fits.FitsMetadataElement;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;


import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.consolidation.ToolOutputConsolidator;
import edu.harvard.hul.ois.fits.exceptions.FitsException;
import edu.harvard.hul.ois.fits.tools.Tool;
import edu.harvard.hul.ois.fits.tools.ToolBelt;
import edu.harvard.hul.ois.fits.tools.ToolOutput;

/** An optimized wrapper class for the FITS-Tool. */
public class FitsClassifier extends Classifier<FitsOutput> {
	// Member fields
	private final ExecutorService executor;
	private final Logger log;

	private static final Configuration CONFIG = ConfigurationProvider.getConfiguration();

	/** Cache for the internal Fits tools */
	private static final FitsProcessorPool FITS_PROCESSORS = new FitsProcessorPool();

	/** Constructor */
	public FitsClassifier(ExecutorService executor) throws FitsException {
		this(executor, Logger.getLogger(FitsClassifier.class.getName()));
	}

	/** Constructor */
	public FitsClassifier(ExecutorService executor, Logger log) throws FitsException {
		this.executor = executor;
		this.log = log;
	}

	/**
	 * Classifies files under the specified path using FITS.
	 * 
	 * @param input The file or directory to classify.
	 * @param verbose If true, then more log-messages will be printed.
	 * @return A list of results
	 */
	public List<FitsOutput> classify(Path input, boolean verbose) throws IOException {
		log.info("Analyzing file(s) at '" + input.toString() + "'...");

		final ArrayList<FitsOutput> results = new ArrayList<FitsOutput>();
		final FitsProcessor fits = FITS_PROCESSORS.get();
		int numOutputsFailed = 0;
		if (!Files.isDirectory(input)) {
			// Input is a single file
			FitsOutput output = fits.process(input, log, verbose);
			if (output != null)
				results.add(output);
			else
				++numOutputsFailed;
		} else {
			// Input is a directory, process all files inside it
			final ConcurrentLinkedQueue<FitsTaskOutput> outputs = new ConcurrentLinkedQueue<FitsTaskOutput>();
			final ConcurrentLinkedQueue<Path> paths = new ConcurrentLinkedQueue<Path>();
			final FileVisitor visitor = new FileVisitor(paths);
			Files.walkFileTree(input, visitor);

			// Create additional worker tasks
			final int numTasksPerRequest = Integer.parseInt(CONFIG.get("imageclassifier.num_tasks_per_request"));
			final int numWorkerTasks = numTasksPerRequest- 1;
			if (numWorkerTasks > 0) {
				log.info("Starting " + numWorkerTasks + " worker task(s)...");
				for (int i = 0; i < numWorkerTasks; ++i)
					executor.execute(new FitsTask(i + 1, paths, outputs, log, verbose));
			}

			// Collect classification results
			int numOutputsLeft = visitor.getNumVisitedPaths();
			while (numOutputsLeft > 0) {
				FitsOutput result = null;
				FitsTaskOutput output = outputs.poll();
				if (output == null) {
					// No worker output is ready yet, try to process next path directly
					Path path = paths.poll();
					if (path == null)
						continue; // No paths left!

					result = fits.process(path, log, verbose);
				} else {
					// A worker output is ready!
					result = output.get();
				}

				// A path was processed, get result
				if (result != null)
					results.add(result);
				else
					++numOutputsFailed;

				--numOutputsLeft;
			}
		}

		FITS_PROCESSORS.add(fits);

		String statusMessage = "Analyzing of '" + input.toString() + "' finished. " + results.size()
				+ " file(s) processed, " + numOutputsFailed + " failed.";

		log.info(statusMessage);
		return results;
	}

	@Override
	public IdentificationOutputIndex<FitsOutput> runIdentification(boolean verbose) {
		IdentificationOutputIndex<FitsOutput> index = new FitsOutputIndex(256);
		for(Path p : contentDirectories)
		{
			try {
				index.add(classify(p, verbose));
				index.addContentPath(p.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return index;
	}

	/* =============== Internal Helpers =============== */

	private static class FitsProcessorPool
	{
		private final Deque<FitsProcessor> processors;
		private final int maxNumProcessors;

		public FitsProcessorPool() {
			this(Integer.parseInt(CONFIG.get("imageclassifier.num_cached_fits_processors")));
		}

		public FitsProcessorPool(int maxNumProcessors) {
			if (maxNumProcessors <= 0)
				throw new IllegalArgumentException("Max. number of processors is invalid: " + maxNumProcessors);

			this.processors = new ArrayDeque<FitsProcessor>(maxNumProcessors);
			this.maxNumProcessors = maxNumProcessors;
		}

		public synchronized FitsProcessor get() {
			FitsProcessor processor = processors.pollLast();
			if (processor == null) {
				// No free instances left!
				processor = FitsProcessor.create();
			}

			return processor;
		}

		public synchronized void add(FitsProcessor processor) {
			if (processors.size() >= maxNumProcessors)
				return;

			processors.add(processor);
		}
	}

	private static class FileVisitor extends SimpleFileVisitor<Path> {
		private final Queue<Path> paths;
		private int numVisitedPaths;

		public FileVisitor(Queue<Path> paths) {
			super();

			this.paths = paths;
			this.numVisitedPaths = 0;
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
			paths.add(path);
			++numVisitedPaths;

			return FileVisitResult.CONTINUE;
		}

		public int getNumVisitedPaths() {
			return numVisitedPaths;
		}
	}

	private static class FitsProcessor {
		private final ToolOutputConsolidator consolidator;
		private final List<Tool> tools;

		public FitsProcessor(ToolOutputConsolidator consolidator, List<Tool> tools) {
			this.consolidator = consolidator;
			this.tools = tools;
		}

		private static String replacePathPrefix(FitsOutput output, String prefix, String replacement) {
			final int offset = prefix.length();

			final FitsMetadataElement element = output.getMetadataElement("filepath");
			String path = element.getValue();
			if (path.startsWith(prefix))
				path = replacement + path.substring(offset);

			return path;
		}

		public FitsOutput processOrThrow(Path input, Logger log, boolean verbose) {
			// Reimplementation of Fits.examine() method

			if (!Files.exists(input) || !Files.isReadable(input))
				throw new IllegalArgumentException(input.toString() + " does not exist or is not readable!");

			final String filename = input.getFileName().toString();
			final String fileext = filename.substring(filename.lastIndexOf(".") + 1);
			final List<ToolOutput> outputs = new ArrayList<ToolOutput>();

			for (Tool tool : tools) {
				// Check tool's include and exclude extension lists
				if (tool.hasIncludedExtensions() && !tool.hasIncludedExtension(fileext))
					continue;
				else if (tool.hasExcludedExtensions() && tool.hasExcludedExtension(fileext))
					continue;

				if (verbose)
					log.info("Running tool " + tool.getName() + " on '" + input.toString() + "'...");

				// Run tool with specified path
				tool.setInputFile(input.toFile());
				tool.run();

				// Check and save tool's result
				ToolOutput output = tool.getOutput();
				if (output != null) {
					outputs.add(output);
					tool.resetOutput();
				}
			}

			if (outputs.isEmpty()) {
				log.warning("Processing '" + input.toString() + "' failed!");
				return null;
			}

			FitsOutput result = consolidator.processResults(outputs);
			if (verbose)
				log.info("Processing '" + input.toString() + "' done.");

			return result;
		}

		public FitsOutput process(Path input, Logger log, boolean verbose) {
			try {
				return this.processOrThrow(input, log, verbose);
			}
			catch (Throwable error) {
				log.log(Level.WARNING, "Processing '" + input.toString() + "' failed!\n", error);
				return null;
			}
		}

		public static FitsProcessor create() {
			// We need to get access to Fits internal ToolBelt and
			// ToolOutputConsolidator objects.
			// Use a dirty hack to get access to the private
			// ToolOutputConsolidator object...

			try {
				Field field = Fits.class.getDeclaredField("consolidator");
				field.setAccessible(true);

				// Fits object needs to be created at least once to initialize
				// the public static fields, used by the internal tools
				Fits fits = new Fits(CONFIG.get("imageclassifier.fitshome"));
				ToolOutputConsolidator consolidator = (ToolOutputConsolidator) field.get(fits);
				ToolBelt toolbelt = fits.getToolbelt();
				return new FitsProcessor(consolidator, toolbelt.getTools());
			} catch (Exception exception) {
				Logger _log = Logger.getLogger("FitsProcessor:create");
				_log.log(Level.SEVERE, exception.getMessage(), exception);
				return null;
			}
		}
	}

	private static class FitsTaskOutput {
		private final FitsOutput output;

		public FitsTaskOutput(FitsOutput output) {
			this.output = output;
		}

		public FitsOutput get() {
			return output;
		}
	}

	/** Class for worker tasks */
	private static class FitsTask implements Runnable {
		private final int taskid;
		private final Logger log;
		private final boolean verbose;
		private final Queue<Path> paths;
		private final Queue<FitsTaskOutput> outputs;

		public FitsTask(int taskid, Queue<Path> paths, Queue<FitsTaskOutput> outputs, Logger log, boolean verbose) {
			this.taskid = taskid;
			this.log = log;
			this.verbose = verbose;
			this.paths = paths;
			this.outputs = outputs;
		}

		@Override
		public void run() {
			if (paths.isEmpty())
				return;   // Nothing to process!

			FitsProcessor fits = FITS_PROCESSORS.get();
			int numPathsProcessed = 0;
			int numPathsFailed = 0;
			Path path = null;

			log.info("Worker task " + taskid + " started.");

			while ((path = paths.poll()) != null) {
				FitsOutput output = fits.process(path, log, verbose);
				outputs.add(new FitsTaskOutput(output));
				if (output == null)
					++numPathsFailed;

				++numPathsProcessed;
			}

			FITS_PROCESSORS.add(fits);

			String status = "Worker task " + taskid + " stopped. " + numPathsProcessed + " file(s) processed, "
					+ numPathsFailed + " failed.";

			log.info(status);
		}
	}
}
