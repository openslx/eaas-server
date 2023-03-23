package de.bwl.bwfla.objectarchive.datatypes;

import com.openslx.eaas.common.databind.DataUtils;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.METS.MetsUtil;
import de.bwl.bwfla.emucomp.api.Binding;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import gov.loc.mets.*;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.bwl.bwfla.common.utils.METS.MetsUtil.MetsEaasConstant.FILE_GROUP_OBJECTS;


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

    public void setLabel(String newLabel) {
        metsRoot.setLabel1(newLabel);
    }

    public String toString() {
        return metsRoot.toString();
    }

    private static boolean containsId(List <DivType.Fptr> fptrs, String fileId)
    {
        for(DivType.Fptr fptr : fptrs)
        {
            if(fptr.getFILEID().equals(fileId))
                return true;
        }
        return false;
    }

    private void setStructInfo(ObjectFile of)
    {
        List<StructMapType> listStructMap = metsRoot.getStructMap();
        for (StructMapType struct : listStructMap) {

            DivType div = struct.getDiv();
            if (div == null)
                continue;

            List<DivType> divlist = div.getDiv();
            for (DivType _div : divlist) {
                List <DivType.Fptr> fptrs = _div.getFptr();
                if(!containsId(fptrs, of.id))
                    continue;

                String label = _div.getLabel3();
                BigInteger order = _div.getORDER();

                of.label = label;
                if(order != null)
                    of.order = order.toString();
                return;
            }
        }
    }

    private void init() {
        metsContextMap = new HashMap<>();
        objectFiles = new HashMap<>();

        List<StructMapType> listStructMap = metsRoot.getStructMap();
        if(listStructMap == null || listStructMap.size() == 0)
        {
            MetsUtil.initStructMap(metsRoot);
        }

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
                if (!fileGrp.getUSE().equals(FILE_GROUP_OBJECTS.toString()))
                    continue;  // skip all other file-groups for now!

                for (FileType file : fileGrp.getFile()) {
                    try {
                        ObjectFile of = new ObjectFile();
                        getFileLocation(of, file);
                        setTypeInfo(of, file);
                        of.id = file.getID();

                        setStructInfo(of);

                        if(file.getSIZE() != null)
                            of.size = file.getSIZE();

                        objectFiles.put(of.id, of);
                        // log.info(of.toString());
                    }
                    catch (MetsObjectMetadataException error) {
                        final var message = "Processing METS with ID '" + this.getId() + "' failed!";
                        log.log(Level.WARNING, message, error);
                    }
                }
            }
        }
        else
            log.warning("No files found for METS with ID '" + this.getId() + "'!");

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

    /*
    Example:

    <mets:amdSec ID="Provenance-xxx">
        <mets:sourceMD></mets:sourceMD>
    </mets:amdSec>
    <mets:amdSec ID="Format-xx">
        <mets:techMD></mets:techMD>
    </mets:amdSec>
    */
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
                    of.mediumType = getMediumType(amdSecType, true);
                } else if (id.startsWith("Format")) {
                    of.fileType = getFileType(amdSecType, true);
                } else
                    log.warning("unknown amdSec " + id);
            }
        }
        if(of.fileType == null && of.mediumType == null)
            throw new MetsObjectMetadataException("Unknown file format/media-type for file '" + file.getID() + "'...");
    }

    /*
    Example:

    <mets:techMD ID="FileFormat-xxx">
        <mets:mdRef ID="Q877050" LABEL="ISO image" LOCTYPE="URL" MDTYPE="OTHER" xlink:href="https://www.wikidata.org/wiki/Q877050"/>
     </mets:techMD>
    */
    private String getFileType(AmdSecType amdSecType, boolean strictVerification) throws MetsObjectMetadataException {
        List<MdSecType> techMdList = amdSecType.getTechMD();
        for (MdSecType techMd : techMdList) {
            if (!techMd.getID().startsWith("FileFormat")) {
                continue;
            }

            MdSecType.MdRef mdref = techMd.getMdRef();
            if (mdref == null)
                throw new MetsObjectMetadataException("missing mdref");

            String id = mdref.getID();
            if(id == null || id.isEmpty())
                throw new MetsObjectMetadataException("device type id not set");
            if(strictVerification)
            {
                if(!id.startsWith("Q") || !id.startsWith("q"))
                    throw new MetsObjectMetadataException("strict verification requires wikidata QID for type FileFormat");
            }
            return id;
        }
        throw new MetsObjectMetadataException("invalid amdSecType for id Format");
    }

    /*
    Example:

    <mets:sourceMD ID="Device-xxx">
        <mets:mdRef ID="Q495265" LABEL="Combo drive" LOCTYPE="URL" MDTYPE="OTHER" xlink:href="http://www.wikidata.org/wiki/Q495265"/>
    </mets:sourceMD>
     */
    private String getMediumType(AmdSecType amdSecType, boolean strictVerification) throws MetsObjectMetadataException {
        List<MdSecType> sourceMdList = amdSecType.getSourceMD();
        for (MdSecType sourceMd : sourceMdList) {
            if (!sourceMd.getID().startsWith("Device"))
                continue;

            MdSecType.MdRef mdref = sourceMd.getMdRef();
            if (mdref == null)
                throw new MetsObjectMetadataException("missing mdref");

            String id = mdref.getID();
            if(id == null || id.isEmpty())
                throw new MetsObjectMetadataException("device type id not set");
            if(strictVerification)
            {
                if(!id.startsWith("Q") || !id.startsWith("q"))
                    throw new MetsObjectMetadataException("strict verification requires wikidata QID for type Device");
            }
            return id;
        }
        throw new MetsObjectMetadataException("invalid amdSecType for id Provenance");
    }

    private static void getFileLocation(ObjectFile of, FileType file) throws MetsObjectMetadataException {
        List<FileType.FLocat> locationList = file.getFLocat();
        List<String> result = new ArrayList<>();
        String filename = null;
        for (FileType.FLocat fLocat : locationList) {
            if (fLocat.getLOCTYPE() != null && fLocat.getLOCTYPE().equals("URL")) {
                if (fLocat.getHref() != null)
                    result.add(fLocat.getHref());
                if(fLocat.getTitle() != null)
                    of.filename = fLocat.getTitle();
            }
        }
        if(result.size() == 0)
            throw new MetsObjectMetadataException("no file locations found");
        of.fileLocations = result;
    }

    public String getWikiId() {
        return wikiId;
    }


    public MetsObject(String metsdata) throws BWFLAException
    {
        if(metsdata == null)
            throw new BWFLAException("no mets data available: null");

        try {
            this.metsRoot = DataUtils.xml()
                    .read(metsdata, Mets.class);
        } catch (JAXBException e) {
            throw new BWFLAException(e);
        }

        init();
    }

    public MetsObject(File metsFile) throws BWFLAException {

        if (!metsFile.exists())
            throw new BWFLAException("METS file not found: " + metsFile);

        try {
            final Unmarshaller unmarshaller = DataUtils.xml()
                    .unmarshaller(Mets.class);

            this.metsRoot = (Mets) unmarshaller.unmarshal(metsFile);
        } catch (JAXBException e) {
            throw new BWFLAException(e);
        }

        init();
        // getFileCollection();
    }

    public MetsObject(Element element) throws BWFLAException {
        try {
            final Unmarshaller unmarshaller = DataUtils.xml()
                    .unmarshaller(Mets.class);

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
        FileCollection c = new FileCollection(getId());
        c.setLabel(getLabel());
        for(String fileId : objectFiles.keySet())
        {
            ObjectFile of = objectFiles.get(fileId);
            if(of.fileLocations.size() == 0 ) {
                log.warning("METS ObjectFile " + of.id + " has no file locations");
                continue;
            }

            String url = of.fileLocations.get(0);
            Drive.DriveType t = null;
            Binding.ResourceType rt = null;
            try {
                if (of.mediumType != null) {
                    t = Drive.DriveType.fromQID(of.mediumType);
                }
                else if (of.fileType != null) {
                    rt = Binding.ResourceType.fromQID(of.fileType);
                }
                else {
                    log.warning("can't resolve drive type: " + of.mediumType + " " + of.fileType);
                }

                if (t == null && rt == null)
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
            fce.setResourceType(rt);
            if(of.label != null)
                fce.setLabel(of.label);
            else {
                final int pos = 1 + url.lastIndexOf("/");
                fce.setLabel(url.substring(pos));
            }

            if(of.filename != null)
                fce.setLocalAlias(of.filename);

            c.files.add(fce);
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
        String filename;
        String label;
        String order;

        public String toString() {
            String out = "fileId: " + id + "\n";
            out += "filetype: " + fileType + "\n";
            out += "mediumtype " + mediumType + "\n";
            out += "size: " + size + "\n";
            out += "label: " + label + "\n";
            out += "order: " + order + "\n";

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