package es.aalvarez.modelica.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import es.aalvarez.modelica.model.Expediente;
import es.aalvarez.modelica.model.ExpedienteExample;
import es.aalvarez.modelica.model.ExpedienteRelacionado;
import es.aalvarez.modelica.model.ExpedienteRelacionadoExample;
import es.aalvarez.modelica.model.TramiteExpediente;
import es.aalvarez.modelica.model.TramiteExpedienteExample;
import es.aalvarez.modelica.service.ExpedienteMapper;
import es.aalvarez.modelica.service.ExpedienteRelacionadoMapper;
import es.aalvarez.modelica.service.TramiteExpedienteMapper;
import es.aalvarez.modelica.util.CreatePdfResumenExpediente;
import es.aalvarez.modelica.util.MyBatisUtil;
@ManagedBean
@ViewScoped
public class SeleccionarExpedienteMB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2245756545735328584L;
	final  Logger logger = LogManager.getLogger(SeleccionarExpedienteMB.class); 
	
	public List<Expediente> expedientes = new ArrayList<Expediente>();
	private List<Expediente> filteredExpedientes;
	private final static String[] tiposExpediente;
	private final static String[] sestadosExpediente;
	

	private List<String> estadosExpediente;
	private List<Integer> iDsExpedientesYaRelacionados;
	private String[] strIDSExpedientesYaRelacionados;
	
	public SeleccionarExpedienteMB() {
		 
    }
    
    

	static{
    	tiposExpediente = new String[3];
		tiposExpediente[0] = "OBRAMAYOR";
		tiposExpediente[1] = "OBRAMENOR";
        tiposExpediente[2] = "OCUPACION";
         
        sestadosExpediente = new String[10];
        sestadosExpediente[0] = "I-SUBSANACION"; //PENDIENTE DE SUBSANACIÓN INICIAL
        sestadosExpediente[1] = "T-PROVIDENCIA"; //PENDIENTE DE HACER PROVIDENCIA
        sestadosExpediente[2] = "T-INFTECNICO";  //PENDIENTE DE INFORME TÉCNICO
        sestadosExpediente[3] = "T-INFORMEJURIDICO"; //PENDIENTE DE INFORME JURÍDICO
        sestadosExpediente[4] = "T-SUBSANACION"; //PENDIENTE DE SUBSANACIÓN TRAS INFORMES 
        sestadosExpediente[5] = "F-RESOLUCION";  //PENDIENTE DE RESOLUCIÓN
        sestadosExpediente[6] = "F-NOTIFICACION"; //PENDIENTE DE NOTIFICACIÓN
        sestadosExpediente[7] = "F-ARCHIVO";     //PENDIENTE DE ARCHIVO
        sestadosExpediente[8] = "FINALIZADO";    //EXPEDIENTE FINALIZADO Y ARCHIVADO
        sestadosExpediente[9] = "seleccionar ..."; 
    }
    @PostConstruct
    public void init(){
    	logger.debug("init SeleccionarExpedienteMB (seleccionar expediente)");
    	    	
    	this.estadosExpediente = new ArrayList<String>();
    	for (int d=0;d<10;d++){
    		this.estadosExpediente.add(sestadosExpediente[d]);
    	}
    	
    	this.iDsExpedientesYaRelacionados = new ArrayList<Integer>();
 
    	
    	
    	Map<String, String[]> mapParams =   FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();

    	this.strIDSExpedientesYaRelacionados = mapParams.get("ids");

    	for (int i = 0; i < this.strIDSExpedientesYaRelacionados.length; i++) {
    		logger.debug("Excluyendo de los expedientes a seleccionar el id: "+this.strIDSExpedientesYaRelacionados[i]);
    		this.iDsExpedientesYaRelacionados.add(Integer.valueOf(this.strIDSExpedientesYaRelacionados[i]));
    	}
    			
    	extraerExpedientesBBDD();
    	    	
    }

    public void extraerExpedientesBBDD(){
		
		 
    	//Extraer Expedientes  base de datos
    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
        ExpedienteMapper expMapper = null;
        
        
        String destino=null;
//        request.setAttribute("mensaje", "Antes de entrar en faena");
        if (dbSession!=null){
            try {
              expMapper = dbSession.getMapper(ExpedienteMapper.class);
              ExpedienteExample expExample = new ExpedienteExample();
              expExample.createCriteria().andIdNotIn(iDsExpedientesYaRelacionados);
              this.expedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
                                   
            } catch (Exception e) {
				
				logger.error("Error en funcion (Extraer Expedientes  BBDD  SeleccionarExpedienteMB: "+ e.getLocalizedMessage());
                        
            }finally{
            	logger.debug("Expedientes recuperados de la base de datos, total: "+this.expedientes.size());
                dbSession.close();
            }
        }else{
        	logger.error("Extraer expedientes (SeleccionarExpedienteMB), sesion de base de datos nula ");
    	}
    }
    
    
    
    
    
    
    public List<Expediente> crearListaExpedientesRelacionados(Expediente expte){
		
		
    	logger.debug("Obtener expedientes relacionados ");
		List<ExpedienteRelacionado> erList = new ArrayList<ExpedienteRelacionado>();
	  	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ExpedienteRelacionadoMapper service = null;
        if (dbSession!=null){
            try {
              service = dbSession.getMapper(ExpedienteRelacionadoMapper.class);
              ExpedienteRelacionadoExample pExample = new ExpedienteRelacionadoExample();
              pExample.createCriteria().andIdexpedienteaEqualTo(expte.getId());
              pExample.or(pExample.createCriteria().andIdexpedientebEqualTo(expte.getId()));
              erList = (List<ExpedienteRelacionado>) service.selectByExample(pExample);
              if (erList.size()>0){
            	logger.debug("Se han encontrado "+erList.size()+" expedientes relacionados con el seleccionado");
              }else{
            	logger.debug("No se han encontrado expedientes relacionados");	
              }
              
                                     
            }catch(Exception e){
            	logger.error("Error: ",e);
                        
            }finally{
            	
              dbSession.close();
             
    		}
            
            
	    }else{
	    	System.out.println("Obtener expedientes relacionados, sesion de base de datos nula ");
	    }
    	
    	
    	
    	List<Expediente> listaExpedientes = new ArrayList<Expediente>();
		
		List<Integer> listaIds = new ArrayList<Integer>();
		Iterator<ExpedienteRelacionado> it1 = erList.iterator();
		ExpedienteRelacionado expR = new ExpedienteRelacionado();
		while(it1.hasNext()){
			expR = it1.next();
			//añadimos a la lista el id expediente que no coincida con selected, que será el relacionado.
		  	if (expR.getIdexpedientea()==expte.getId()){
		  		listaIds.add(expR.getIdexpedienteb());
		  	}else{
		  		listaIds.add(expR.getIdexpedientea());
		  	}
			
		}
		
		dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ExpedienteMapper expMapper = null;
		     
		String destino=null;
	    // request.setAttribute("mensaje", "Antes de entrar en faena");
		if (dbSession!=null){
		     try {
		          expMapper = dbSession.getMapper(ExpedienteMapper.class);
		          ExpedienteExample expExample = new ExpedienteExample();
		          expExample.createCriteria().andIdIn(listaIds);
		          listaExpedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
		     } catch (Exception e) {
		    	 logger.error("Error en funcion (crearListaExpedientesRelacionados)  SeleccionarExpedienteMB: "+ e.getLocalizedMessage());
		     
		     }finally{
		    	 
		         logger.debug("Expedientes relacionados, recuperados de la base de datos,  total: "+listaExpedientes.size());
		                dbSession.close();
		     } 
		 }else{
		   	logger.error("Error en función crearListaExpedientesRelacionados (SeleccionarExpedienteMB), sesion de base de datos nula ");
		 }
		return listaExpedientes;
	 }
	
    
    public void openInfoExpediente(Expediente expediente){
		
		CreatePdfResumenExpediente informe = new CreatePdfResumenExpediente();
		List<TramiteExpediente> listaTramites = new ArrayList<TramiteExpediente>();
		listaTramites = obtenerTramitesExpediente(expediente);
		
		List<Expediente> listaExpedientesRelacionados = new ArrayList<Expediente>();
		listaExpedientesRelacionados=this.crearListaExpedientesRelacionados(expediente);
		
		logger.debug("Generando informe del expediente "+expediente.getId());
		try {
			informe.generaPDF(expediente, listaTramites, listaExpedientesRelacionados);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error generando informe del expediente "+expediente.getId()+ " ...  ", e);
		}
		openPDF("imprimir-pdf-emodal");
		
	}
    
    public void openPDF(String dialog) {
    	
    	try {
			Map<String,Object> options = new HashMap<String, Object>();
			options.put("modal", true);
			options.put("draggable", false);
			options.put("resizable", true);
			options.put("closable", true);
			options.put("contentWidth", 1280);
			RequestContext.getCurrentInstance().openDialog(dialog,options,null);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Abriendo Informe del expediente", " ... " );
			FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    public List<TramiteExpediente> obtenerTramitesExpediente(Expediente expediente){
	  	System.out.println("Obtener trámites del expediente ");
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		TramiteExpedienteMapper service = null;
		List<TramiteExpediente> listaTramites = new ArrayList<TramiteExpediente>();
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(TramiteExpedienteMapper.class);
	              TramiteExpedienteExample pExample = new TramiteExpedienteExample();
	              pExample.createCriteria().andCodModlicExpedienteEqualTo(expediente.getId());
	              listaTramites = (List<TramiteExpediente>) service.selectByExample(pExample);
	              if (listaTramites.size()>0){
	            	System.out.println("Se han encontrado tŕamites para el expediente del que se va a imprimir informe");
	              }else{
	            	System.out.println("No se han encontrado trámites para el expediente del que se va a imprimir informe");	
	              }
	                                     
	            }catch(Exception e){
	            	logger.error("Error: ",e);
	                        
	            }finally{
	        	  dbSession.close();
	            }
	        }else{
	        	System.out.println("obtenerTramitesExpediente, sesion de base de datos nula ");
	        }
		return listaTramites;
    
  }
    
    public void selectExpedienteFromDialog(Expediente expediente) {
        RequestContext.getCurrentInstance().closeDialog(expediente);
    }
    
    
	public List<Expediente> getExpedientes() {
		return expedientes;
	}

	public void setExpedientes(List<Expediente> expedientes) {
		this.expedientes = expedientes;
	}

	public List<Expediente> getFilteredExpedientes() {
		return filteredExpedientes;
	}

	public void setFilteredExpedientes(List<Expediente> filteredExpedientes) {
		this.filteredExpedientes = filteredExpedientes;
	}

	public List<String> getEstadosExpediente() {
		return estadosExpediente;
	}

	public void setEstadosExpediente(List<String> estadosExpediente) {
		this.estadosExpediente = estadosExpediente;
	}
	public List<String> getTiposExpediente() {
		return Arrays.asList(tiposExpediente);
	}
	public List<Integer> getiDsExpedientesYaRelacionados() {
		return iDsExpedientesYaRelacionados;
	}

	public void setiDsExpedientesYaRelacionados(
			List<Integer> iDsExpedientesYaRelacionados) {
		this.iDsExpedientesYaRelacionados = iDsExpedientesYaRelacionados;
	}

	public String[] getStrIDSExpedientesYaRelacionados() {
		return strIDSExpedientesYaRelacionados;
	}

	public void setStrIDSExpedientesYaRelacionados(
			String[] strIDSExpedientesYaRelacionados) {
		this.strIDSExpedientesYaRelacionados = strIDSExpedientesYaRelacionados;
	}

	

}
