package de.bwl.bwfla.emucomp.components.emulators;


import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emucomp.api.PrintJob;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostScriptPrinter {

    protected static final Logger LOG = Logger.getLogger(PostScriptPrinter.class.getCanonicalName());

    private BufferedReader br;
    private FileReader fr;
    private CopyOnWriteArrayList<Path> psFiles;
    private File emulatorPrinterOutput;

    private final DateFormat format;

    private String currentPsFile = null;
    private PrintWriter writer = null;

    private boolean eofFound = true;
    private boolean initialized;
    private String psPrefix;

    private final ExecutorService es = Executors.newSingleThreadExecutor();

    public PostScriptPrinter(File emulatorPrinterOutput)  {
        this.emulatorPrinterOutput = emulatorPrinterOutput;
        psFiles = new CopyOnWriteArrayList<>();

        format = new SimpleDateFormat("YYYY-MM-dd-hh:mm:ss");

        Path targetPsPath = emulatorPrinterOutput.toPath();
        psPrefix = targetPsPath.getParent() + "/print/";
        createDir(psPrefix);
        initialized = false;
    }

    private boolean init()
    {
        if(initialized)
            return true;
        try {
            fr = new FileReader(emulatorPrinterOutput);
        } catch (FileNotFoundException e) {
            return false;
        }
        br = new BufferedReader(fr);
        try {
            br.mark(1024 * 1024 * 50);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }

        initialized = true;
        return true;
    }

    public void release()
    {
        if(!initialized)
            return;
        try {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        initialized = false;
    }

    public void update()
    {
        if(!init())
            return;

        es.submit(() -> {
            try {
                parsePrinterData();
            } catch (IOException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        });
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
                convertPsToPdf(currentPsFile, currentPsFile + ".pdf");
                psFiles.add(Paths.get(currentPsFile + ".pdf"));
                LOG.info("print ready: " + currentPsFile + ".pdf");
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
        runner.start();
        runner.waitUntilFinished();
        runner.cleanup();
    }

    private static DataHandler createCompressedPDFs(ArrayList<Path> psFiles) {
        String targetZip = psFiles.get(0).getParent() + "/pdf/";
        createDir(targetZip);
        for (int i = 0; i < psFiles.size(); i++) {
            LOG.info("convert: " + psFiles.get(i).toString());
            convertPsToPdf(psFiles.get(i).toString(), targetZip + i + ".pdf");
            File ps = new File(psFiles.get(i).toString());
            LOG.info("deleting: " + ps);
            ps.delete();
        }
        Path targetZipPath = Paths.get(targetZip);

        DataSource fds = new FileDataSource(compressDirectory(targetZipPath));

        return new DataHandler(fds);
    }

    private static String compressDirectory(Path directory) {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        String targetZip = directory + "/pdf.zip";
        runner.setCommand("zip");
        runner.addArgument("-r");
        runner.addArgument("-j");
        runner.addArgument(targetZip);
        runner.addArgument(directory.toString());
        runner.start();
        runner.waitUntilFinished();
        runner.printStdOut();
        runner.cleanup();
        return targetZip;
    }

    private static void createDir(String strPath) {
        new File(strPath).mkdir();
    }

    private static void closeWriter(PrintWriter writer) {
        writer.flush();
        writer.close();
    }

}