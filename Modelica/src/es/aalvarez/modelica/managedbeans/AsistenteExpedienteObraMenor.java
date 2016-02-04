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
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.aeat.valida.Validador;

import es.aalvarez.modelica.poi.GeneraProvidenciaObraMenor;
import es.aalvarez.modelica.service.ArticuloInformeJuridicoMapper;
import es.aalvarez.modelica.service.ExpedienteMapper;
import es.aalvarez.modelica.service.PuestoMapper;
import es.aalvarez.modelica.service.TramiteExpedienteMapper;
import es.aalvarez.modelica.util.MyBatisUtil;
import es.aalvarez.modelica.model.ArticuloInformeJuridico;
import es.aalvarez.modelica.model.ArticuloInformeJuridicoExample;
import es.aalvarez.modelica.model.Expediente;
import es.aalvarez.modelica.model.ExpedienteExample;
import es.aalvarez.modelica.model.Puesto;
import es.aalvarez.modelica.model.PuestoExample;
import es.aalvarez.modelica.model.TramiteExpediente;
import es.aalvarez.modelica.util.GeneraDocxE;



@ManagedBean
@ViewScoped 
public class AsistenteExpedienteObraMenor implements Serializable {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 8109547409497533679L;
	final  Logger logger = LogManager.getLogger(AsistenteExpedienteObraMenor.class);
	
	private Expediente licenciaOM = new Expediente();
	private int faseExpediente; //TODO revisar
	private boolean skip;
    
	private List<ArticuloInformeJuridico> articulosDisponibles;
    private List<ArticuloInformeJuridico> articulosInsertados;
	private ArticuloInformeJuridico articuloSeleccionado;
	
	
    private Date providenciaFecha;
    private String providenciaFirmanteCargo;
    private String providenciaFirmanteDelegacion;
    private String providenciaFirmante;
    private String providenciaTecnicoAsignado;
    private String providenciaCodId;
    
	private StreamedContent fileProvidencia;
	private boolean disabledBtnProvidencia;
	private boolean disabledBtnGuardar;//para evitar reenvío de formulario
	private List<SelectItem> spuestos;

	@PostConstruct
	private void init(){
		logger.debug("init Asistente Expediente Obra Menor");
		//campos.crearCampos();
    	//articulosInsertados = new ArrayList<ArticuloInformeJuridico>(); 
		this.disabledBtnGuardar=false;
		this.disabledBtnProvidencia=true;
		obtenerPuestosDeTrabajo();
	}
	
	public void obtenerPuestosDeTrabajo(){
		
		System.out.println("Obtener puestos de trabajo ");
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		PuestoMapper service = null;
		List<Puesto> puestos = new ArrayList<Puesto>();
        this.spuestos = new ArrayList<SelectItem>();
        String destino=null;
//      request.setAttribute("mensaje", "Antes de entrar en faena");
        if (dbSession!=null){
            try {
           
              service = dbSession.getMapper(PuestoMapper.class);
              PuestoExample pExample = new PuestoExample();
              pExample.createCriteria().andIdpuestoIsNotNull();
              puestos = (List<Puesto>) service.selectByExample(pExample);
              if (puestos.size()>0){
          		for (Puesto p : puestos){
          			if(p.getIdpuesto()>4 && p.getIdpuesto()<9){
          				spuestos.add(new SelectItem(p.getPuesto(), p.getPuesto()));
          			}
          		}
              }else{
            	  System.out.println("No se han podido extraer los puestos de trabajo de la base de datos");	
              }
                                     
            }catch(Exception e){
            	logger.error("Error: ",e);
                        
            }finally{
            	
              dbSession.close();
             
    		}
	    }else{
	    	System.out.println("Obtener Puestos de trabajo, sesion de base de datos nula ");
	    }
	}
    
