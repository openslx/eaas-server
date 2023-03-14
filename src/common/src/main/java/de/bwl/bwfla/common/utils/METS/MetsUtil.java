package de.bwl.bwfla.common.utils.METS;

import de.bwl.bwfla.common.services.net.HttpUtils;
import gov.loc.mets.*;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;


public class MetsUtil {

    public enum MetsEaasConstant {
        FILE_GROUP_OBJECTS("DIGITAL OBJECTS");

        private final String label;

        MetsEaasConstant(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

    }
    public enum MetsEaasContext {
        INSTALLATION("Installation"),
        USAGE("Configured Usage");

        private final String label;

        MetsEaasContext(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static void initStructMap(Mets mets)
    {
        StructMapType usageMap = new StructMapType();
       // usageMap.setTYPE(MetsEaasContext.USAGE.toString());
        mets.getStructMap().add(usageMap);

       // StructMapType installMap = new StructMapType();
       // usageMap.setTYPE(MetsEaasContext.INSTALLATION.toString());
       //  mets.getStructMap().add(installMap);
    }

    public static Mets createMets(String id, String label)
    {
        Mets metsRoot = new Mets();
        metsRoot.getStructMap();
        metsRoot.setID(id);
        metsRoot.setLabel1(label);

        initStructMap(metsRoot);
        return metsRoot;
    }

    public static Object createFileFormatEntry(Mets metsRoot, String ffmtId) {
        List<AmdSecType> amdSecList = metsRoot.getAmdSec();
        AmdSecType amdSecType;
        if(amdSecList.size() == 0) {
            amdSecType = new AmdSecType();
            amdSecList.add(amdSecType);
        }
        else
            amdSecType = amdSecList.get(0);

        List<MdSecType> mdSecTypeList = amdSecType.getTechMD();
        Optional<MdSecType> existingType = mdSecTypeList.stream()
                .filter(t -> t.getMdRef().getID().equals(ffmtId))
                .findAny();

        if(existingType.isPresent())
            return existingType.get();

        MdSecType type = new MdSecType();
        type.setID("FileFormat-" + UUID.randomUUID().toString());

        MdSecType.MdRef mdref = new MdSecType.MdRef();
        mdref.setID(ffmtId);
        type.setMdRef(mdref);
        mdref.setMDTYPE("OTHER");
        mdref.setOTHERMDTYPE("FILETYPE");

        mdSecTypeList.add(type);
        return type;
    }

    static Object createDeviceEntry(Mets metsRoot, String deviceId) {

        List<AmdSecType> amdSecList = metsRoot.getAmdSec();
        AmdSecType amdSecType;
        if(amdSecList.size() == 0) {
            amdSecType = new AmdSecType();
            amdSecList.add(amdSecType);
        }
        else
            amdSecType = amdSecList.get(0);

        List<MdSecType> mdSecTypeList = amdSecType.getSourceMD();
        Optional<MdSecType> existingType = mdSecTypeList.stream()
                .filter(t -> t.getMdRef().getID().equals(deviceId))
                .findAny();

        if(existingType.isPresent())
            return existingType.get();

        MdSecType type = new MdSecType();
        type.setID("Device-" + UUID.randomUUID().toString());

        MdSecType.MdRef mdref = new MdSecType.MdRef();
        mdref.setID(deviceId);
        mdref.setMDTYPE("OTHER");
        mdref.setOTHERMDTYPE("DEVICETYPE");
        type.setMdRef(mdref);

        mdSecTypeList.add(type);
        return type;
    }

    static FileType updateFileEntry(FileType ft, String objUrl, String filename)
    {
        List<FileType.FLocat> locationList = ft.getFLocat();
        locationList.clear();

        FileType.FLocat fLocat = new FileType.FLocat();
        fLocat.setLOCTYPE("URL");
        fLocat.setHref(objUrl);

        if(filename != null)
            fLocat.setTitle(filename);

        locationList.add(fLocat);
        return ft;
    }

    static FileType createFileEntry(String fileId, String objUrl, String filename) {

        if (fileId == null || fileId.isEmpty())
            fileId = "FID-" + UUID.randomUUID().toString();

        FileType fT = new FileType();
        fT.setID(fileId);

        List<FileType.FLocat> locationList = fT.getFLocat();
        FileType.FLocat fLocat = new FileType.FLocat();
        fLocat.setLOCTYPE("URL");
        fLocat.setHref(objUrl);

        if(filename != null)
            fLocat.setTitle(filename);

        locationList.add(fLocat);
        return fT;
    }

    public static Mets export(Mets mets, String exportPrefix) {
        final BiFunction<String, String, String> prefixer = (exportPrefix == null || exportPrefix.isEmpty()) ?
            null : (id, url) -> exportPrefix + url;

        return MetsUtil.export(mets, prefixer);
    }

    public static Mets export(Mets mets, BiFunction<String, String, String> prefixer) {
        Mets metsRoot = null;
        try {
            metsRoot = Mets.fromValue(mets.value(), Mets.class);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }

        if (metsRoot.getFileSec() == null)
            return metsRoot;

        List<MetsType.FileSec.FileGrp> fileGrpList = metsRoot.getFileSec().getFileGrp();
        Optional<MetsType.FileSec.FileGrp> digitalObjects = fileGrpList.stream()
                .filter(f -> f.getUSE().equals(MetsEaasConstant.FILE_GROUP_OBJECTS.toString()))
                .findAny();

        if (!digitalObjects.isPresent() || prefixer == null)
            return metsRoot;

        MetsType.FileSec.FileGrp fileGrp = digitalObjects.get();

        for (FileType ft : fileGrp.getFile())
        {
            List<FileType.FLocat> locationList = ft.getFLocat();
            if(locationList.size() == 0)
                continue;

            FileType.FLocat fLocat = locationList.get(0);
            if (HttpUtils.isRelativeUrl(fLocat.getHref()))
                fLocat.setHref(prefixer.apply(ft.getID(), fLocat.getHref()));
        }

        return metsRoot;
    }

    public static FileType addFile(Mets metsRoot, String url, FileTypeProperties properties)
    {
        return MetsUtil.addFile(metsRoot, null, url, properties);
    }

    public static FileType addFile(Mets metsRoot, String id, String url, FileTypeProperties properties)
    {
        if(metsRoot.getFileSec() == null)
            metsRoot.setFileSec(new MetsType.FileSec());

        List<MetsType.FileSec.FileGrp> fileGrpList = metsRoot.getFileSec().getFileGrp();
        Optional<MetsType.FileSec.FileGrp> digitalObjects = fileGrpList.stream()
                .filter(f -> f.getUSE().equals(MetsEaasConstant.FILE_GROUP_OBJECTS.toString()))
                .findAny();

        MetsType.FileSec.FileGrp fileGrp;
        if(digitalObjects.isPresent())
            fileGrp = digitalObjects.get();
        else {
            fileGrp = new MetsType.FileSec.FileGrp();
            fileGrp.setUSE(MetsEaasConstant.FILE_GROUP_OBJECTS.toString());
            metsRoot.getFileSec().getFileGrp().add(fileGrp);
        }

        FileType ft = createFileEntry(id, url, properties.filename);
        fileGrp.getFile().add(ft);

        if(properties.fileSize > 0)
            ft.setSIZE(properties.fileSize);

        if(properties.deviceId != null)
        {
            Object idref = createDeviceEntry(metsRoot, properties.deviceId);
            ft.getADMID().add(idref);
        }

        if(properties.fileFmt != null)
        {
            Object idRef = createFileFormatEntry(metsRoot, properties.fileFmt);
            ft.getADMID().add(idRef);
        }

        return ft;
    }

    public static class FileTypeProperties {
        public long fileSize = 0;
        public String checksum = null;
        public String deviceId = null;
        public String fileFmt = null;
        public String filename = null;
    }
}
