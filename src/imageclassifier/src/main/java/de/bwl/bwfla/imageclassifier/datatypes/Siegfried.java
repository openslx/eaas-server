package de.bwl.bwfla.imageclassifier.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Siegfried extends JaxbType{

    @XmlElement
    private String siegfried;

    @XmlElement
    private String scandate;

    @XmlElement
    private String signature;

    @XmlElement
    private String created;

    @XmlElement
    private List<Identifiers> identifiers;

    @XmlElement
    private List<File> files;

    public String getSiegfried() {
        return siegfried;
    }

    public void setSiegfried(String siegfried) {
        this.siegfried = siegfried;
    }

    public String getScandate() {
        return scandate;
    }

    public void setScandate(String scandate) {
        this.scandate = scandate;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public List<Identifiers> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<Identifiers> identifiers) {
        this.identifiers = identifiers;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }


    public static class File
    {
        @XmlElement
        private String filename;

        @XmlElement
        private String filesize;

        @XmlElement
        private String modified;

        @XmlElement
        private String errors;

        @XmlElement
        private List<Match> matches;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFilesize() {
            return filesize;
        }

        public void setFilesize(String filesize) {
            this.filesize = filesize;
        }

        public String getModified() {
            return modified;
        }

        public void setModified(String modified) {
            this.modified = modified;
        }

        public String getErrors() {
            return errors;
        }

        public void setErrors(String errors) {
            this.errors = errors;
        }

        public List<Match> getMatches() {
            return matches;
        }

        public void setMatches(List<Match> matches) {
            this.matches = matches;
        }


        public static class Match {

            @XmlElement
            private String ns;

            @XmlElement
            private String id;

            @XmlElement
            private String format;

            @XmlElement
            private String version;

            @XmlElement
            private String mime;

            @XmlElement
            private String basis;

            @XmlElement
            private String warning;

            public String getNs() {
                return ns;
            }

            public void setNs(String ns) {
                this.ns = ns;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getFormat() {
                return format;
            }

            public void setFormat(String format) {
                this.format = format;
            }

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }

            public String getMime() {
                return mime;
            }

            public void setMime(String mime) {
                this.mime = mime;
            }

            public String getBasis() {
                return basis;
            }

            public void setBasis(String basis) {
                this.basis = basis;
            }

            public String getWarning() {
                return warning;
            }

            public void setWarning(String warning) {
                this.warning = warning;
            }
        }
    }

    public static class Identifiers
    {
        @XmlElement
        private String name;

        @XmlElement
        private String details;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }
}
