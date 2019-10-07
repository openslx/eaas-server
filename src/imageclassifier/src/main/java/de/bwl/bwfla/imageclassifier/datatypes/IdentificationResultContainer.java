package de.bwl.bwfla.imageclassifier.datatypes;

public class IdentificationResultContainer<T> {

    private final T data;

    public IdentificationResultContainer(T data)
    {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
