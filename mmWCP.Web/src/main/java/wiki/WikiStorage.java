package wiki;

import home.costin.util.ByteSource;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class WikiStorage implements IStorage  {
	
	
	private static final int mod3 = 7;
	private static final int mod2 = 11;
	private static final int mod1 = 13;
	File rootDir ;
	private static boolean isWindowsSystem= System.getProperty("os.name").toLowerCase().contains("windows");
	
	/**
	 * @deprecated to be replaced with proper constructor injection of configuration
	 * @return
	 */
	public static WikiStorage testStorage() {
		String wikiTmpDir= System.getProperty("wiki.testdir");
		if (wikiTmpDir == null){
			wikiTmpDir = System.getenv("WCP_DATA");
		}
		
		if (wikiTmpDir ==  null) {
		    String xdgData= System.getenv("XDG_DATA");
		    if (xdgData != null) {
		        wikiTmpDir= xdgData + File.separatorChar + "me.mywiki"+File.separatorChar+"test.0";
		    }
		}
		
		//last ditch attempt
		if (wikiTmpDir == null) wikiTmpDir = isWindowsSystem ? "c:/temp" :"/tmp";
		
		File wikiDir= new File(wikiTmpDir,"wikidir");
		if (!wikiDir.exists()) wikiDir.mkdirs();
		return new WikiStorage(wikiDir);
	}
	
	public WikiStorage(File rootDir_) {
		this.rootDir= rootDir_;
		if (!rootDir.exists()) {
			rootDir.mkdirs();
		}
	}
	
	static String documentIDToPath(String docID) {
		int n=docID.hashCode();
		n = ~n;
		int x1= n%mod1; if (x1<0) x1 +=mod1;
		int x2= n%mod2; if (x2<0) x2 +=mod2;
		int x3= n%mod3; if (x3 <0 ) x3+=mod3;
		return ""+x1+"/"+x2+"/"+x3+"/"+docID+"/"; 
	}
	
	static String fileNameFor(String form, String rev ) {
		return rev+"."+form;
	}
	
	/* (non-Javadoc)
     * @see wiki.IStorage#save(home.costin.util.ByteSource, java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public void save(ByteSource content, String documentID, String form, String revision) throws IOException {
		String path= documentIDToPath(documentID);
		File dir= new File(rootDir,path);
		if (!dir.exists())
			dir.mkdirs();
		String fileName= fileNameFor(form,revision);
		File dest= new File(dir, fileName+".tmp");
		dest.createNewFile();
		OutputStream os= new BufferedOutputStream (new FileOutputStream(dest));
		content.transferTo(os);
		os.close();
		File finalTarget= new File(dir,fileName);
		if (finalTarget.exists()) finalTarget.delete();
		dest.renameTo(new File(dir, fileName));
	}
	
	/* (non-Javadoc)
     * @see wiki.IStorage#open(java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public ByteSource open(String documentID,String form,String revision) throws IOException {
		String path= documentIDToPath(documentID);
		File dir= new File(rootDir, path);
		File f= new File(dir,fileNameFor(form,revision));
		if (f.exists())
			return ByteSource.make(new BufferedInputStream(new FileInputStream(f)));
		else
			return null;
	}
	
	public static void main(String args[]) {
		try {
		String content= "Hello storage ";
		String docIDs[]= {"1","2","3","4"};
		WikiStorage storage= testStorage();
		System.out.println(storage.rootDir.getCanonicalPath());
		for (int i = 0; i < docIDs.length; i++) {
			storage.save(ByteSource.make(content+" "+i+".\n"),docIDs[i],"src","1");
			}
		System.out.println("reading...");
		storage.open(docIDs[1],"src","1").transferTo(System.out);
		System.out.flush();
		System.out.println("OK");
		}
		catch(Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		}
	}

}
