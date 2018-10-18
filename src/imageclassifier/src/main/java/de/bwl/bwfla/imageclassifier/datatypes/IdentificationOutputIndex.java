package de.bwl.bwfla.imageclassifier.datatypes;

import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.imageclassifier.client.ClassificationEntry;
import de.bwl.bwfla.imageclassifier.client.HistogramEntry;
import edu.harvard.hul.ois.fits.FitsOutput;
import pronom.nationalarchives.gov.impl.PronomParser;

import java.util.*;
import java.util.logging.Logger;

public abstract class IdentificationOutputIndex<T> {

    /** Mapping for FileTypes: PUID -> Name */
    protected static final Map<String, String> FILETYPE_NAMES;
    static {
        try {
            FILETYPE_NAMES = PronomParser.buildPronomIdToNameIndex();
        }
        catch (Exception error) {
            throw new RuntimeException("Building filetype name index failed!", error);
        }
    }

    protected final static Logger LOG = Logger.getLogger(IdentificationOutputIndex.class.getName());

    public final Map<String, List<T>> mimetypes;
    public final Map<String, List<T>> exttypes;
    public final List<T> unclassified;
    protected final List<String> pathList;

    public IdentificationOutputIndex()
    {
        this.mimetypes = new HashMap<String, List<T>>();
        this.exttypes = new HashMap<String, List<T>>();
        this.unclassified = new ArrayList<T>();
        this.pathList = new ArrayList<>();
    }

    /** Constructor */
    public IdentificationOutputIndex(int capacity)
    {
        this.mimetypes = new HashMap<String, List<T>>(capacity);
        this.exttypes = new HashMap<String, List<T>>(capacity);
        this.unclassified = new ArrayList<T>();
        this.pathList = new ArrayList<>();
    }

    /** Returns the outputs indexed by mimetypes. */
    public Map<String, List<T>> getMimetypeIndex()
    {
        return mimetypes;
    }

    /** Returns the outputs indexed by external types. */
    public Map<String, List<T>> getExtTypeIndex()
    {
        return exttypes;
    }

    public abstract void add(T output);

    public abstract List<ClassificationEntry> getClassifierList(Map<String, String> policy);

    public abstract List<HistogramEntry> getSummaryClassifierList(Map<String, String> policy);

    /** Returns the unclassified outputs. */
    public List<T> getUnclassifiedFiles()
    {
        return unclassified;
    }

    /** Clears this index. */
    public void clear()
    {
        mimetypes.clear();
        exttypes.clear();
        unclassified.clear();
        pathList.clear();
    }

    public void addContentPath(String path)
    {
        pathList.add(path);
    }

    public List<Pair<String, Integer>> getOrderedPUIDList()
    {
        List<Pair<String, Integer>> out = new ArrayList<Pair<String,Integer>>();
        for (Map.Entry<String, List<T>> entry : exttypes.entrySet()) {
            out.add(new Pair<String, Integer>(entry.getKey(), new Integer(entry.getValue().size())));
        }

        Collections.sort(out, new Comparator<Pair<String, Integer>>() {
            public int compare(final Pair<String, Integer> a, final Pair<String, Integer> b) {
                return -a.getB().compareTo(b.getB());}
        });

        return out;
    }

    protected void add(Map<String, List<T>> map, String key, T value)
    {
        List<T> list = map.get(key);
        if (list == null) {
            list = new ArrayList<T>();
            map.put(key, list);
        }

        list.add(value);
    }

    protected void addList(Map<String, List<T>> map, String key, List<T> l)
    {
        List<T> list = map.get(key);
        if (list == null)
            map.put(key, l);
        else
            list.addAll(l);
    }

    public void add(List<T> outputs)
    {
        for (T output : outputs)
            this.add(output);
    }

//	/** Add content of the other index into this index. */
//	public void join(FitsOutputIndex other)
//	{
//		if (other == null)
//			return;
//
//		for (Map.Entry<String, List<FitsOutput>> entry : other.mimetypes.entrySet())
//			this.addList(mimetypes, entry.getKey(), entry.getValue());
//
//		for (Map.Entry<String, List<FitsOutput>> entry : other.exttypes.entrySet())
//			this.addList(exttypes, entry.getKey(), entry.getValue());
//	}
}
