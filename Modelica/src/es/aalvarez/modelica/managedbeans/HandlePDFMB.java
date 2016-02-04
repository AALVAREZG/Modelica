package es.aalvarez.modelica.managedbeans;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.aalvarez.modelica.util.MyBatisUtil;


@ManagedBean
@ViewScoped 
public class HandlePDFMB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6018464845390666159L;
	final  Logger logger = LogManager.getLogger(AsistenteExpedienteObraMayor.class);
	
	String titulo;
	
	@PostConstruct
	private void init(){
		logger.debug("Init HandlePDFMB ... ");
		this.titulo="EXPEDIENTE";
	}
	
	public String getTitulo() {
		return titulo;
	}



	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}



	public void closeImprimir(){
    	
    		
    		System.out.println( "en funcion CloseImprimir)");
    	
			
		
    }

	

}
