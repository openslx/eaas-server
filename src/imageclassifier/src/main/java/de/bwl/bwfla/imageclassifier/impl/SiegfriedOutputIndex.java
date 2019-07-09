package de.bwl.bwfla.imageclassifier.impl;

import de.bwl.bwfla.imageclassifier.client.ClassificationEntry;
import de.bwl.bwfla.imageclassifier.client.HistogramEntry;
import de.bwl.bwfla.imageclassifier.datatypes.IdentificationOutputIndex;
import de.bwl.bwfla.imageclassifier.datatypes.Siegfried;
import de.bwl.bwfla.wikidata.reader.QIDsFinder;
import de.bwl.bwfla.wikidata.reader.entities.SoftwareQIDs;
import edu.harvard.hul.ois.fits.FitsMetadataElement;
import edu.harvard.hul.ois.fits.FitsOutput;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SiegfriedOutputIndex extends IdentificationOutputIndex<Siegfried.File> {

    @Override
    public void add(Siegfried.File output) {

        if(output == null)
            return;

        List<Siegfried.File.Match> matches = output.getMatches();
        if(matches == null)
            return;

        for(Siegfried.File.Match m : matches)
        {
            if(m.getMime() != null)
                this.add(mimetypes, m.getMime(), output);

            if(m.getNs() != null && m.getNs().equals("pronom") && m.getId() != null)
                this.add(exttypes, m.getId(), output);
            else
                unclassified.add(output);
        }
    }

    @Override
    public List<ClassificationEntry> getClassifierList(Map<String, String> policy) {
        List<ClassificationEntry> entries = new ArrayList<ClassificationEntry>();
        final String defaultValue = policy.get("default");
        final String replacement = ".";

        long now = (new Date()).getTime();
        long fromDate = now;
        long toDate = 0;

        for (Map.Entry<String, List<Siegfried.File>> entry : exttypes.entrySet()) {
            final String type = entry.getKey();
            List<Siegfried.File> files = entry.getValue();
            String value = policy.get(type);
            if (value == null)
                value = defaultValue;
            // SoftwareQIDs softwareQIDs = QIDsFinder.findQIDs(type);

            final List<String> fileNames = new ArrayList<String>(files.size());
            for(Siegfried.File sf : files)
            {
                if(sf.getFilename() == null)
                {
                    LOG.warning("filename null in Siegfried's result");
                    continue;
                }
                fileNames.add(replacePathPrefix(sf.getFilename(), replacement));

                long time = 0;
                try {
                    time = getModificationDate(sf.getModified()).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(time < fromDate)
                    fromDate = time;

                if(time < now && time > toDate)
                    toDate = time;
            }
            ClassificationEntry cf = new ClassificationEntry(type, value, fileNames,
                    new ArrayList<>(), new ArrayList<>(), FILETYPE_NAMES.get(type));

            cf.setFromDate(fromDate);
            cf.setToDate(toDate);
            entries.add(cf);
        }

        if (unclassified.size() > 0) {
            final String type = "unknown";
            final String value = policy.get(type);

            final List<String> fileNames = new ArrayList<String>(unclassified.size());
            for(Siegfried.File sf : unclassified)
            {
                if(sf.getFilename() == null)
                {
                    LOG.warning("filename null in Siegfried's result");
                    continue;
                }
                fileNames.add(replacePathPrefix(sf.getFilename(), replacement));
            }
            ClassificationEntry cf = new ClassificationEntry(type, value, fileNames,
                    null, null, "unknown");

            entries.add(cf);
        }
        return entries;
    }

    private String replacePathPrefix(String filename, String replacement) {
        for(String path : pathList) {
            if (!filename.startsWith(path))
                continue;
            final int offset = path.length();
            filename = replacement + filename.substring(offset);
            break;
        }
        return filename;
    }

    private static Date getModificationDate(String input) throws ParseException {
        // 1998-05-15T10:19:00Z 1993-09-30T12:04:31Z
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return sdf.parse(input);
    }

    @Override
    public List<HistogramEntry> getSummaryClassifierList(Map<String, String> policy) {
        List<HistogramEntry> entries = new ArrayList<HistogramEntry>();
        final String defaultValue = policy.get("default");

        for (Map.Entry<String, List<Siegfried.File>> entry : exttypes.entrySet()) {
            final String type = entry.getKey();
            final int count = entry.getValue().size();
            String value = policy.get(type);
            if (value == null)
                value = defaultValue;

            entries.add(new HistogramEntry(type, count, value));
        }

        if (unclassified.size() > 0) {
            final String type = "unknown";
            final String value = policy.get(type);
            entries.add(new HistogramEntry(type, unclassified.size(), value));
        }
        return entries;
    }
}
