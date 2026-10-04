package com.curea.document.storage;

public interface DocumentStorage {

    void put(String publicId, byte[] content);

    byte[] get(String publicId);

    void delete(String publicId);
}
