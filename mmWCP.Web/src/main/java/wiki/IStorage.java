package wiki;

import home.costin.util.ByteSource;

import java.io.IOException;

public interface IStorage {

    public abstract void save(ByteSource content, String documentID,
            String form, String revision) throws IOException;

    public abstract ByteSource open(String documentID, String form,
            String revision) throws IOException;

}