package de.bwl.bwfla.objectarchive.datatypes;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.BwflaFileInputStream;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import gov.loc.mets.*;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.File;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MetsObject {

    protected final Logger log = Logger.getLogger(this.getClass().getName());

    public Mets getMets() {
        return metsRoot;
    }

    public enum Context {
        INSTALLATION("Installation"),
        USAGE("Configured Usage");

        private final String label;

        Context(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private Context defaultContext = Context.INSTALLATION;
    HashMap<Context, YaleMetsContext> metsContextMap;
    HashMap<String, ObjectFile> objectFiles;

    Mets metsRoot;
    private String wikiId = null;

    public String getId()
    {
        return metsRoot.getID();
    }

    public String getLabel()
    {
        return metsRoot.getLabel1();
    }

    public String toString() {
        String out = "Yale METS data:\n";
        for (Context ctxType : Context.values()) {
            YaleMetsContext ctx = metsContextMap.get(ctxType);
            if (ctx == null)
                out += ctxType + " not configured.\n";
            else
                out += ctx.toString();
        }
        return out;
    }

    private void init() {
        metsContextMap = new HashMap<>();
        objectFiles = new HashMap<>();

//        List<StructMapType> listStructMap = metsRoot.getStructMap();
//        for (StructMapType struct : listStructMap) {
//            YaleMetsContext ctx = new YaleMetsContext(struct.getTYPE());
//            metsContextMap.put(ctx.context, ctx);
//            System.out.println("struct: " + struct.getTYPE());
//
//            DivType div = struct.getDiv();
//            if (div == null)
//                continue;
//
//            List<DivType> divlist = div.getDiv();
//            for (DivType _div : divlist) {
//                String id = null;
//                String label = _div.getLabel3();
//                BigInteger order = _div.getORDER();
//
//                //  System.out.println("div: " + _div.getLabel3() + " " + _div.getORDER());
//                List<DivType.Fptr> fptrList = _div.getFptr();
//                if (fptrList == null)
//                    continue;
//                //   System.out.println("fptr: " + fptrList.get(0).getFILEID());
//                id = fptrList.get(0).getFILEID();
//                if (id != null)
//                    id = id.replace("file-", "");
//
//                ctx.files.put(id, new YaleMetsFileInformation(id, label, order));
//            }
//        }

        if (metsRoot.getFileSec() != null) {
            List<MetsType.FileSec.FileGrp> fileGrpList = metsRoot.getFileSec().getFileGrp();
            for (MetsType.FileSec.FileGrp fileGrp : fileGrpList) {
                for (FileType file : fileGrp.getFile()) {
                    try {
                        ObjectFile of = new ObjectFile();
                        of.fileLocations = getFileLocation(file);
                        setTypeInfo(of, file);

                        if(file.getSIZE() != null)
                            of.size = file.getSIZE();
                        of.id = file.getID();
                        objectFiles.put(of.id, of);
                        log.info(of.toString());
                    }
                    catch (MetsObjectMetadataException e)
                    {
                        log.warning("caught exception " + e.getMessage());
                    }
                }
            }
        }
        else
            log.warning("no files section found");

        // extracting Wikidata information
        List<MdSecType> dmdList = metsRoot.getDmdSec();
        for (MdSecType dmd : dmdList) {
            //       System.out.println("looking for wikidata metadata");
            if (!dmd.getID().equals("Wikidata"))
                continue;

            MdSecType.MdRef ref = dmd.getMdRef();
            if (ref == null)
                continue;

            wikiId = ref.getID();

            //    System.out.println("looking for wikidata metadata: got " + wikiId);
        }
    }

    private void setTypeInfo(ObjectFile of, FileType file) throws MetsObjectMetadataException {
        final List<Object> amdIds = file.getADMID();
        for (Object amdIdObject : amdIds) {
            if(amdIdObject instanceof MdSecType)
            {
                MdSecType mdSec = (MdSecType) amdIdObject;
                MdSecType.MdRef mdRef = mdSec.getMdRef();

                if(mdRef.getOTHERMDTYPE() != null)
                {
                    String mdType = mdRef.getOTHERMDTYPE();
                    if(mdType.equals("DEVICETYPE"))
                    {
                        of.mediumType = mdRef.getID();
                    }
                    else if (mdType.equals("FILETYPE"))
                    {
                        of.fileType = mdRef.getID();
                    }
                }
            }
            else {
                AmdSecType amdSecType = (AmdSecType) amdIdObject;
                String id = amdSecType.getID();
                if (id.startsWith("Provenance")) {
                    of.mediumType = getMediumTyp(amdSecType);
                } else if (id.startsWith("Format")) {
                    of.fileType = getFileType(amdSecType);
                } else
                    log.warning("unknown amdSec " + id);
            }
        }
    }

    private String getFileType(AmdSecType amdSecType) throws MetsObjectMetadataException {
        List<MdSecType> techMdList = amdSecType.getTechMD();
        for (MdSecType techMd : techMdList) {
            if (!techMd.getID().startsWith("FileFormat")) {
                continue;
            }

            MdSecType.MdRef mdref = techMd.getMdRef();
            if (mdref == null)
                throw new MetsObjectMetadataException("missing mdref");

            return mdref.getID();
        }
        throw new MetsObjectMetadataException("invalid amdSecType for id Format");
    }

    private String getMediumTyp(AmdSecType amdSecType) throws MetsObjectMetadataException {
        List<MdSecType> sourceMdList = amdSecType.getSourceMD();
        for (MdSecType sourceMd : sourceMdList) {
            if (!sourceMd.getID().startsWith("Device"))
                continue;

            MdSecType.MdRef mdref = sourceMd.getMdRef();
            if (mdref == null)
                throw new MetsObjectMetadataException("missing mdref");

            return mdref.getID();
        }
        throw new MetsObjectMetadataException("invalid amdSecType for id Provenance");
    }

    private static List<String> getFileLocation(FileType file) throws MetsObjectMetadataException {
        List<FileType.FLocat> locationList = file.getFLocat();
        List<String> result = new ArrayList<>();
        for (FileType.FLocat fLocat : locationList) {
            if (fLocat.getLOCTYPE() != null && fLocat.getLOCTYPE().equals("URL")) {
                if (fLocat.getHref() != null)
                    result.add(fLocat.getHref());
            }
        }
        if(result.size() == 0)
            throw new MetsObjectMetadataException("no file locations found");
        return result;
    }

    public String getWikiId() {
        return wikiId;
    }


    public MetsObject(File metsFile) throws BWFLAException {

        if (!metsFile.exists())
            throw new BWFLAException("METS file not found: " + metsFile);

        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(Mets.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            this.metsRoot = (Mets) unmarshaller.unmarshal(metsFile);
        } catch (JAXBException e) {
            throw new BWFLAException(e);
        }

        init();
        // getFileCollection();
    }

    public MetsObject(Element element) throws BWFLAException {
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(Mets.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            this.metsRoot = (Mets) unmarshaller.unmarshal(element);
        } catch (JAXBException e) {
            throw new BWFLAException(e);
        }
        init();

        // getFileCollection(null);
    }

    public YaleMetsFileInformation getFileInformation(String id) {
        YaleMetsContext ctx = metsContextMap.get(defaultContext);
        return ctx.files.get(id);
    }

    class YaleMetsContext {
        Context context;
        HashMap<String, YaleMetsFileInformation> files = new HashMap<>();

        YaleMetsContext(String context) {
            for (Context _ctx : Context.values()) {
                if (!_ctx.label.equals(context))
                    continue;

                this.context = _ctx;
                return;
            }

            throw new IllegalArgumentException("undefined usage context: " + context);
        }

        public String toString() {
            String out = "Context: " + context;
            for (String key : files.keySet()) {
                YaleMetsFileInformation info = files.get(key);
                if (info == null)
                    out += "file information null for id: " + key + "\n";
                else
                    out += info.toString();
            }
            return out;
        }
    }

    public FileCollection getFileCollection(String exportPrefix)
    {
        log.severe("get object file collection");
        FileCollection c = new FileCollection(getId());
        for(String fileId : objectFiles.keySet())
        {
            log.severe("adding object: " + fileId);
            ObjectFile of = objectFiles.get(fileId);
            if(of.fileLocations.size() == 0 ) {
                log.warning("METS ObjectFile " + of.id + " has no file locations");
                continue;
            }

            String url = of.fileLocations.get(0);
            Drive.DriveType t;
            try {
                if(of.mediumType != null)
                    t = Drive.DriveType.fromQID(of.mediumType);
                else if (of.fileType != null)
                    t = Drive.DriveType.fromQID(of.fileType);
                else {
                    log.severe("can't resolve drive type: ");
                    continue;
                }
                if(t == null)
                    continue;
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
                continue;
            }
            if(exportPrefix != null)
            {
                if(!url.startsWith("http://"))
                    url = exportPrefix + "/" + url;
            }
            FileCollectionEntry fce = new FileCollectionEntry(url, t, of.id);
            c.files.add(fce);
        }
        try {
            log.info(c.value(true));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return c;
    }

    public class YaleMetsFileInformation implements Comparable {
        String label;
        String id;
        BigInteger order;

        YaleMetsFileInformation(String id, String label, BigInteger order) {
            this.label = label;
            this.id = id;
            this.order = order;
        }

        public String toString() {
            return "id: " + id + " label " + label + " order " + order + "\n";
        }

        @Override
        public int compareTo(Object o) {
            return order.compareTo(((YaleMetsFileInformation) o).order);
        }

        public String getLabel() {
            return label;
        }

        public String getId() {
            return id;
        }

        public BigInteger getOrder() {
            return order;
        }
    }

    public static class ObjectFile {
        List<String> fileLocations;
        String fileType;
        String mediumType;
        long size;
        String id;

        public String toString() {
            String out = "fileId: " + id + "\n";
            out += "filetype: " + fileType + "\n";
            out += "mediumtype " + mediumType + "\n";
            out += "size: " + size + "\n";
            for (String loc : fileLocations)
            {
                out += "loc: " + loc + "\n";
            }
            return out;
        }
    }

    public static class MetsObjectMetadataException extends Exception {
        public MetsObjectMetadataException(String message, Throwable cause) {
            super(message, cause);
        }

        public MetsObjectMetadataException(String message)
        {
            super(message);
        }
    }
}