package solutions.emulation.preservica.client;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import gov.loc.mets.DivType;
import gov.loc.mets.MdSecType;
import gov.loc.mets.Mets;
import gov.loc.mets.StructMapType;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.math.BigInteger;
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
                BigInteger order = _div.getORDER();

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

    public String getWikiId() {
        return wikiId;
    }

    public YaleMetsData(File metsFile) throws BWFLAException {

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
    }

    public YaleMetsData(Element element) throws BWFLAException {
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(Mets.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
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
        BigInteger order;

        YaleMetsFileInformation(String id, String label, BigInteger order) {
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

        public BigInteger getOrder() {
            return order;
        }
    }
}
