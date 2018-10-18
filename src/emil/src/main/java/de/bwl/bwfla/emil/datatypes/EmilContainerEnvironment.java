package de.bwl.bwfla.emil.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmilContainerEnvironment extends EmilEnvironment {

    @XmlElement
    private String input;

    @XmlElement
    private String output;
    @XmlElement
    private String title;

    @XmlElement
    private List<String> args;

    @XmlElement
    private List<String> env;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = new ArrayList<>();
        this.args.addAll(args);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getEnv() {
        return env;
    }

    public void setEnv(List<String> env) {
        this.env = new ArrayList<>();
        this.env.addAll(env);
    }
}
