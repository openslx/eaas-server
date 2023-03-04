package solutions.emulation.preservica.client;

import com.openslx.eaas.common.databind.DataUtils;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import gov.loc.mets.*;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class YaleMetsData {


    public enum Context
    {
        INSTALLATION("Installation"),
        USAGE("Configured Usage");

        private final String label;
        Context(String label)
        {
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

    public String toString()
    {
        String out = "Yale METS data:\n";
        for(Context ctxType : Context.values())
        {
            YaleMetsContext ctx = metsContextMap.get(ctxType);
            if(ctx == null)
                out += ctxType + " not configured.\n";
            else
                out += ctx.toString();
        }
        return out;
    }

    private void init() {
        metsContextMap = new HashMap<>();
        objectFiles = new HashMap<>();

        List<StructMapType> listStructMap = metsRoot.getStructMap();
        for (StructMapType struct : listStructMap) {
            YaleMetsContext ctx = new YaleMetsContext(struct.getTYPE());
            metsContextMap.put(ctx.context, ctx);

            //       System.out.println("struct: " + struct.getTYPE());

            DivType div = struct.getDiv();
            if (div == null)
                continue;

            List<DivType> divlist = div.getDiv();
            for (DivType _div : divlist) {
                String id = null;
                String label = _div.getLabel3();
                String order = _div.getORDER().toString();

                //  System.out.println("div: " + _div.getLabel3() + " " + _div.getORDER());
                List<DivType.Fptr> fptrList = _div.getFptr();
                if (fptrList == null)
                    continue;
                //   System.out.println("fptr: " + fptrList.get(0).getFILEID());
                id = fptrList.get(0).getFILEID();
                if (id != null)
                    id = id.replace("file-", "");

                ctx.files.put(id, new YaleMetsFileInformation(id, label, order));
            }
        }

        if(metsRoot.getFileSec() != null) {
            List<MetsType.FileSec.FileGrp> fileGrpList = metsRoot.getFileSec().getFileGrp();
            for(MetsType.FileSec.FileGrp fileGrp : fileGrpList)
            {
                for(FileType file : fileGrp.getFile())
                {
                    ObjectFile of = new ObjectFile();
                    of.fileLocations = getFileLocation(file);
                    setTypeInfo(of, file);
                    of.size = file.getSIZE();
                    of.id = file.getID();
                }
            }
        }


        // extracting Wikidata information
        List<MdSecType> dmdList = metsRoot.getDmdSec();
        for (MdSecType dmd : dmdList)
        {
     //       System.out.println("looking for wikidata metadata");
            if(!dmd.getID().equals("Wikidata"))
                continue;

            MdSecType.MdRef ref = dmd.getMdRef();
            if(ref == null)
                continue;

            wikiId = ref.getID();

        //    System.out.println("looking for wikidata metadata: got " + wikiId);
        }
    }

    private void setTypeInfo(ObjectFile of, FileType file) {

        final List<Object> amdIds = file.getADMID();
        for(Object amdId : amdIds)
            System.out.println("amdId " + amdId.toString());
    }

    private static List<String> getFileLocation(FileType file) {
        List<FileType.FLocat> locationList = file.getFLocat();
        List<String> result = new ArrayList<>();
        for(FileType.FLocat fLocat : locationList)
        {
            if(fLocat.getLOCTYPE() != null && fLocat.getLOCTYPE().equals("URL"))
            {
                if(fLocat.getHref() != null)
                    result.add(fLocat.getHref());
            }
        }
        return result;
    }

    public String getWikiId() {
        return wikiId;
    }

    public YaleMetsData(File metsFile) throws BWFLAException {

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
    }

    public YaleMetsData(Element element) throws BWFLAException {
        try {
            final Unmarshaller unmarshaller = DataUtils.xml()
                    .unmarshaller(Mets.class);

            this.metsRoot = (Mets) unmarshaller.unmarshal(element);
        } catch (JAXBException e) {
            throw new BWFLAException(e);
        }
        init();
    }

    public YaleMetsFileInformation getFileInformation(String id)
    {
        YaleMetsContext ctx = metsContextMap.get(defaultContext);
        return ctx.files.get(id);
    }

    class YaleMetsContext {
        Context context;
        HashMap<String, YaleMetsFileInformation> files = new HashMap<>();

        YaleMetsContext(String context) {
           for (Context _ctx : Context.values())
           {
               if(!_ctx.label.equals(context))
                   continue;

               this.context = _ctx;
               return;
           }

           throw new IllegalArgumentException("undefined usage context: " + context);
        }

        public String toString()
        {
            String out = "Context: " + context;
            for(String key : files.keySet())
            {
                YaleMetsFileInformation info = files.get(key);
                if(info == null)
                    out += "file information null for id: " + key + "\n";
                else
                    out += info.toString();
            }
            return out;
        }
    }

    public class YaleMetsFileInformation implements Comparable
    {
        String label;
        String id;
        String order;

        YaleMetsFileInformation(String id, String label, String order) {
            this.label = label;
            this.id = id;
            this.order = order;
        }

        public String toString(){
            return "id: " + id + " label " + label + " order " + order + "\n";
        }

        @Override
        public int compareTo(Object o) {
            return order.compareTo(((YaleMetsFileInformation)o).order);
        }

        public String getLabel() {
            return label;
        }

        public String getId() {
            return id;
        }

        public String getOrder() {
            return order;
        }
    }

    public static class ObjectFile
    {
        List<String> fileLocations;
        String fileType;
        String mediumType;
        long size;
        String id;
    }
}
