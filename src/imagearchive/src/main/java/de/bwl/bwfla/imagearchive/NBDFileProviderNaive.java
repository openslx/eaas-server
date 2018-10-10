//package de.bwl.bwfla.imagearchive;
//
//import de.bwl.bwfla.common.exceptions.BWFLAException;
//import de.bwl.bwfla.common.interfaces.NBDFileProvider;
//import de.bwl.bwfla.common.utils.ImageInformation;
//import de.bwl.bwfla.imagearchive.conf.ImageArchiveSingleton;
//import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//
//import javax.ejb.Stateless;
//
//
//@Stateless
//public class NBDFileProviderNaive implements NBDFileProvider {
//
//    private static HashMap<String, File> exportCache = new HashMap<>();
//
//    @Override
//    public File resolveRequest(String reqStr) {
//
//        File cached = exportCache.get(reqStr);
//        {
//            if(cached != null) {
//                return cached;
//            }
//        }
//
//        String filename = "/" + reqStr;
//
//        for(ImageType type : ImageType.values())
//        {
//            File f = new File(ImageArchiveSingleton.iaConfig.imagePath + "/" + type.name() + filename);
//            if(f.exists()) {
//                exportCache.put(reqStr, f);
//                return f;
//            }
//        }
//
//        return null;
//    }
//}
//
//
