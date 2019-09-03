package de.bwl.bwfla.emucomp.components.emulators;


import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emucomp.api.PrintJob;
import de.bwl.bwfla.common.services.sse.EventSink;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.xml.bind.annotation.XmlElement;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class PostScriptPrinter implements Runnable
{
    protected static final Logger LOG = Logger.getLogger(PostScriptPrinter.class.getCanonicalName());

    private boolean running = true;
    private AbstractEaasComponent component;
    private BufferedReader br;
    private Queue<Path> psFiles;
    private Path emulatorPrinterOutput;

    private final DateFormat format;

    private String currentPsFile = null;
    private PrintWriter writer = null;

    private boolean eofFound = true;
    private String psPrefix;

    public PostScriptPrinter(Path emulatorPrinterOutput, AbstractEaasComponent component)  {
        this.emulatorPrinterOutput = emulatorPrinterOutput;
        this.component = component;
        psFiles = new ConcurrentLinkedQueue<>();
        format = new SimpleDateFormat("YYYY-MM-dd-hh:mm:ss");
        psPrefix = emulatorPrinterOutput.getParent() + "/print/";
        createDir(psPrefix);
    }

    @Override
    public void run()
    {
        // Wait for printed data...
        while (!this.prepare()) {
            try {
                Thread.sleep(5000L);
            }
            catch (Exception error) {
                return;  // Thread was interrupted!
            }
        }

        // Parse printed data...
        while (running) {
            try {
                this.parsePrinterData();
            }
            catch (IOException error) {
                LOG.log(Level.WARNING, "Parsing printed data failed!", error);
            }
            try {
                Thread.sleep(10000L);
            }
            catch (Exception error) {
                break;  // Thread was interrupted!
            }
        }
    }

    private boolean prepare()
    {
        try {
            br = Files.newBufferedReader(emulatorPrinterOutput, ISO_8859_1);
        } catch (IOException e) {
            return false;
        }
        try {
            br.mark(1024 * 1024 * 50);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }

        return true;
    }

    public void stop()
    {
        running = false;
        try {
            if (br != null)
                br.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void parsePrinterData() throws IOException {
        String sCurrentLine;

        while ((sCurrentLine = br.readLine()) != null) {

            if (sCurrentLine.contains("%!PS") && eofFound) {
                currentPsFile = psPrefix + "/ps-" + format.format(new Date());
                LOG.info("starting printout to: " + currentPsFile);
                writer = new PrintWriter(new File(currentPsFile), "UTF-8");
                writer.println(sCurrentLine.substring(sCurrentLine.indexOf("%")));
                eofFound = false;
            } else if (sCurrentLine.equals("%%EOF")) {
                // br.mark(1024 * 1024 * 25); // limiting printouts to 25mb
                writer.println(sCurrentLine);
                closeWriter(writer);
                final Path pdfpath = Paths.get(currentPsFile + ".pdf");
                convertPsToPdf(currentPsFile, pdfpath.toString());
                psFiles.add(pdfpath);
                LOG.info("print ready: " + pdfpath.toString());
                if (component.hasEventSink()) {
                    final String filename = pdfpath.getFileName().toString();
                    final PrintJobNotification notification = new PrintJobNotification("done", filename);
                    final EventSink esink = component.getEventSink();
                    final OutboundSseEvent event = esink.newEventBuilder()
                            .name(PrintJobNotification.name())
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .data(notification)
                            .build();

                    esink.send(event);
                }

                br.mark(1024 * 1024 * 50);
                writer = null;
                eofFound = true;
            } else {
                if(writer == null)
                {
                    // LOG.info("parsePrinterData: we should not write here");
                }
                else
                    writer.println(sCurrentLine);
            }
        }

        br.reset();
        eofFound = true;
        writer = null;
    }

    public List<PrintJob> getPrintJobs()
    {
        List<PrintJob> jobs = new ArrayList<>();
        for(Path p : psFiles)
        {
            PrintJob pj = new PrintJob();
            pj.setLabel(p.getFileName().toString());
            pj.setDataHandler(new DataHandler(new FileDataSource(p.toFile())));
            jobs.add(pj);
        }
        return jobs;
    }

    private static void convertPsToPdf(String psFilepath, String targetPath) {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand("ps2pdf");
        runner.addArgument(psFilepath);
        runner.addArgument(targetPath);
        runner.execute();
    }

//    private static DataHandler createCompressedPDFs(ArrayList<Path> psFiles) {
//        String targetZip = psFiles.get(0).getParent() + "/pdf/";
//        createDir(targetZip);
//        for (int i = 0; i < psFiles.size(); i++) {
//            LOG.info("convert: " + psFiles.get(i).toString());
//            convertPsToPdf(psFiles.get(i).toString(), targetZip + i + ".pdf");
//            File ps = new File(psFiles.get(i).toString());
//            LOG.info("deleting: " + ps);
//            ps.delete();
//        }
//        Path targetZipPath = Paths.get(targetZip);
//
//        DataSource fds = new FileDataSource(compressDirectory(targetZipPath));
//
//        return new DataHandler(fds);
//    }

    private static String compressDirectory(Path directory) {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        String targetZip = directory + "/pdf.zip";
        runner.setCommand("zip");
        runner.addArgument("-r");
        runner.addArgument("-j");
        runner.addArgument(targetZip);
        runner.addArgument(directory.toString());
        runner.execute();
        return targetZip;
    }

    private static void createDir(String strPath) {
        new File(strPath).mkdir();
    }

    private static void closeWriter(PrintWriter writer) {
        writer.flush();
        writer.close();
    }


    public static class PrintJobNotification
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