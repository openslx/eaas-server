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

package de.bwl.bwfla.emucomp.components.emulators;


import de.bwl.bwfla.common.services.sse.EventSink;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emucomp.api.PrintJob;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PostScriptPrinter implements Runnable
{
	private static final String TOKEN_START_DOCUMENT  = "%!PS";
	private static final String TOKEN_END_DOCUMENT    = "%%EOF";

	private final Logger log;
	private final Path datapipe;
	private final Charset charset;
	private final AbstractEaasComponent component;
	private final Queue<Path> documents;
	private Converter converter;
	private PrinterState state;
	private Thread worker;
	private boolean running;
	private int docNumber;

	private enum PrinterState
	{
		WAITING,
		PRINTING
	}

	public PostScriptPrinter(Path input, AbstractEaasComponent component, Logger log)
	{
		this(input, StandardCharsets.ISO_8859_1, component, log);
	}

	public PostScriptPrinter(Path input, Charset charset, AbstractEaasComponent component, Logger log)
	{
		this.log = log;
		this.datapipe = input;
		this.charset = charset;
		this.component = component;
		this.documents = new ConcurrentLinkedQueue<>();
		this.state = PrinterState.WAITING;
		this.converter = null;
		this.docNumber = 0;
		this.running = true;
	}

	@Override
	public void run()
	{
		log.info("Start processing printer's data stream...");

		final CharBuffer buffer = CharBuffer.allocate(8 * 1024);
		buffer.limit(0);
		buffer.mark();

		try (final Reader reader = new InputStreamReader(Files.newInputStream(datapipe), charset)) {
			while (running)
				this.process(reader, buffer);
		}
		catch (Exception error) {
			if (!(error instanceof InterruptedException))
				log.log(Level.WARNING, "Processing printer's data stream failed!", error);
		}

		log.info("Stop processing printer's data stream");
	}

	public synchronized void stop()
	{
		running = false;

		if (converter != null) {
			converter.abort();
			converter.cleanup();
		}

		if (worker != null)
			worker.interrupt();
	}

	public PostScriptPrinter setWorkerThread(Thread worker)
	{
		this.worker = worker;
		return this;
	}

	public Thread getWorkerThread()
	{
		return worker;
	}

	public List<PrintJob> getPrintJobs()
	{
		final List<PrintJob> jobs = new ArrayList<>();
		for (Path document : documents) {
			final PrintJob job = new PrintJob();
			job.setLabel(document.getFileName().toString());
			job.setDataHandler(new DataHandler(new FileDataSource(document.toFile())));
			jobs.add(job);
		}

		return jobs;
	}


	/* ========== Internal Helpers ==================== */

	private void setPrinterState(PrinterState newstate)
	{
		if (newstate != state)
			log.info("Printer state changed: " + state + " --> " + newstate);

		state = newstate;
	}

	private String nextDocumentName()
	{
		return String.format("document-%02d.pdf", ++docNumber);
	}

	private void process(Reader reader, CharBuffer buffer) throws IOException, InterruptedException
	{
		// Data consumed completely?
		if (!buffer.hasRemaining()) {
			// Yes, update buffer state
			final int offset = buffer.position();
			final int limit = buffer.limit();
			if (offset < buffer.capacity())
				buffer.limit(buffer.capacity());
			else {
				buffer.clear();
				buffer.mark();
			}

			final int length = reader.read(buffer);
			if (length <= 0) {
				// Wait before trying to read more data, preventing busy-loop
				Thread.sleep((state == PrinterState.PRINTING) ? 250L : 3000L);

				// Revert buffer state
				buffer.position(offset);
				buffer.limit(limit);
				return;
			}

			buffer.limit(buffer.position());
			buffer.reset();
		}

		try {
			this.process(buffer);
		}
		catch (IOException error) {
			log.log(Level.WARNING, "Processing printed document failed!", error);
			this.setPrinterState(PrinterState.WAITING);
			synchronized (this) {
				if (converter != null) {
					converter.abort();
					this.finish();
				}
			}
		}
	}

	private void process(CharBuffer buffer) throws IOException
	{
		switch (state) {
			case WAITING:
				if (this.find(buffer, TOKEN_START_DOCUMENT) > 0) {
					// Beginning of a PS document found
					this.begin();
				}

				break;

			case PRINTING:
				final int offset = buffer.position();
				final int length = this.find(buffer, TOKEN_END_DOCUMENT);
				converter.write(buffer.array(), offset, Math.abs(length));
				if (length > 0) {
					// End of current PS document found
					this.finish();
				}

				break;
		}
	}

	private int find(CharBuffer buffer, String token)
	{
		int numBytesConsumed = 0;

		retry:

		while (buffer.hasRemaining()) {
			++numBytesConsumed;

			// Find token's first char in the buffer
			if (buffer.get() != token.charAt(0)) {
				buffer.mark();
				continue;
			}

			// First char matched, look at the rest...

			if (buffer.remaining() < token.length() - 1) {
				// Not enough chars to compare, reset to first char
				buffer.position(buffer.position() - 1);
				buffer.mark();
				--numBytesConsumed;

				if (buffer.position() + token.length() < buffer.capacity()) {
					// Enough capacity left, request to append more data
					buffer.position(buffer.position() + buffer.remaining());
					break;
				}

				// Not enough capacity, re-allocate data to the beginning
				final int length = buffer.remaining();
				final int offset = buffer.position();
				for (int i = 0; i < length; ++i)
					buffer.put(i, buffer.get(offset + i));

				// Request more data
				buffer.rewind();
				buffer.mark();
				buffer.position(length);
				buffer.limit(length);
				break;
			}

			buffer.mark();

			// Compare the rest of the token
			for (int i = 1, imax = token.length(); i < imax; ++i) {
				if (buffer.get() != token.charAt(i)) {
					// Mismatch, retry search with next char
					buffer.reset();
					break retry;
				}
			}

			numBytesConsumed += token.length() - 1;

			// Token matched!
			buffer.mark();
			return numBytesConsumed;
		}

		// No match!
		return -numBytesConsumed;
	}

	private void begin() throws IOException
	{
		final String docname = this.nextDocumentName();
		final Path output = datapipe.getParent().resolve(docname);
		log.info("Start printing " + docname);

		synchronized (this) {
			converter = new Converter(output, log)
					.write(TOKEN_START_DOCUMENT);
		}

		this.setPrinterState(PrinterState.PRINTING);
	}

	private void finish()
	{
		this.setPrinterState(PrinterState.WAITING);

		final Path docpath = converter.getOutputPath();
		final String docname = docpath.getFileName().toString();
		final long numBytesProcessed = converter.getNumBytesProcessed();
		final boolean successful = converter.finish();
		synchronized (this) {
			converter.cleanup();
			converter = null;
		}

		if (successful)
			documents.add(docpath);

		{
			final String message = "Printing " + docname + ((successful) ? " finished." : " failed!")
					+ " " + numBytesProcessed + " bytes processed";

			if (successful)
				log.info(message);
			else log.warning(message);
		}

		if (component.hasEventSink()) {
			final String status = (successful) ? "done" : "failed";
			final PrintJobNotification notification = new PrintJobNotification(status, docname);
			final EventSink esink = component.getEventSink();
			final OutboundSseEvent event = esink.newEventBuilder()
					.name(PrintJobNotification.name())
					.mediaType(MediaType.APPLICATION_JSON_TYPE)
					.data(notification)
					.build();

			esink.send(event);
		}
	}


	private static class Converter
	{
		private final Logger log;
		private final Path output;
		private final DeprecatedProcessRunner ps2pdf;
		private final Writer writer;
		private long numBytesProcessed;

		public Converter(Path output, Logger log) throws IllegalStateException, IOException
		{
			this.log = log;
			this.output = output;
			this.numBytesProcessed = 0L;

			this.ps2pdf = new DeprecatedProcessRunner("ps2pdf");
			ps2pdf.addArguments("-", output.toString());
			ps2pdf.setLogger(log);
			if (!ps2pdf.start())
				throw new IllegalStateException("Starting converter failed!");

			this.writer = ps2pdf.getStdInWriter();
		}

		public Converter write(String string) throws IOException
		{
			return this.write(string, 0, string.length());
		}

		public Converter write(String string, int offset, int length) throws IOException
		{
			numBytesProcessed += length;

			writer.write(string, offset, length);
			writer.flush();
			return this;
		}

		public Converter write(char[] data, int offset, int length) throws IOException
		{
			numBytesProcessed += length;

			writer.write(data, offset, length);
			writer.flush();
			return this;
		}

		public boolean finish()
		{
			try {
				writer.close();
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Closing converter's stdin failed!", error);
				this.abort();
			}

			return (ps2pdf.waitUntilFinished() == 0);
		}

		public void abort()
		{
			ps2pdf.kill();
			if (ps2pdf.isProcessRunning())
				ps2pdf.waitUntilFinished();
		}

		public void cleanup()
		{
			ps2pdf.printStdOut();
			ps2pdf.printStdErr();
			ps2pdf.cleanup();
		}

		public Path getOutputPath()
		{
			return output;
		}

		public long getNumBytesProcessed()
		{
			return numBytesProcessed;
		}
	}


	private static class PrintJobNotification
	{
		private String status;
		private String filename;

		public PrintJobNotification(String status, String filename)
		{
			this.status = status;
			this.filename = filename;
		}

		@XmlElement(name = "status")
		public String getStatus()
		{
			return status;
		}

		@XmlElement(name = "filename")
		public String getFileName()
		{
			return filename;
		}

		public static String name()
		{
			return "print-job";
		}
	}
}
