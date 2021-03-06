package es.aalvarez.modelica.managedbeans;

import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.aeat.valida.Validador;

import es.aalvarez.modelica.poi.GeneraProvidenciaObraMayor;
import es.aalvarez.modelica.poi.GeneraProvidenciaObraMenor;
import es.aalvarez.modelica.poi.GeneraProvidenciaOcupacion;
import es.aalvarez.modelica.poi.GeneraProvidenciaOtros;
import es.aalvarez.modelica.poi.GeneraProvidenciaSegregacion;
import es.aalvarez.modelica.service.ArticuloInformeJuridicoMapper;
import es.aalvarez.modelica.service.ExpedienteMapper;
import es.aalvarez.modelica.service.ExpedienteRelacionadoMapper;
import es.aalvarez.modelica.service.PuestoMapper;
import es.aalvarez.modelica.service.TramiteExpedienteMapper;
import es.aalvarez.modelica.util.MyBatisUtil;
import es.aalvarez.modelica.model.ArticuloInformeJuridico;
import es.aalvarez.modelica.model.ArticuloInformeJuridicoExample;
import es.aalvarez.modelica.model.Expediente;
import es.aalvarez.modelica.model.ExpedienteExample;
import es.aalvarez.modelica.model.ExpedienteRelacionado;
import es.aalvarez.modelica.model.ExpedienteRelacionadoExample;
import es.aalvarez.modelica.model.Puesto;
import es.aalvarez.modelica.model.PuestoExample;
import es.aalvarez.modelica.model.TramiteExpediente;
import es.aalvarez.modelica.model.TramiteExpedienteExample;
import es.aalvarez.modelica.util.GeneraDocxE;



@ManagedBean
@ViewScoped 
public class AsistenteExpedienteObraMayor implements Serializable {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 8109547409497533679L;
	final  Logger logger = LogManager.getLogger(AsistenteExpedienteObraMayor.class);
	
	private Expediente licenciaOM = new Expediente();
	private String tipoExpediente;
	
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
	 
	FacesContext facesContext;
	Boolean editar;
	
	private List<ExpedienteRelacionado> expedientesRelacionados;

	

	@PostConstruct
	private void init(){
		System.out.println("Inicianizando FACESCONTEXT .... ");
    	facesContext = FacesContext.getCurrentInstance();
    	//System.out.println("Tamaño de la lista de atributos .... " + facesContext.getExternalContext().getRequestParameterMap().size());
		this.tipoExpediente = (String) facesContext.getCurrentInstance().getExternalContext().getFlash().get("tipo");
		logger.debug("init AsistenteExpedienteObraMayor, con tipo de expediente: "+this.tipoExpediente);
		
		//campos.crearCampos();
    	//articulosInsertados = new ArrayList<ArticuloInformeJuridico>(); 
		
		this.disabledBtnGuardar=false;
		this.disabledBtnProvidencia=true;
		obtenerPuestosDeTrabajo();
		
		///MANEJANDO DIALOGO CUANDO ESTÁ EN MODO EDICIÓN
		this.editar=false;
		
    	if (facesContext.getExternalContext().getRequestParameterMap().get("modo")!=null){
    		String modo = facesContext.getExternalContext().getRequestParameterMap().get("modo");
    				if (modo.compareTo("editar")==0){
    		    		this.editar=true;
    		    		Integer idExpediente = Integer.valueOf(facesContext.getExternalContext().getRequestParameterMap().get("expediente"));
    		    		obtenerExpedienteDesdeId(idExpediente);
    		    		recuperarTramitesRelacionados(idExpediente);
    		    		this.disabledBtnProvidencia=false;
    		    			this.providenciaFecha=this.licenciaOM.getProvidenciaFecha();
    		    			this.providenciaFirmanteCargo = this.licenciaOM.getProvidenciaFirmanteCargo();
    		    			this.providenciaFirmanteDelegacion = this.licenciaOM.getProvidenciaFirmanteDelegacion();
    		    			this.providenciaFirmante=this.licenciaOM.getProvidenciaFirmante();
    		    			this.providenciaTecnicoAsignado=this.licenciaOM.getProvidenciaTecnicoAsignado();
    		    			this.providenciaCodId=this.licenciaOM.getProvidenciaCodId();
    		    		this.tipoExpediente=this.licenciaOM.getTipoExpediente();
    		    		logger.debug("Modo editar: tipoExpediente"+this.tipoExpediente);
    		    			
    		    	}else{
    		    		 this.expedientesRelacionados = new ArrayList<ExpedienteRelacionado>();
    		    	}
    	}
    	
    	   	logger.debug("¿Modo edicion? :" + String.valueOf(this.editar) + " tipo expediente");
    	   	
	}
	
