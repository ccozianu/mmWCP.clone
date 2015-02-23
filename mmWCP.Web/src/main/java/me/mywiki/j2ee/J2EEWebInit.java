package me.mywiki.j2ee;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import me.mywiki.WikiApp;
import me.mywiki.config.WikiConfig;

/**
 * This class bootstraps
 * our configuration system by wiring into J2EE WebApp concept of context listener
 * This should be the only point where we take a hard dependency on J2EE ContextListeners
 */

@WebListener
public class J2EEWebInit implements ServletContextListener, WikiApp.Initializer {

    /**
     * This is the place in the j2ee context (i.e. servlet context for a servet API)
     * where we'll wire our configured live WikiApp object
     */
    public static final String J2EE_APP_LOOKUP_NAME = "me.mywiki.WikiApp";
    
    
    private WikiConfig wikiConfig;
    private WikiApp    theApp;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.out.println("Entering the server container");
        
            System.getProperties().store(System.err,"");
        
            this.wikiConfig= initializeConfigFromContext(sce.getServletContext());

            this.theApp= WikiApp.initialize(this);
            sce.getServletContext().setAttribute(J2EE_APP_LOOKUP_NAME, theApp);
        }
        catch (Exception ex) {
            if (ex instanceof RuntimeException)  { throw (RuntimeException) ex; }
            else                                 { throw new RuntimeException(ex); }   
        }
        
    }
    
    


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Bye bye servlet container");
    }
    
    @Override
    public WikiConfig config() {
        return this.wikiConfig;
    }
    
    public WikiApp theApp(){
        return this.theApp;
    }
    
    public static WikiApp theApp(ServletContext sCtx) {
        WikiApp result= (WikiApp) sCtx.getAttribute(J2EE_APP_LOOKUP_NAME);
        if (result==null) {
            throw new IllegalStateException("WikiApp not initialized in the current context: "+sCtx);
        }
        return result;
    }
    
    /**
     * Finds the best fit config values from a J2EE servlet context.
     * TODO: bind specific config values from J2EE config mechanisms
     */
    private WikiConfig initializeConfigFromContext(ServletContext servletContext) {
         return new WikiConfig();
    }

}
