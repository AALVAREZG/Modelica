package es.aalvarez.modelica.managedbeans;

import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import es.aalvarez.modelica.poi.GeneraInformeJuridicoObraMayor;
import es.aalvarez.modelica.poi.GeneraProvidenciaObraMayor;
import es.aalvarez.modelica.service.ArticuloInformeJuridicoMapper;
import es.aalvarez.modelica.service.ExpedienteMapper;
import es.aalvarez.modelica.util.MyBatisUtil;
import es.aalvarez.modelica.model.ArticuloInformeJuridico;
import es.aalvarez.modelica.model.ArticuloInformeJuridicoExample;
import es.aalvarez.modelica.model.Expediente;
import es.aalvarez.modelica.model.ExpedienteExample;
import es.aalvarez.modelica.util.GeneraDocxE;



@ManagedBean
@ViewScoped 
public class AsistenteEditarObraMayor implements Serializable {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 8109547409497533679L;
	final  Logger logger = LogManager.getLogger(AsistenteEditarObraMayor.class);

	
	private Expediente licenciaOM = new Expediente();
	private int faseExpediente; //TODO revisar
	private boolean skip;
    
	private List<ArticuloInformeJuridico> articulosDisponibles;
    private List<ArticuloInformeJuridico> articulosInsertados;
	private ArticuloInformeJuridico articuloSeleccionado;
	
	//variables para cumplimentar la providencia
    private String fechaFirmaProvidencia;
    private String lineaFirmaProvidencia1;
    private String lineaFirmaProvidencia2;
    private String firmanteProvidencia;
    private String tecnicoAsignado;
    private String codigoOrden;
	private StreamedContent fileProvidencia;
	private boolean disabledBtnProvidencia;
	
	@PostConstruct
	private void init(){
		//campos.crearCampos();
    	articulosInsertados = new ArrayList<ArticuloInformeJuridico>();
    	this.licenciaOM = (Expediente) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("expediente");
    	obtenerFecha1();
    	
    	//System.out.println("PostConstruct  "+this.licenciaOM.getId());
	}
	
