package me.mywiki;

import wiki.IStorage;
import wiki.WikiStorage;
import me.mywiki.config.WikiConfig;

public class WikiApp {
    
    private final IStorage storage;
  
    public IStorage wikiStorage() {
         return storage;
    }
    
    /**
     * not a public method, this method will be called
     * by context specific initializers such as 
     */
    public static WikiApp initialize(Initializer initComponent){
        _initComponent= initComponent;
        return WikiAppSingletonEnforcer.instance();
    }
    
    
    private static Initializer _initComponent;
    
    private static class WikiAppSingletonEnforcer {
        private static final WikiApp instance;
        private static final Initializer _copyOfInitComponent= _initComponent;
        static {
            instance= new WikiApp(_copyOfInitComponent);
        }
        
        private static WikiApp instance() {
            if (_copyOfInitComponent != _initComponent) {
                throw new IllegalStateException("Detected attempt to twice initialize WikiApp");
            }
            return instance;
        }
        
    }
    
    private WikiApp(Initializer initComponent) {
        this.appConfig= initComponent.config();
        this.storage= WikiStorage.testStorage();
    }
    
    /**
     * SPI interface for context specific initializing mechanism
     *
     */
    public static abstract interface Initializer {
        WikiConfig config();
    }
    
    private final WikiConfig appConfig;
}
