package de.bwl.bwfla.eaas.cluster.provider;

import de.bwl.bwfla.eaas.cluster.ResourceSpec;

import java.util.function.Consumer;
import java.util.function.Function;

public class NodeAllocationRequest
{
    private ResourceSpec spec;
    private Consumer<Node> onUpCallback;
    private Consumer<ResourceSpec> onErrorCallback;
    private Function<String, String> userMetaData;

    public NodeAllocationRequest(ResourceSpec spec, Consumer<Node> onUpCallback, Consumer<ResourceSpec> onErrorCallback)
    {
        this.spec = spec;
        this.onUpCallback = onUpCallback;
        this.onErrorCallback = onErrorCallback;
    }

    public ResourceSpec getSpec() {
        return spec;
    }

    public Consumer<Node> getOnUpCallback() {
        return onUpCallback;
    }

    public Consumer<ResourceSpec> getOnErrorCallback() {
        return onErrorCallback;
    }

    public Function<String, String> getUserMetaData() {
        return userMetaData;
    }

    public void setUserMetaData(Function<String, String> userMetaData) {
        this.userMetaData = userMetaData;
    }
}