	public void establecerFaseProvidencia(){
		this.licenciaOM.setFaseExpediente(1);
		this.licenciaOM.setEstadoExpediente("T-PROVIDENCIA");
		FacesMessage msg = new FacesMessage("Expediente Actualizado " + "Fase actual: Providencia");
    	FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	public void establecerFaseInfTecnico(){
		this.licenciaOM.setFaseExpediente(2);
		this.licenciaOM.setEstadoExpediente("T-INFTECNICO");
		FacesMessage msg = new FacesMessage("Expediente Actualizado " + "Fase actual: Inf-Tecnico");
    	FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void obtenerFecha1(){
		Date now = new Date();
		
		DateFormat df3 = DateFormat.getDateInstance(DateFormat.LONG);
		String s3 = df3.format(now);
	
		System.out.println("(LONG)    Hoy es:" + s3);
		this.fechaFirmaProvidencia =s3;
		
	}
    public void obtenerArticulos(){
		
		System.out.println("Obtener artículos disponibles para insertar en informe jurídico ");
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ArticuloInformeJuridicoMapper service = null;
        
        String destino=null;
//        request.setAttribute("mensaje", "Antes de entrar en faena");
        if (dbSession!=null){
            try {
           
              service = dbSession.getMapper(ArticuloInformeJuridicoMapper.class);
              ArticuloInformeJuridicoExample aIJExample = new ArticuloInformeJuridicoExample();
              aIJExample.createCriteria().andIdIsNotNull();
              this.articulosDisponibles =  service.selectByExampleWithBLOBs(aIJExample);
            }catch(Exception e){
            	logger.error("Error: ",e);
                        
            }finally{
            	
              dbSession.close();
             
    		}
    }else{
    	System.out.println("Obtener Articulos Informe Juridico, sesion de base de datos nula "+ dbSession.toString());
    	}
    }
	
		
	


	
	public String cambiarEstado() {

		return "success";
	
	}
	public void onArticuloDrop(DragDropEvent ddEvent) {
		 System.out.println("On articulo drop ddevent.getData "+ ddEvent.getData());
		 
		 ArticuloInformeJuridico art = ((ArticuloInformeJuridico) ddEvent.getData());
		 articulosInsertados.add(art);
		 articulosDisponibles.remove(art);
		 
	    }
	
	public void actualizarEstadoLicencia(String nuevoEstado, String documento, String rutaDocumento){
		
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ExpedienteMapper service = null;
		int resultado = 0;
        String destino=null;
//        request.setAttribute("mensaje", "Antes de entrar en faena");
        if (dbSession!=null){
            try {
               System.out.println("Parametros: "+ nuevoEstado + " " +documento +" "+ rutaDocumento);
              service = dbSession.getMapper(ExpedienteMapper.class);
              ExpedienteExample eExample = new ExpedienteExample();
              eExample.createCriteria().andIdEqualTo(this.licenciaOM.getId());
              this.licenciaOM = (Expediente) service.selectByExample(eExample).get(0);
              //this.licenciaOM.setEstadoExpediente(nuevoEstado);
              if (rutaDocumento!=null){
            	  switch (documento) {
	            	case "PROVIDENCIA":
	            		this.licenciaOM.setRutaProvidencia(rutaDocumento);
	            		//this.licenciaOM.setFaseExpediente(2);
	            		break;
	            	case "INFTECNICO":
	            		this.licenciaOM.setRutaInfTecnico(rutaDocumento);
	            		//this.licenciaOM.setFaseExpediente(3);
	            		break;
	            	case "INFJURIDICO":
	            		this.licenciaOM.setRutaInfJuridico(rutaDocumento);
	            		//this.licenciaOM.setFaseExpediente(4);
	            		break;
	            	case "RESOLUCION":
	            		this.licenciaOM.setRutaResolucion(rutaDocumento);
	            		//this.licenciaOM.setFaseExpediente(5);
	            		break;
	            	}
            	}
              resultado = service.updateByPrimaryKey(licenciaOM);
              dbSession.commit(true);
            }catch(Exception e){
            	logger.error("Error Actualizando Estado de licencia de obra Mayor ",e);
                        
            }finally{
            	 FacesMessage msg1 = new FacesMessage("Licencia actualizada", licenciaOM.getEstadoExpediente() + " Expte: " +(licenciaOM.getExpediente()+" - "+licenciaOM.getAnyo() +
            			 " Registro/s afectado/s: "+ resultado));
                 FacesContext.getCurrentInstance().addMessage(null, msg1);
              dbSession.close();
             
    		}
	    }else{
	    	System.out.println("Actualizar Licencia, sesion de base de datos nula ");
	    	}
	    }
	
	private int obtenerEstadoDeFase(){ //TODO Se ha creado la funcion por error, borrar si no fuese necesaria
		 String estadoExpediente = this.licenciaOM.getEstadoExpediente();
	        switch (estadoExpediente) {
	            case "INICIO":  
	            		this.faseExpediente = 0;
	            		return 0;
	                    
	            case "T-PROVIDENCIA":  
	            		this.faseExpediente = 1;
	            		return 1;
	            case "T-INFTECNICO":  
           		this.faseExpediente = 2;
           		return 2;
	            case "T-INFJURIDICO":  
           		this.faseExpediente = 3;
           		return 3;
	            case "T-RESOLUCION":  
           		this.faseExpediente = 4;
           		return 4;
	            default: this.faseExpediente = 0;
	            return 0;
	        }
		
	}
	public void generaProvidencia(){
    GeneraProvidenciaObraMayor docx = new GeneraProvidenciaObraMayor();
    String rutaDocumento = null;
    String nombreDocumento = null;

	
    this.actualizarEstadoLicencia("T-PROVIDENCIA", null, null);
   
    
	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
            .getExternalContext().getContext();
	String realPath = ctx.getRealPath("/");
	
	try {
		if (this.licenciaOM.getRutaProvidencia()!=null){
		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
		FacesMessage msg = new FacesMessage("Completado", "ruta :" + rutaDocumento);
    	FacesContext.getCurrentInstance().addMessage(null, msg);
    	
    	System.out.println("Asignado a ruta el valor "+rutaDocumento);
    	System.out.println("Método genera Providencia en formulario 1: ObjetoAsistenteDocumentosObraMayor "+this.toString());
    	InputStream stream =((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream(rutaDocumento);
    	System.out.println("Método genera Providencia, inputstream "+ stream);
    	nombreDocumento=this.licenciaOM.getAnyo().toString()+this.licenciaOM.getExpediente().toString()+"_Providencia.docx";
        this.fileProvidencia = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.wordprocessingml.document",nombreDocumento);
        System.out.println("Método genera Providencia, streamedContent file "+ this.fileProvidencia);
        this.disabledBtnProvidencia=false;
        //TODO probar cambio de fase en este punto
        this.faseExpediente=2;
        actualizarEstadoLicencia("T-INFTECNICO","PROVIDENCIA",rutaDocumento);
    	this.licenciaOM.setFaseExpediente(3);
      
		}else{
			
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	
}




public void generaInformeJuridico(){
	GeneraInformeJuridicoObraMayor docx = new GeneraInformeJuridicoObraMayor();
    String rutaDocumento = null;
    String nombreDocumento = null;

	
    this.actualizarEstadoLicencia("T-INFJURIDICO", null, null);
   
    
	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
            .getExternalContext().getContext();
	String realPath = ctx.getRealPath("/");
	
	try {
		if (true){
		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM, this.articulosInsertados);
		FacesMessage msg = new FacesMessage("Completado", "ruta :" + rutaDocumento);
    	FacesContext.getCurrentInstance().addMessage(null, msg);
    	
    	System.out.println("Asignado a ruta el valor "+rutaDocumento);

    	InputStream stream =((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream(rutaDocumento);

    	nombreDocumento=this.licenciaOM.getAnyo().toString()+this.licenciaOM.getExpediente().toString()+"_Providencia.docx";
        this.fileProvidencia = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.wordprocessingml.document",nombreDocumento);
        actualizarEstadoLicencia("T-INFTECNICO","PROVIDENCIA",rutaDocumento);
    	this.licenciaOM.setFaseExpediente(3);
      	}else{
			
		}
	} catch (Exception e) {
		e.printStackTrace();
	}	
}
	public void guardar() {
		
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
        
        ExpedienteMapper eMapper = null;
        String resultado = null;
        int generatedID = -1;
        int resultadoActualizacion = -1;
        
          
        
        if (dbSession!=null){
            try {
            System.out.println("Guardando expediente ... "+licenciaOM.getExpediente()+" - "+licenciaOM.getAnyo() );
            eMapper = dbSession.getMapper(ExpedienteMapper.class);
            ExpedienteExample expExample = new ExpedienteExample();
            expExample.createCriteria().andIdEqualTo(licenciaOM.getId());
           
            resultadoActualizacion = eMapper.updateByPrimaryKey(licenciaOM);
            dbSession.commit(true);
            }catch(Exception e){
            	logger.error("Error Guardando licencia de obra Mayor ",e);
            	
            }finally{
            	
            	 FacesMessage msg = new FacesMessage("Registros Afectados: " + resultadoActualizacion );
                 FacesContext.getCurrentInstance().addMessage(null, msg);
                                        
    		}
        }
		
        if (dbSession!=null){dbSession.close();} //por si no se ejecutara la operación y quedara la conexión abierta
        //return resultado = "success";
	}
	
	public void generaProvidencia_old(){
        GeneraDocxE docx = new GeneraDocxE();
    	
    	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
    	String realPath = ctx.getRealPath("/");
    	
    	try {
			docx.replaceTextFound(realPath, licenciaOM);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + licenciaOM.getExpediente());
        FacesContext.getCurrentInstance().addMessage(null, msg);
		
	}
	
	public void save() {       
    	GeneraDocxE docx = new GeneraDocxE();
    	
    	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
    	String realPath = ctx.getRealPath("/");
    	
    	try {
//			docx.replaceTextFound(realPath, licencia);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + licenciaOM.getExpediente());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
        
    public boolean isSkip() {
        return skip;
    }
 
    public void setSkip(boolean skip) {
        this.skip = skip;
    }
     
    public String onFlowProcess(FlowEvent event) {
    	  	String step = event.getNewStep();
    	  	
   	String step2 = event.getOldStep();
   	System.out.println("desde .." + step2);
   	System.out.println("hacia .." + step);
   	
        if(skip) {
            skip = false;   //reset in case licencia goes back
            return "confirm";
        }
        else {
        	if (step.equals(new String("T-INFJURIDICO"))){
        		this.obtenerArticulos();
        	}
        	//TODO estudiar si es conveniente el cambio de fase en este punto
        	//o a la hora de generar y obtener los documentos
        	
        	System.out.println("hacia fase ...." + this.faseExpediente);
        	RequestContext.getCurrentInstance().update("statusForm");            return event.getNewStep();
        }
    }
    
    public List<ArticuloInformeJuridico> getArticulosDisponibles() {
		return articulosDisponibles;
	}
	public void setArticulosDisponibles(
			List<ArticuloInformeJuridico> articulosDisponibles) {
		this.articulosDisponibles = articulosDisponibles;
	}
	public List<ArticuloInformeJuridico> getArticulosInsertados() {
		return articulosInsertados;
	}
	public void setArticulosInsertados(
			List<ArticuloInformeJuridico> articulosInsertados) {
		this.articulosInsertados = articulosInsertados;
	}
	public ArticuloInformeJuridico getArticuloSeleccionado() {
		return articuloSeleccionado;
	}
	public void setArticuloSeleccionado(ArticuloInformeJuridico articuloSeleccionado) {
		this.articuloSeleccionado = articuloSeleccionado;
	}

	public StreamedContent getFileProvidencia() {
    	System.out.println("Método getFile en formulario 2: ObjetoAsistenteDocumentosObraMayor "+this.toString());
    	//System.out.println("Get file, ruta: "+ this.ruta);
    	System.out.println("Get file, ruta: "+ this.fileProvidencia.getStream());
    	
    	
    	return fileProvidencia;
    }
	public void setFileProvidencia(StreamedContent fileProvidencia) {
		this.fileProvidencia = fileProvidencia;
	}
	public boolean isDisabledBtnProvidencia() {
		System.out.println("Ruta Providencia ..." + this.licenciaOM.getRutaProvidencia());
		return (this.licenciaOM.getRutaProvidencia()==null);
	}
	public void setDisabledBtnProvidencia(boolean disabledBtnProvidencia) {
		this.disabledBtnProvidencia = disabledBtnProvidencia;
	}
	public int getFaseExpediente() {
		return faseExpediente;
	}
	public void setFaseExpediente(int faseExpediente) {
		this.faseExpediente = faseExpediente;
	}
	public String getFechaFirmaProvidencia() {
		return fechaFirmaProvidencia;
	}
	public void setFechaFirmaProvidencia(String fechaFirmaProvidencia) {
		this.fechaFirmaProvidencia = fechaFirmaProvidencia;
	}
	public String getLineaFirmaProvidencia1() {
		return lineaFirmaProvidencia1;
	}
	public void setLineaFirmaProvidencia1(String lineaFirmaProvidencia1) {
		this.lineaFirmaProvidencia1 = lineaFirmaProvidencia1;
	}
	public String getLineaFirmaProvidencia2() {
		return lineaFirmaProvidencia2;
	}
	public void setLineaFirmaProvidencia2(String lineaFirmaProvidencia2) {
		this.lineaFirmaProvidencia2 = lineaFirmaProvidencia2;
	}
	public String getFirmanteProvidencia() {
		return firmanteProvidencia;
	}
	public void setFirmanteProvidencia(String firmanteProvidencia) {
		this.firmanteProvidencia = firmanteProvidencia;
	}
	public String getTecnicoAsignado() {
		return tecnicoAsignado;
	}
	public void setTecnicoAsignado(String tecnicoAsignado) {
		this.tecnicoAsignado = tecnicoAsignado;
	}
	public String getCodigoOrden() {
		return codigoOrden;
	}
	public void setCodigoOrden(String codigoOrden) {
		this.codigoOrden = codigoOrden;
	}
	public Expediente getLicenciaOM() {
		return licenciaOM;
	}
	public void setLicenciaOM(Expediente licenciaOM) {
		this.licenciaOM = licenciaOM;
	}
   
}