        public void obtenerArticulos(){
		
		logger.debug("Obtener artículos disponibles para insertar en informe jurídico ");
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ArticuloInformeJuridicoMapper service = null;
        
        String destino=null;
//        request.setAttribute("mensaje", "Antes de entrar en faena");
        if (dbSession!=null){
            try {
           
              service = dbSession.getMapper(ArticuloInformeJuridicoMapper.class);
              ArticuloInformeJuridicoExample aIJExample = new ArticuloInformeJuridicoExample();
              aIJExample.createCriteria().andIdIsNotNull();
              this.articulosDisponibles =  service.selectByExample(aIJExample);
             
                        
            }catch(Exception e){
            	logger.error("Error: ",e);
                        
            }finally{
            	
              dbSession.close();
             
    		}
    }else{
    	logger.error("Obtener Articulos Informe Juridico, sesion de base de datos nula "+ dbSession.toString());
    	}
    }
	
		
	


	
	public String cambiarEstado() {

		return "success";
	
	}
	public void onArticuloDrop(DragDropEvent ddEvent) {
		 logger.debug("On articulo drop ddevent.getData "+ ddEvent.getData());
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
              logger.debug("Parametros: "+ nuevoEstado + " " +documento +" "+ rutaDocumento);
              service = dbSession.getMapper(ExpedienteMapper.class);
              ExpedienteExample eExample = new ExpedienteExample();
              eExample.createCriteria().andIdEqualTo(this.licenciaOM.getId());
              this.licenciaOM = (Expediente) service.selectByExample(eExample).get(0);
              this.licenciaOM.setEstadoExpediente(nuevoEstado);
              this.licenciaOM.setProvidenciaFecha(providenciaFecha);
              this.licenciaOM.setProvidenciaFirmanteCargo(providenciaFirmanteCargo);
              this.licenciaOM.setProvidenciaFirmanteDelegacion(providenciaFirmanteDelegacion);
              this.licenciaOM.setProvidenciaFirmante(providenciaFirmante);
              this.licenciaOM.setProvidenciaTecnicoAsignado(providenciaTecnicoAsignado);
              this.licenciaOM.setProvidenciaCodId(providenciaCodId);
              if (rutaDocumento!=null){
            	  switch (documento) {
  	            	case "PROVIDENCIA":
  	            		this.licenciaOM.setRutaProvidencia(rutaDocumento);
  	            		this.licenciaOM.setFaseExpediente(2);
  	            		break;
  	            	case "INFTECNICO":
  	            		this.licenciaOM.setRutaInfTecnico(rutaDocumento);
  	            		this.licenciaOM.setFaseExpediente(3);
  	            		break;
  	            	case "INFJURIDICO":
  	            		this.licenciaOM.setRutaInfJuridico(rutaDocumento);
  	            		this.licenciaOM.setFaseExpediente(4);
  	            		break;
  	            	case "RESOLUCION":
  	            		this.licenciaOM.setRutaResolucion(rutaDocumento);
  	            		this.licenciaOM.setFaseExpediente(5);
  	            		break;
  	            		
            	  }
            	}
              resultado = service.updateByPrimaryKey(licenciaOM);
              dbSession.commit(true);
            }catch(Exception e){
            	logger.error("Error actualizarEstadoLicencia obra Menor ",e);           
            }finally{
            	 FacesMessage msg1 = new FacesMessage("Estado de la Licencia actualizada", licenciaOM.getEstadoExpediente() + " Expte: " +(licenciaOM.getExpediente()+" - "+licenciaOM.getAnyo() +
            			 " Registro/s afectado/s: "+ resultado));
                 FacesContext.getCurrentInstance().addMessage(null, msg1);
              dbSession.close();
             
    		}
	    }else{
	    	logger.error("Actualizar Licencia, sesion de base de datos nula ");
	    	}
	    }
	
	private void obtenerEstadoDeFase(){ //TODO Se ha creado la funcion por error, borrar si no fuese necesaria
		 String estadoExpediente = this.licenciaOM.getEstadoExpediente();
	        switch (estadoExpediente) {
	            case "INICIO":  
	            		this.faseExpediente = 0;
	                    break;
	            case "T-PROVIDENCIA":  
	            		this.faseExpediente = 1;
	                    break;
	            case "T-INFTECNICO":  
           		this.faseExpediente = 2;
                   break;
	            case "T-INFJURIDICO":  
           		this.faseExpediente = 3;
                   break;
	            case "T-RESOLUCION":  
           		this.faseExpediente = 4;
                   break;
	            default: this.faseExpediente = 0;
	                     break;
	        }
		
	}
	
	
	public void generaProvidencia(){
    GeneraProvidenciaObraMenor docx = new GeneraProvidenciaObraMenor();
    String rutaDocumento = null;
    String nombreDocumento = null;

	
    this.actualizarEstadoLicencia("T-PROVIDENCIA", null, null);
   
    
	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
            .getExternalContext().getContext();
	String realPath = ctx.getRealPath("/");
	
	try {
		if (this.licenciaOM.getRutaProvidencia()==null){
			logger.debug("Cargo "+this.licenciaOM.getProvidenciaFirmanteCargo());
			logger.debug("Cargo "+this.providenciaFirmanteCargo);
		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
		FacesMessage msg = new FacesMessage("Completado", "ruta :" + rutaDocumento +this.licenciaOM.getProvidenciaFirmanteCargo());
    	FacesContext.getCurrentInstance().addMessage(null, msg);
    	
    	logger.debug("Asignado a ruta el valor "+rutaDocumento);
    	logger.debug("Método genera Providencia en formulario 1: ObjetoAsistenteDocumentosObraMenor "+this.toString());
    	InputStream stream =((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream(rutaDocumento);
    	logger.debug("Método genera Providencia, inputstream "+ stream);
    	nombreDocumento=this.licenciaOM.getAnyo().toString()+this.licenciaOM.getExpediente().toString()+"_Providencia.docx";
        this.fileProvidencia = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.wordprocessingml.document",nombreDocumento);
        logger.debug("Método genera Providencia, streamedContent file "+ this.fileProvidencia);
        this.disabledBtnProvidencia=false;
      
        actualizarEstadoLicencia("T-INFTECNICO","PROVIDENCIA",rutaDocumento);
    	this.setFaseExpediente(2);
        obtenerEstadoDeFase();
		TramiteExpediente tramiteProvidencia = new TramiteExpediente();
			tramiteProvidencia.setCodModlicExpediente(this.licenciaOM.getId());
			tramiteProvidencia.setFechaTramite(licenciaOM.getProvidenciaFecha());
			DateFormat df2 = DateFormat.getDateInstance(DateFormat.MEDIUM);
            String tFecha = df2.format(licenciaOM.getProvidenciaFecha());
			tramiteProvidencia.setDescripcionTramite("Providencia del "+licenciaOM.getProvidenciaFirmanteCargo() +" de fecha "+tFecha + " disponiendo la emisión de informes técnico y jurídico." );
			tramiteProvidencia.setTramiteAsignadoA(this.licenciaOM.getProvidenciaTecnicoAsignado());
			tramiteProvidencia.setEstadoTramite("T-INFTÉCNICO");
			tramiteProvidencia.setTramiteActivo(false);
			tramiteProvidencia.setIncluirEnIndice(true);
			insertarTramiteEnLicencia(tramiteProvidencia);
		
		}else{
			//el archivo ya esta generado
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	
}




	public void generaInformeJuridico(){
	    GeneraDocxE docx = new GeneraDocxE();
		
		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
	            .getExternalContext().getContext();
		String realPath = ctx.getRealPath("/");
		
		try {
	//		docx.replaceTextFound(realPath, licencia);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	//    FacesMessage msg = new FacesMessage("Successful", "Welcome :" + licencia.getExpediente());
	//    FacesContext.getCurrentInstance().addMessage(null, msg);
		
	}
	
	public void guardar() {
		
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
        
        ExpedienteMapper eMapper = null;
        String resultado = null;
        int generatedID = -1;
        licenciaOM.setTipoExpediente("OBRAMENOR");
        licenciaOM.setEstadoExpediente("T-PROVIDENCIA");
          
        
        if (dbSession!=null){
            try {
            
				logger.debug("Creando nueva licencia ... "+licenciaOM.getExpediente()+" - "+licenciaOM.getAnyo() );
				eMapper = dbSession.getMapper(ExpedienteMapper.class);
				eMapper.insert(licenciaOM);
				generatedID = licenciaOM.getId();
				logger.debug("El ID asignado a la licencia es "+ generatedID );
				//Insertamos el trámite en la tabla trámites
				    TramiteExpedienteMapper service = dbSession.getMapper(TramiteExpedienteMapper.class);
				    TramiteExpediente tramiteInicial = new TramiteExpediente();
				    tramiteInicial.setCodModlicExpediente(generatedID);
				    tramiteInicial.setFechaTramite(licenciaOM.getFechaEntrada());
				    tramiteInicial.setDescripcionTramite("El interesado presenta solicitud de licencia de obras (RE "+licenciaOM.getNumEntrada()+").");
				    tramiteInicial.setTramiteAsignadoA("ADVO. SECRETARÍA");
				    tramiteInicial.setEstadoTramite("T-PROVIDENCIA");
				    tramiteInicial.setTramiteActivo(true);
				    tramiteInicial.setIncluirEnIndice(true);
				    service.insert(tramiteInicial);
				dbSession.commit(true);
			} catch (Exception e) {
		
				logger.error("Error en funcion (guardar) AsistenteExpedienteObraMenor: "+ e.getLocalizedMessage());
			       
        	}finally{
        		this.disabledBtnGuardar=true;
        		this.disabledBtnProvidencia=false;

            	FacesMessage msg = new FacesMessage("Licencia creada ", "ID= "+ generatedID + " - " +licenciaOM.getInteresado());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                                        
    		}
        }
	
			
        if (dbSession!=null){dbSession.close();} //por si no se ejecutara la operación y quedara la conexión abierta
        //return resultado = "success";
	}
	public void insertarTramiteEnLicencia(TramiteExpediente tramite){
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
        
       
        String resultado = null;
        int generatedID = -1;
                 
        
        if (dbSession!=null){
            try {
            logger.debug("Insertando trámite en tiempo de ejecución en el expediente... "+tramite.getCodModlicExpediente());
            //Insertamos el trámite en la tabla trámites
	            TramiteExpedienteMapper service = dbSession.getMapper(TramiteExpedienteMapper.class);
	            service.insert(tramite);
	            dbSession.commit(true);
            } catch (Exception e) {
				
				logger.error("Error en funcion (insertarTramiteEnLicencia) AsistenteExpedienteObraMenor: "+ e.getLocalizedMessage());
            
            }finally{
            	
            	 FacesMessage msg = new FacesMessage("Licencia modificada ", "ID= "+ generatedID + " - " +licenciaOM.getInteresado());
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
	
	public void validateNIF(FacesContext context, 
            UIComponent inputComponent,
            Object value) throws ValidatorException {
	      
		 Validador validador = new Validador();
		 int e = validador.checkNif(String.valueOf(value));
		 if (e <= 0){
		 System.out.println("MAL");
		 	throw new ValidatorException(new FacesMessage("NIF, CIF o NIE incorrecto "));
		 }
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
   	logger.debug("desde .." + step2);
   	logger.debug("hacia .." + step);
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
        	
        	logger.debug("hacia fase ...." + this.faseExpediente);
        	//RequestContext.getCurrentInstance().update("statusForm");            
        	return event.getNewStep();
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
    	logger.debug("Método getFile en formulario 2: ObjetoAsistenteDocumentosObraMenor "+this.toString());
    	//logger.debug("Get file, ruta: "+ this.ruta);
    	logger.debug("Get file, ruta: "+ this.fileProvidencia.getStream());
    	
    	
    	return fileProvidencia;
    }
	public void setFileProvidencia(StreamedContent fileProvidencia) {
		this.fileProvidencia = fileProvidencia;
	}
	public boolean isDisabledBtnProvidencia() {
		return disabledBtnProvidencia;
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
	/*public String getFechaFirmaProvidencia() {
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
	}*/
	public Expediente getLicenciaOM() {
		return licenciaOM;
	}

	public void setLicenciaOM(Expediente licenciaOM) {
		this.licenciaOM = licenciaOM;
	}

	public Date getProvidenciaFecha() {
		return providenciaFecha;
	}

	public void setProvidenciaFecha(Date providenciaFecha) {
		this.providenciaFecha = providenciaFecha;
	}

	public String getProvidenciaFirmanteCargo() {
		return providenciaFirmanteCargo;
	}

	public void setProvidenciaFirmanteCargo(String providenciaFirmanteCargo) {
		this.providenciaFirmanteCargo = providenciaFirmanteCargo;
	}

	public String getProvidenciaFirmanteDelegacion() {
		return providenciaFirmanteDelegacion;
	}

	public void setProvidenciaFirmanteDelegacion(
			String providenciaFirmanteDelegacion) {
		this.providenciaFirmanteDelegacion = providenciaFirmanteDelegacion;
	}

	public String getProvidenciaFirmante() {
		return providenciaFirmante;
	}

	public void setProvidenciaFirmante(String providenciaFirmante) {
		this.providenciaFirmante = providenciaFirmante;
	}

	public String getProvidenciaTecnicoAsignado() {
		return providenciaTecnicoAsignado;
	}

	public void setProvidenciaTecnicoAsignado(String providenciaTecnicoAsignado) {
		this.providenciaTecnicoAsignado = providenciaTecnicoAsignado;
	}

	public String getProvidenciaCodId() {
		return providenciaCodId;
	}

	public void setProvidenciaCodId(String providenciaCodId) {
		this.providenciaCodId = providenciaCodId;
	}
	public boolean isDisabledBtnGuardar() {
		return disabledBtnGuardar;
	}

	public void setDisabledBtnGuardar(boolean disabledBtnGuardar) {
		this.disabledBtnGuardar = disabledBtnGuardar;
	}

	public List<SelectItem> getSpuestos() {
		return spuestos;
	}

	public void setSpuestos(List<SelectItem> spuestos) {
		this.spuestos = spuestos;
	}
   
}
