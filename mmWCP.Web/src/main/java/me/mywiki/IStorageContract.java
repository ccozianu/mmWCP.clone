package me.mywiki;

import home.costin.util.ByteSource;

import java.io.IOException;

/**
 * @status WIP, intention
 */
public interface IStorageContract {
    
    public static interface IStorage {
        public abstract void save( ByteSource content, 
                                   String documentID,
                                   String form, 
                                   String revision) throws IOException;

        public abstract ByteSource open( String documentID, 
                                         String form,
                                         String revision) throws IOException;
        
    }

    /**
     * The service provider interface for storage
     *
     * @param <CfgClass>
     */
    public static interface IStorageSp<CfgType> {
        public IStorage createStorage(CfgType cfg);
    }
    
    public static class StorageSpRegistry {
        private StorageSpRegistry() {}
        
        public static <CfgType> 
            void registerStorageProvider( Class<CfgType> cfgTypeClass, 
                                          IStorageSp<CfgType> storageSpImpl ) 
        {
            throw new UnsupportedOperationException("not implemented yet");
        }
    }
}
