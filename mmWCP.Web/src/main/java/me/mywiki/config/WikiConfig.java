package me.mywiki.config;

/**
 * This modules defines all configuration values
 * needed to run a wiki
 * @author Costin
 *
 */
public class WikiConfig {
    
    /**
     * The root read-only configuration
     */
    public static interface RootRO {
        public String localAlias();
        public String localXDGConfig();
        public String localXDGData();
        
    }
}
