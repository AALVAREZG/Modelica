package es.aalvarez.modelica.util;


import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.util.Constants;





@ManagedBean
@ApplicationScoped
public class Constantes implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3158683010567840151L;
	final  Logger logger = LogManager.getLogger(Constantes.class);
	public final static String ROL_USUARIO_ADMINISTRADOR = "ADMIN";
	public String MOJARRA_VERSION;
	
	public String PRIMEFACES_VERSION;
	
	@PostConstruct
	public void init(){
		
		//returns the major version (2.1)
		FacesContext.class.getPackage().getImplementationVersion();

		//returns the specification version (2.1)
		Package.getPackage("com.sun.faces").getSpecificationVersion();

		//returns the minor implementation version (2.1.x)
		Package.getPackage("com.sun.faces").getImplementationVersion();
		MOJARRA_VERSION = Package.getPackage("com.sun.faces").getImplementationTitle()+ " "+Package.getPackage("com.sun.faces").getImplementationVersion();
		PRIMEFACES_VERSION= Constants.LIBRARY + " " ;
		logger.debug("Init Constantes Class.... ");
		logger.debug("MOJARRA_VERSION.... "+MOJARRA_VERSION);
		logger.debug("PRIMEFACES_VERSION.... "+PRIMEFACES_VERSION);
		
	}
	public String getMOJARRA_VERSION() {
		return MOJARRA_VERSION;
	}

	public void setMOJARRA_VERSION(String mOJARRA_VERSION) {
		MOJARRA_VERSION = mOJARRA_VERSION;
	}


	public String getPRIMEFACES_VERSION() {
		return PRIMEFACES_VERSION;
	}

	public void setPRIMEFACES_VERSION(String pRIMEFACES_VERSION) {
		PRIMEFACES_VERSION = pRIMEFACES_VERSION;
	}
	
	
}