	public void recuperarTramitesRelacionados(Integer idExpediente){
		  
	  	System.out.println("Obtener trámites relacionados del expediente ");
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ExpedienteRelacionadoMapper service = null;
        if (dbSession!=null){
            try {
              service = dbSession.getMapper(ExpedienteRelacionadoMapper.class);
              ExpedienteRelacionadoExample pExample = new ExpedienteRelacionadoExample();
              pExample.createCriteria().andIdrelacionEqualTo(idExpediente);
              this.expedientesRelacionados = (List<ExpedienteRelacionado>) service.selectByExample(pExample);
              if (this.expedientesRelacionados.size()>0){
            	logger.debug("Se han encontrado tŕamites para el expediente");
              }else{
            	logger.debug("No se han encontrado trámites para el expediente");	
              }
                                     
            }catch(Exception e){
            	logger.error("Error: ",e);
                        
            }finally{
            	
              dbSession.close();
             
    		}
    }else{
    	System.out.println("Obtener expedientes relacionados, sesion de base de datos nula ");
    }
    
   }
	public void actualizarExpedientesRelacionados(){
	   	logger.debug("Actualizando Expedientes Relacionados... "); 
	  	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ExpedienteRelacionadoMapper service = null;
		int nuevos = 0;
		int editados = 0;
		int generatedID;
		
        if (dbSession!=null){
            try {
              service = dbSession.getMapper(ExpedienteRelacionadoMapper.class);
              ExpedienteRelacionado er = new ExpedienteRelacionado();
              for (int p=0;p<this.expedientesRelacionados.size();p++){
              		er = this.expedientesRelacionados.get(p);
              		
              		if (er.getIdrelacion()<0){
              			//tramite nuevo, insertar
              			nuevos=nuevos+service.insert(er);
	              		generatedID = er.getIdrelacion();
	              		
              		}else{
              			//editar trámite
              			 editados = editados+ service.updateByPrimaryKey(er);
              		}
            }
          	
          	logger.debug("Actualizando Expediente (pendiente de commit), con los siguientes registros relacionados: nuevos ("+nuevos +"), editados ("+editados+")");
          		dbSession.commit(true);
            } catch (Exception e) {
				
            	logger.error("Error en funcion (actualizarTramitesExpediente) ViewLicenciasMB: "+ e.getLocalizedMessage());
                        
            }finally{
            	 logger.debug("Expediente Relacionados Actualizado!");
            	            	 
                 dbSession.close();
                 
    		}

        }else{
	    	logger.error("Actualizar Expedientes relacionados, sesion de base de datos nula ");
	    }
	    
  }
	
