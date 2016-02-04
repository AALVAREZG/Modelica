package es.aalvarez.modelica.managedbeans;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FlowEvent;

import es.aalvarez.modelica.model.Expediente;
import es.aalvarez.modelica.util.GeneraDocxE;


@ManagedBean
@ViewScoped
public class AsistenteLicenciaMB implements Serializable {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 8109547409497533679L;
	final  Logger logger = LogManager.getLogger(AsistenteLicenciaMB.class);

	private Expediente licencia = new Expediente();
	
		
	public Expediente getLicencia() {
		return licencia;
	}

	public void setLicencia(Expediente licencia) {
		this.licencia = licencia;
	}

	public String fsTipoExpediente;
     
   
	public String getFsTipoExpediente() {
		return fsTipoExpediente;
	}

	public void setFsTipoExpediente(String fsTipoExpediente) {
		this.fsTipoExpediente = fsTipoExpediente;
		}

		
	public String nuevo() {
		
		FacesMessage msg = new FacesMessage("Nuevo Expediente ... creando :" + fsTipoExpediente,  fsTipoExpediente);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        logger.debug("Acceso a pantalla nuevo expediente del tipo..." + this.fsTipoExpediente);
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("tipo", this.fsTipoExpediente);
        return this.fsTipoExpediente; //manejamos este return con faces-config

	}      
	
	
   
}