	public void obtenerExpedienteDesdeId(Integer idExpediente){
		logger.debug("Obtener Expediente Desde ID ");
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ExpedienteMapper service = null;
        if (dbSession!=null){
            try {
           
              service = dbSession.getMapper(ExpedienteMapper.class);
              ExpedienteExample eExample = new ExpedienteExample();
              eExample.createCriteria().andIdEqualTo(idExpediente);
              
              this.licenciaOM = service.selectByExampleWithBLOBs(eExample).get(0);
            
            }catch(Exception e){
            	logger.error("Error obtenerExpedienteDesdeId ",e);       
                                     
            }finally{
            	
              dbSession.close();
             
    		}
	    }else{
	    	logger.error("Error obtenerExpedienteDesde Id , sesion de base de datos nula ");
	    }
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
            	logger.error("Error obtenerPuestosDeTrabajo: No se han podido extraer los puestos de trabajo de la base de datos ",e);       
                                     
            }finally{
            	
              dbSession.close();
             
    		}
	    }else{
	    	logger.error("Obtener Puestos de trabajo, sesion de base de datos nula ");
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
            	logger.error("Error obtenerArticulos:  ",e);       
                                     
            }finally{
            	
              dbSession.close();
             
    		}
    }else{
    	logger.error("Obtener Articulos Informe Juridico, sesion de base de datos nula "+ dbSession.toString());
    	}
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
            	logger.error("Error actualizarEstadoLicencia ",e);           
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
	
	
	
	
	public void generaProvidencia(){
	
		
		String rutaDocumento = null;
		String nombreDocumento = null;
		this.actualizarEstadoLicencia("T-PROVIDENCIA", null, null);
		
		try {
		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String realPath = ctx.getRealPath("/");
		if (this.licenciaOM.getRutaProvidencia()==null || this.editar==true){
			logger.debug("Generando providencia.. RE:  "+this.licenciaOM.getNumEntrada()+"-"+this.licenciaOM.getAnyo());
			
		
		switch (licenciaOM.getTipoExpediente()) {
		  	//añadimos los corchetes en cada CASE para que el scope de docx se limite a los mismos,
		  	//si no lo hacemos nos da el error docx, duplicate variable
        	case "OBRAMAYOR":{ 
        		GeneraProvidenciaObraMayor docx = new GeneraProvidenciaObraMayor();
        		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
        		break;
        	}
        	case "OBRAmenor": { 
        		GeneraProvidenciaObraMenor docx = new GeneraProvidenciaObraMenor();
        		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
        		break;
        	}
        	case "OCUPACION": {
        		GeneraProvidenciaOcupacion docx = new GeneraProvidenciaOcupacion();
        		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
        		break;
        	}
        	case "SEGREGACION": {
        		GeneraProvidenciaSegregacion docx = new GeneraProvidenciaSegregacion();
        		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
        		break;
        	}
        	case "PLANEAMIENTO": {
        		GeneraProvidenciaOtros docx = new GeneraProvidenciaOtros();
        		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
        		break;
        	}
        	case "CERTIFICADO": {
        		GeneraProvidenciaOtros docx = new GeneraProvidenciaOtros();
        		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
        		break;
        	}
        	case "OTROS": {
        		GeneraProvidenciaOtros docx = new GeneraProvidenciaOtros();
        		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
        		break;
        	}
        	default:
        		GeneraProvidenciaOtros docx = new GeneraProvidenciaOtros();
        		rutaDocumento = docx.replaceTextFound(realPath, this.licenciaOM);
        		break;
        		
       }
		FacesMessage msg = new FacesMessage("Completado", "ruta :" + rutaDocumento +this.licenciaOM.getProvidenciaFirmanteCargo());
    	FacesContext.getCurrentInstance().addMessage(null, msg);
    	
    	logger.debug("Asignado a ruta el valor "+rutaDocumento);
    	logger.debug("Método genera Providencia en formulario 1: ObjetoAsistenteDocumentosObraMayor "+this.toString());
    	InputStream stream =((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream(rutaDocumento);
    	logger.debug("Método genera Providencia, inputstream "+ stream);
    	if (this.licenciaOM.getExpediente()!=null){
    		nombreDocumento=this.licenciaOM.getAnyo().toString()+this.licenciaOM.getExpediente().toString()+"_Providencia.docx";
    	}else if(this.licenciaOM.getNumEntrada()!=null){
    		nombreDocumento=this.licenciaOM.getAnyo().toString()+"RE"+this.licenciaOM.getAnyo()+String.format("%04d",licenciaOM.getNumEntrada())+"_Providencia.docx";
    	}else{
    		SimpleDateFormat formatter = new SimpleDateFormat("Mdy");
    		String date = formatter.format(this.licenciaOM.getFechaEntrada());
    		nombreDocumento=this.licenciaOM.getAnyo().toString()+"RE"+date+"_Providencia.docx";
    	}
        this.fileProvidencia = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.wordprocessingml.document",nombreDocumento);
        logger.debug("Método genera Providencia, streamedContent file "+ this.fileProvidencia);
        this.disabledBtnProvidencia=false;
      
        //actualizarEstadoLicencia("T-INFTECNICO","PROVIDENCIA",rutaDocumento);
//    	this.setFaseExpediente(2);
//        obtenerEstadoDeFase();
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
			//nueva licencia, el archivo ya se ha generado
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
        licenciaOM.setTipoExpediente(this.tipoExpediente);
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
				    tramiteInicial.setTramiteAsignadoA("ADMINISTRATIVOS SECRETARÍA");
				    tramiteInicial.setEstadoTramite("T-PROVIDENCIA");
				    tramiteInicial.setTramiteActivo(true);
				    tramiteInicial.setIncluirEnIndice(true);
				    service.insert(tramiteInicial);
				dbSession.commit(true);
			} catch (Exception e) {
		
				logger.error("Error en funcion (guardar) AsistenteExpedienteObraMayor: "+ e.getLocalizedMessage());
				FacesMessage msg = new FacesMessage("Error al guardar licencia ", "ID= "+ generatedID + " - " +licenciaOM.getInteresado());
                FacesContext.getCurrentInstance().addMessage(null, msg);
			       
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
	
	public void actualizar(){
		
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		ExpedienteMapper service = null;
		int resultado = 0;
        if (dbSession!=null){
            try {
              logger.debug("Actualizando Expediente "+(this.tipoExpediente)+" ... " + this.licenciaOM.getId()+" Expte: " + licenciaOM.getExpediente()+" - "+licenciaOM.getAnyo());
              service = dbSession.getMapper(ExpedienteMapper.class);
//              ExpedienteExample eExample = new ExpedienteExample();
//              eExample.createCriteria().andIdEqualTo(this.licenciaOM.getId());
              logger.debug("Observaciones" + this.licenciaOM.getObservaciones());
              resultado = service.updateByPrimaryKeySelective(this.licenciaOM);
              dbSession.commit(true);
            }catch(Exception e){
            	logger.error("Error actualizar Licencia ",e);           
            }finally{
            	 
            	 FacesMessage msg1 = new FacesMessage("Licencia actualizada", licenciaOM.getEstadoExpediente() + " Expte: " +(licenciaOM.getExpediente()+" - "+licenciaOM.getAnyo() +
            			 " Registro/s afectado/s: "+ resultado));
                 FacesContext.getCurrentInstance().addMessage(null, msg1);
              dbSession.close();
             
    		}
	    }else{
	    	logger.error("Actualizar Licencia, sesion de base de datos nula ");
	    	}
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
				
				logger.error("Error en funcion (insertarTramiteEnLicencia) AsistenteExpedienteObraMayor: "+ e.getLocalizedMessage());
            
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
    	//logger.debug("desde .." + step2 + " ---> hacia .." + step);
   	
        if(skip) {
            skip = false;   //reset in case licencia goes back
            return "confirm";
        }
        else {
        	if (step.equals(new String("T-INFJURIDICO"))){
        		this.obtenerArticulos();
        	}
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
    	logger.debug("Método getFile en formulario 2: ObjetoAsistenteDocumentosObraMayor "+this.toString());
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

	public List<ExpedienteRelacionado> getExpedientesRelacionados() {
		return expedientesRelacionados;
	}

	public void setExpedientesRelacionados(
			List<ExpedienteRelacionado> expedientesRelacionados) {
		this.expedientesRelacionados = expedientesRelacionados;
	}

	public String getTipoExpediente() {
		return tipoExpediente;
	}

	public void setTipoExpediente(String tipoExpediente) {
		this.tipoExpediente = tipoExpediente;
	}
	
}
