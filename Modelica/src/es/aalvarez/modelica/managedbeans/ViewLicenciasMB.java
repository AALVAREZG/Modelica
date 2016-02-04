
package es.aalvarez.modelica.managedbeans;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

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
import es.aalvarez.modelica.service.ArticuloInformeJuridicoMapper;
import es.aalvarez.modelica.service.ExpedienteMapper;
import es.aalvarez.modelica.service.ExpedienteRelacionadoMapper;
import es.aalvarez.modelica.service.PuestoMapper;
import es.aalvarez.modelica.service.TramiteExpedienteMapper;
import es.aalvarez.modelica.util.CreatePdfResumenExpediente;
import es.aalvarez.modelica.util.MyBatisUtil;

@ManagedBean
@ViewScoped
public class ViewLicenciasMB implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6051818210828096037L;
	final  Logger logger = LogManager.getLogger(ViewLicenciasMB.class); 
	
	public String mojarraVersion;
	
	public String getMojarraVersion() {
		return mojarraVersion;
	}

	public void setMojarraVersion(String mojarraVersion) {
		this.mojarraVersion = mojarraVersion;
	}

	public List<Expediente> expedientes = new ArrayList<Expediente>();
	private List<Expediente> filteredExpedientes;
	private final static String[] tiposExpediente;
	private final static String[] sestadosExpediente;
	

	private List<String> estadosExpediente;
	private Expediente selected;
    private Expediente current;
	
    private int selectedItemIndex;
    
    private List<TramiteExpediente> tramitesExpediente;
    private TramiteExpediente tramiteSelected;
    
    private List<ExpedienteRelacionado> expedientesRelacionados;
    private List<Expediente> listaExpedientesRelacionados;
    private Expediente expedienteRelacioadoSelected;
    private Boolean activarActualizar2;
    
    private List<Puesto> puestos;
    private List<SelectItem> spuestos;
    
    private String password;
    private Boolean invitado;
    private Boolean activarActualizar;
    

	public ViewLicenciasMB() {
 
    }
    
    static{
    	tiposExpediente = new String[3];
		tiposExpediente[0] = "OBRAMAYOR";
		tiposExpediente[1] = "OBRAMENOR";
        tiposExpediente[2] = "OCUPACION";
         
        sestadosExpediente = new String[11];
        sestadosExpediente[0] = "I-SUBSANACION"; //PENDIENTE DE SUBSANACIÓN INICIAL
        sestadosExpediente[1] = "T-PROVIDENCIA"; //PENDIENTE DE HACER PROVIDENCIA
        sestadosExpediente[2] = "T-INFTECNICO";  //PENDIENTE DE INFORME TÉCNICO
        sestadosExpediente[3] = "T-INFORMEJURIDICO"; //PENDIENTE DE INFORME JURÍDICO
        sestadosExpediente[4] = "T-SUBSANACION"; //PENDIENTE DE SUBSANACIÓN TRAS INFORMES 
        sestadosExpediente[5] = "F-RESOLUCION";  //PENDIENTE DE RESOLUCIÓN
        sestadosExpediente[6] = "F-NOTIFICACION"; //PENDIENTE DE NOTIFICACIÓN
        sestadosExpediente[7] = "F-ARCHIVO";     //PENDIENTE DE ARCHIVO
        sestadosExpediente[8] = "FINALIZADO";    //EXPEDIENTE FINALIZADO Y ARCHIVADO
        sestadosExpediente[9] = "T-OTROS";         //P.E PENDIENTE DE INFORME SECTORIAL
        sestadosExpediente[10] = "seleccionar ..."; 
    }
    @PostConstruct
    public void init(){
    	logger.debug("init ViewLicencias MB (buscar expedientes)");
    	Package p = FacesContext.class.getPackage();
    	this.mojarraVersion = "Run on Mojarra: "+ p.getImplementationTitle() + " " + p.getImplementationVersion();
    	logger.debug(this.mojarraVersion);
    	this.estadosExpediente = new ArrayList<String>();
    	for (int d=0;d<11;d++){
    		this.estadosExpediente.add(sestadosExpediente[d]);
    	}
    	extraerExpedientesBBDD();
    		obtenerPuestosDeTrabajo();
    	//los trámites se extraen e inicializan cada vez que se abre el modal en sus corresp. métodos
    	this.tramiteSelected = new TramiteExpediente();
    	this.invitado=true;
    	this.activarActualizar=false;
    	this.activarActualizar2=false;
    	
    }

    public void extraerExpedientesBBDD(){
		
		 
    	//Extraer Expedientes EN TRÁMITE base de datos
    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
        ExpedienteMapper expMapper = null;
        
        
        String destino=null;
//        request.setAttribute("mensaje", "Antes de entrar en faena");
        if (dbSession!=null){
            try {
              expMapper = dbSession.getMapper(ExpedienteMapper.class);
              ExpedienteExample expExample = new ExpedienteExample();
              expExample.createCriteria().andIdIsNotNull().andEstadoExpedienteNotEqualTo("FINALIZADO");
              this.expedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
                                   
            } catch (Exception e) {
				
				logger.error("Error en funcion (Extraer Expedientes (en trámite) BBDD  ViewLicenciasMB: "+ e.getLocalizedMessage());
                        
            }finally{
            	logger.debug("Expedientes recuperados de la base de datos, total: "+this.expedientes.size());
                dbSession.close();
            }
        }else{
        	logger.error("Extraer expedientes (en trámite) Base de Datos, sesion de base de datos nula ");
    	}
    }
    
    public void obtenerPuestosDeTrabajo(){
		
		System.out.println("Obtener puestos de trabajo ");
		SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
		PuestoMapper service = null;
        this.puestos = new ArrayList<Puesto>();
        this.spuestos = new ArrayList<SelectItem>();
        String destino=null;
//      request.setAttribute("mensaje", "Antes de entrar en faena");
        if (dbSession!=null){
            try {
           
              service = dbSession.getMapper(PuestoMapper.class);
              PuestoExample pExample = new PuestoExample();
              pExample.createCriteria().andIdpuestoIsNotNull();
              this.puestos = (List<Puesto>) service.selectByExample(pExample);
              if (this.puestos.size()>0){
          		for (Puesto p : this.puestos){
          			spuestos.add(new SelectItem(p.getPuesto(), p.getPuesto()));
          		}
              }else{
            	  System.out.println("No se han podido extraer los puestos de trabajo de la base de datos");	
              }
            } catch (Exception e) {
				
				logger.error("Error en funcion (Obtener puestos de trabajo) ViewLicenciasMB: "+ e.getLocalizedMessage());
                        
            }finally{
            	
              dbSession.close();
             
    		}
	    }else{
	    	System.out.println("Obtener Puestos de trabajo, sesion de base de datos nula ");
	    	}
	    }
    
	  public void actualizarExpediente(){
		   	System.out.println("Actualizando Expediente... "); 
		  	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			ExpedienteMapper service = null;
			int resultado = 0;
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(ExpedienteMapper.class);
	              resultado = service.updateByPrimaryKey(selected);
	              dbSession.commit(true);
	            } catch (Exception e) {
					
					logger.error("Error en funcion (actualizarExpediente) ViewLicenciasMB: "+ e.getLocalizedMessage());
	                        
	            }finally{
	            	 System.out.println("Expediente Actualizado!");
	            	 FacesMessage msg1 = new FacesMessage("Expediente Actualizado", selected.getEstadoExpediente() + " Expte: " +(selected.getExpediente()+" - "+selected.getAnyo() +
	            			 " Registro/s afectado/s: "+ resultado));
	                 FacesContext.getCurrentInstance().addMessage(null, msg1);
	                 dbSession.close();
	    		}

	        }else{
		    	System.out.println("Actualizar Expediente, sesion de base de datos nula ");
		    }
		    
	  	 }
	  
	  public void actualizarEstadoExpediente(Expediente expediente, String nuevoEstado){
			
			SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			ExpedienteMapper service = null;
			int resultado = 0;
	        String destino=null;
//	        request.setAttribute("mensaje", "Antes de entrar en faena");
	        if (dbSession!=null){
	            try {
	              logger.debug("Actualizar Estado Expediente. Parametros:... "+ expediente.getId() + "... " +nuevoEstado);
	              service = dbSession.getMapper(ExpedienteMapper.class);
	              ExpedienteExample eExample = new ExpedienteExample();
	              eExample.createCriteria().andIdEqualTo(expediente.getId());
	              expediente = (Expediente) service.selectByExample(eExample).get(0);
	              expediente.setEstadoExpediente(nuevoEstado);
	              resultado = service.updateByPrimaryKey(expediente);
	              dbSession.commit(true);
	            } catch (Exception e) {
					
					logger.error("Error en funcion (actualizarEstadoExpediente) ViewLicenciasMB: "+ e.getLocalizedMessage());
	                        
	                        
	            }finally{
	            	 FacesMessage msg1 = new FacesMessage("Estado de la Licencia actualizada:  ", expediente.getEstadoExpediente() + " Expte: " +(expediente.getExpediente()+" - "+expediente.getAnyo() +
	            			 " Registro/s afectado/s: "+ resultado));
	                 FacesContext.getCurrentInstance().addMessage(null, msg1);
	                 dbSession.close();
	            }
		    }else{
		    	logger.error("Actualizar Licencia, sesion de base de datos nula ");
		    	}
		    }
	  
	  	public void borrarExpediente(Expediente expediente){
	  		
	  		 logger.debug("Borrar expediente, borrando también sus tŕamites!" +expediente.getExpediente()+"-"+expediente.getAnyo() );
			  HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			  String ip = request.getHeader("X-FORWARDED-FOR");
			  String datos = "BORRANDO EXPEDIENTE: "+expediente.getId() + expediente.getInteresado()
					  + expediente.getActuacion() + " by: "  + request.getRemoteUser() + " from: " + ip; 
						logger.debug(datos);	  
			 
			  	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
				ExpedienteMapper service = null;
				int resultado = 0;
				
		        if (dbSession!=null){
		            try {
		              service = dbSession.getMapper(ExpedienteMapper.class);
		              resultado = service.deleteByPrimaryKey(expediente.getId());
		              dbSession.commit(true);
		           } catch (Exception e) {
		        	   logger.error("Error en funcion (borrar Expediente) ViewLicenciasMB: "+ e.getLocalizedMessage());
		           }finally{
		        	   
		            	 logger.debug("Expediente eliminado de la baase de datos!" + expediente.getId()+ " Registro/s afectado/s: " + resultado);
		            	 FacesMessage msg1 = new FacesMessage("Expediente Eliminado: ", +expediente.getId()+" - "+expediente.getTipoExpediente()+  " Registro/s afectado/s: "+ resultado);
		                 FacesContext.getCurrentInstance().addMessage(null, msg1);
		                 dbSession.close();
		                 int size=this.expedientes.size();
			 	        	for (int i=0; i<size; i++){
			 	        		if (this.expedientes.get(i).getId()==expediente.getId()){
			 	        			this.expedientes.remove(i);
			 	        			break;
			 	        		}
			 	        	}
		             		                 
		    		}

		        }else{
			    	logger.error("Borrar Expediente, sesion de base de datos nula ");
			    }
			 
	  	}
	  	
	 
	  	
	  	public void añadirTramiteaExpediente(String tipo){
	    	
	  		String descripcion, estado, puesto;
	  		String descripcion2, puesto2;
	  		descripcion2=null;
	  		puesto = puestos.get(0).getPuesto();
	  		Boolean incluir;
	  		DateFormat df2 = DateFormat.getDateInstance(DateFormat.MEDIUM);
	        String tFecha1 = df2.format(new Date());
	        
	  	    switch (tipo) {
	  	         case "providencia":
	  	             descripcion="Providencia del Tte. Alcalde Delegado de Urbanismo disponiendo la emisión de informes técnico y jurídico.";
	  	             estado=estadosExpediente.get(2);
	  	             incluir=true;
	  	             descripcion2="Pendiente de elaboración de Informe Técnico";
	  	             break;
	  	         case "inftecnico":
	  	        	 descripcion="Informe técnico suscrito por el Arquitecto Técnico D..... .";
	  	             estado=estadosExpediente.get(3);
	  	             incluir=true;
	  	             descripcion2="Pendiente elaborar borrador de Informe Jurídico y revisión del mismo";
	  	             break;
	  	         case "infjuridico":
	  	        	descripcion="Informe jurídico suscrito por el Secretario.";
	  	             estado=estadosExpediente.get(5);
	  	             incluir=true;
	  	           descripcion2="Pendiente elaborar borrador de resolución/subsanación";
	  	             break;
	  	         case "resolucion":
	  	        	 descripcion="Resolución del Tte. Alcalde Delegado de Urbanismo por la que se ACUERDA ....";
	  	             estado=estadosExpediente.get(6);
	  	             descripcion2="Pendiente elaborar-firmar notificación";
	  	             incluir=true;
	  	             break;
	  	         case "notificacion":
	  	        	 descripcion="Notificación de la resolución anterior cursada al interesado en fecha " +tFecha1+ " (RS ....).";
	  	             estado=estadosExpediente.get(7);
	  	             incluir=true;
	  	             break;
	  	         case "subsanacion":
	  	        	 descripcion="Oficio de subsanación cursado al interesado (RS ....) requiriendo la subsanación de las siguientes incidencias: .";
	  	             estado=estadosExpediente.get(4);
	  	             incluir=true;
	  	             break;
	  	         case "otro":
	  	        	 descripcion="..editar trámite";
	  	             estado=estadosExpediente.get(9);
	  	             incluir=false;
	  	             break;
	  	         case "archivo":
	  	        	 descripcion="Expediente Archivado en Caja ...";
	  	             estado=estadosExpediente.get(8);
	  	             puesto=puestos.get(10).getPuesto();
	  	             incluir=true;
	  	             break;
	  	         	             
	           default:
	               throw new IllegalArgumentException("Invalid TIPO: " + tipo);
	  	    }
	  		try {
	    		TramiteExpediente nuevo = new TramiteExpediente();
	        	Integer codExpediente = this.selected.getId();
	        	nuevo.setCodModlicExpediente(codExpediente);
	        	nuevo.setFechaTramite(new Date());
	        	nuevo.setDescripcionTramite(descripcion);
	        	nuevo.setTramiteAsignadoA(puesto);
	        	nuevo.setTramiteActivo(false);
	        	nuevo.setEstadoTramite(estado);
	        	nuevo.setIncluirEnIndice(incluir);
	        	
	        	//Generamos id de linea de factura temporal
	        	Random rand = new Random();
	            // nextInt excludes the top value so we have to add 1 to include the top value
	        	// nextInt(n) genera numeros positivos entre 0 y n, si restamos n generará números negativos
	            int randomNum = rand.nextInt(1000)-1000;
	            System.out.println("Número aleatorio negativo" + randomNum);
	        	nuevo.setIdtramite(randomNum);
	        	this.tramitesExpediente.add(nuevo);
	        	
	        	if (descripcion2!=null){
		        	
	        		TramiteExpediente nuevo2 = new TramiteExpediente();
		        	Integer codExpediente2 = this.selected.getId();
		        	nuevo2.setCodModlicExpediente(codExpediente2);
		        	nuevo2.setFechaTramite(new Date());
		        	nuevo2.setDescripcionTramite(descripcion2);
		        	nuevo2.setTramiteAsignadoA(puesto);
		        	nuevo2.setTramiteActivo(false);
		        	nuevo2.setEstadoTramite(estado);
		        	nuevo2.setIncluirEnIndice(false);
	        	
		        	//Generamos id de linea de factura temporal
		        	Random rand2 = new Random();
		            // nextInt excludes the top value so we have to add 1 to include the top value
		        	// nextInt(n) genera numeros positivos entre 0 y n, si restamos n generará números negativos
		            int randomNum2 = rand2.nextInt(1000)-1000;
		            System.out.println("Número aleatorio negativo" + randomNum2);
		        	nuevo2.setIdtramite(randomNum2);
		        	this.tramitesExpediente.add(nuevo2);
	        	}
	        	
	        	
	    		 
			} catch (Exception e) {
				System.out.println("error: "+ e.getMessage() + e.getCause());
				e.printStackTrace();
			}
	       
	       
	        FacesMessage msg = new FacesMessage("Añadiendo nuevo trámite");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	  }
	  
	  public void seleccionarTramiteActivo(TramiteExpediente tramite){
		  logger.debug("Cambiando tramite activo ....");
		  if (tramite.getEstadoTramite()=="FINALIZADO"){
				 tramite.setTramiteAsignadoA("ARCHIVO");
			  }
		  if (tramite.getTramiteActivo()==true){
			  actualizarEstadoExpediente(this.selected,tramite.getEstadoTramite());  
			  int idTramite = tramite.getIdtramite();
			  for (int i=0;i<this.tramitesExpediente.size();i++){
				  if(this.tramitesExpediente.get(i).getIdtramite()!=idTramite){
					 this.tramitesExpediente.get(i).setTramiteActivo(false);			  
				  }
				  
			  }
		  }else{
			  int i=this.tramitesExpediente.size()-1;
			  this.tramitesExpediente.get(i).setTramiteActivo(true);			  
			  actualizarEstadoExpediente(this.selected,this.tramitesExpediente.get(i).getEstadoTramite());  	  
			  
		  }
		  
		  this.activarActualizar=true;
	  }
	  
	  public void borrarTramite(TramiteExpediente tramite){
		  System.out.println("Borrar Trámite" + tramite.getIdtramite());
		  HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String ip = request.getHeader("X-FORWARDED-FOR");
		  String datos = "BORRANDO TRÁMITE: Borrando trámite "+tramite.getCodModlicExpediente() + tramite.getFechaTramite()
				  + tramite.getDescripcionTramite() + " by: "  + request.getRemoteUser() + " from: " + ip; 
			logger.debug(datos);	  
		  if (tramite.getIdtramite()>0){
		  //si el id del trámite es positivo, el trámite se ha recuperado de la BBDD
		  	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			TramiteExpedienteMapper service = null;
			int resultado = 0;
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(TramiteExpedienteMapper.class);
	              resultado = service.deleteByPrimaryKey(tramite.getIdtramite());
	              dbSession.commit(true);
	           } catch (Exception e) {
					
					logger.error("Error en funcion (borrarTramite) ViewLicenciasMB: "+ e.getLocalizedMessage());
	                        
	                    
	                        
	            }finally{
	            	 System.out.println("Trámite eliminado de la baase de datos!" + tramite.getIdtramite());
	            	 FacesMessage msg1 = new FacesMessage("Trámite Eliminado: ", +tramite.getIdtramite()+ " Registro/s afectado/s: "+ resultado);
	                 FacesContext.getCurrentInstance().addMessage(null, msg1);
	                 dbSession.close();
	                 int size=this.tramitesExpediente.size();
		 	        	for (int i=0; i<size; i++){
		 	        		if (this.tramitesExpediente.get(i).getIdtramite()==tramite.getIdtramite()){
		 	        			this.tramitesExpediente.remove(i);
		 	        			break;
		 	        		}
		 	        	}
	                 
	    		}

	        }else{
		    	System.out.println("Borrar Trámite, sesion de base de datos nula ");
		    }
		  }else{
			  //si el id del trámite es negativo es un id temporal, y significa que se ha creado en la vista, no recuperado de la BBDD
			  System.out.println("Trámite eliminado del objeto, aún no se había guardado en la BBDD!" + tramite.getIdtramite());
	        	int size=this.tramitesExpediente.size();
	        	for (int i=0; i<size; i++){
	        		
	        		if (this.tramitesExpediente.get(i).getIdtramite()==tramite.getIdtramite()){
	        			this.tramitesExpediente.remove(i);
	        			break;
	        		}
	        	}
	        }
		  
		  	//AHORA SELECCIONAMOS EL ÚLTIMO TRÁMITE COMO ACTIVO
		  	if (this.tramitesExpediente.size()>0){
			  	int i=this.tramitesExpediente.size()-1;
			  	this.tramitesExpediente.get(i).setTramiteActivo(true);
				actualizarEstadoExpediente(this.selected,this.tramitesExpediente.get(i).getEstadoTramite());
				this.activarActualizar=true;
		  	}
	 
   
	  	 
	  }
	  
	 
	  
	  public void recuperarTramitesBBDD(){
		  
		  	System.out.println("Obtener trámites del expediente ");
			SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			TramiteExpedienteMapper service = null;
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(TramiteExpedienteMapper.class);
	              TramiteExpedienteExample pExample = new TramiteExpedienteExample();
	              pExample.createCriteria().andCodModlicExpedienteEqualTo(this.selected.getId());
	              this.tramitesExpediente = (List<TramiteExpediente>) service.selectByExample(pExample);
	              if (this.tramitesExpediente.size()>0){
	            	System.out.println("Se han encontrado tŕamites para el expediente");
	              }else{
	            	System.out.println("No se han encontrado trámites para el expediente");	
	              }
	                                     
	            }catch(Exception e){
	            	logger.error("Error: ",e);
	                        
	            }finally{
	            	
	              dbSession.close();
	             
	    		}
	    }else{
	    	System.out.println("Obtener trámites del expediente, sesion de base de datos nula ");
	    }
	    
	  }
	  
	  public void actualizarTramitesExpediente(){
		   	System.out.println("Actualizando Expediente... "); 
		  	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			TramiteExpedienteMapper service = null;
			int nuevos = 0;
			int editados = 0;
			int generatedID;
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String ip = request.getHeader("X-FORWARDED-FOR");
			if (ip == null) {
			    ip = request.getRemoteAddr();
			}
			System.out.println("ipAddress:" + ip);
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(TramiteExpedienteMapper.class);
	              TramiteExpediente te = new TramiteExpediente();
	              for (int p=0;p<this.tramitesExpediente.size();p++){
	              		te = this.tramitesExpediente.get(p);
	              		te.setFechaModificacion(new Date());
	              		te.setIpCliente(ip);
	              		
	              		if (te.getIdtramite()<0){
	              			//tramite nuevo, insertar
	              			nuevos=nuevos+service.insert(te);
		              		generatedID = te.getIdtramite();
		              		this.tramitesExpediente.get(p).setIdtramite(generatedID);
	              		}else{
	              			//editar trámite
	              			 editados = editados+ service.updateByPrimaryKey(te);
	              		}
	            }
              	
              	logger.debug("Actualizando el Expediente (pendiente de commit), "+this.selected.getExpediente()
              		+ " - "+ this.selected.getAnyo() +" con número de tramites editados  "+editados + " numero de tramites nuevos "+ nuevos
              		+ "  desde la dirección ip: " + ip);
              	dbSession.commit(true);
	            } catch (Exception e) {
					
	            	logger.error("Error en funcion (actualizarTramitesExpediente) ViewLicenciasMB: "+ e.getLocalizedMessage());
	                        
	            }finally{
	            	 System.out.println("Expediente Actualizado!");
	            	 
	            	 FacesMessage msg1 = new FacesMessage("Expediente Actualizado...: " +(selected.getExpediente()+" - "+selected.getAnyo() +
	            			 " Tramites actualizados: "+ editados + " Nuevos: " + nuevos));
	                 FacesContext.getCurrentInstance().addMessage(null, msg1);
	                 dbSession.close();
	                 extraerExpedientesBBDD();
	    		}

	        }else{
		    	System.out.println("Actualizar Expediente, sesion de base de datos nula ");
		    }
		    
	  }
	  
	  public void prepareOpenTramitesForm(Expediente expediente){
		  this.tramitesExpediente = new ArrayList<TramiteExpediente>();
		  this.activarActualizar=false;
		  this.selected=expediente;
		  recuperarTramitesBBDD();
		 
	      
	  }
	  public void prepareOpenRelacionadosForm(Expediente expediente){
		  this.expedientesRelacionados = new ArrayList<ExpedienteRelacionado>();
		  this.listaExpedientesRelacionados = new ArrayList<Expediente>();
		  this.activarActualizar2=false;
		  this.selected=expediente;
		  recuperarExpedientesRelacionadosBBDD();
		  crearListaExpedientesRelacionados();
		 
	      
	  }

	public void recuperarExpedientesRelacionadosBBDD(){
			  
		  	logger.debug("Obtener expedientes relacionados ");
			SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			ExpedienteRelacionadoMapper service = null;
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(ExpedienteRelacionadoMapper.class);
	              ExpedienteRelacionadoExample pExample = new ExpedienteRelacionadoExample();
	              pExample.createCriteria().andIdexpedienteaEqualTo(this.selected.getId());
	              pExample.or(pExample.createCriteria().andIdexpedientebEqualTo(this.selected.getId()));
	              this.expedientesRelacionados = (List<ExpedienteRelacionado>) service.selectByExample(pExample);
	              if (this.expedientesRelacionados.size()>0){
	            	logger.debug("Se han encontrado "+this.expedientesRelacionados.size()+" expedientes relacionados con el seleccionado");
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
	    
		}
		public void crearListaExpedientesRelacionados(){
			
		
			List<Integer> listaIds = new ArrayList<Integer>();
			Iterator<ExpedienteRelacionado> it1 = this.expedientesRelacionados.iterator();
			ExpedienteRelacionado expR = new ExpedienteRelacionado();
			while(it1.hasNext()){
				expR = it1.next();
				//añadimos a la lista el id expediente que no coincida con selected, que será el relacionado.
			  	if (expR.getIdexpedientea()==this.selected.getId()){
			  		listaIds.add(expR.getIdexpedienteb());
			  	}else{
			  		listaIds.add(expR.getIdexpedientea());
			  	}
				
			}
			
			SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			ExpedienteMapper expMapper = null;
			     
			String destino=null;
		    // request.setAttribute("mensaje", "Antes de entrar en faena");
			if (dbSession!=null){
			     try {
			          expMapper = dbSession.getMapper(ExpedienteMapper.class);
			          ExpedienteExample expExample = new ExpedienteExample();
			          expExample.createCriteria().andIdIn(listaIds);
			          this.listaExpedientesRelacionados= (List<Expediente>)(expMapper.selectByExample(expExample));
			     } catch (Exception e) {
			    	 logger.error("Error en funcion (crearListaExpedientesRelacionados)  ViewLicenciasMB: "+ e.getLocalizedMessage());
			     
			     }finally{
			    	 
			         logger.debug("Expedientes relacionados, recuperados de la base de datos,  total: "+this.listaExpedientesRelacionados.size());
			                dbSession.close();
			     } 
			 }else{
			   	logger.error("Error en función crearListaExpedientesRelacionados (ViewLicenciasMB), sesion de base de datos nula ");
			 }
		 }
		
		 public void borrarExpedienteRelacionado(Expediente expediente){
			  logger.debug("Borrar Expediente relacionado..." + expediente.getId() + " del expediente " + this.selected);
			  Iterator<ExpedienteRelacionado> it1 = this.expedientesRelacionados.iterator();
			  ExpedienteRelacionado expR = new ExpedienteRelacionado();
			  Integer idRelacionBorrada = null;
			  while(it1.hasNext()){
					expR = it1.next();
					//buscamos la coincidencia con idExpedientea o idExpediente b, si aparece, esa será la relación a elimimar
				  	if (expediente.getId()==expR.getIdexpedienteb() || expediente.getId()==expR.getIdexpedientea()){
				  		idRelacionBorrada=expR.getIdrelacion();
				  	}
				}
			  
			  if (expR.getIdrelacion()>0){
			  //si el id del trámite es positivo, el trámite se ha recuperado de la BBDD
			  	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
				ExpedienteRelacionadoMapper service = null;
				int resultado = 0;
		        if (dbSession!=null){
		            try {
		              service = dbSession.getMapper(ExpedienteRelacionadoMapper.class);
		              resultado = service.deleteByPrimaryKey(idRelacionBorrada);
		              dbSession.commit(true);
		           } catch (Exception e) {
						
						logger.error("Error en funcion (borrarExpedienteRelacionado) ViewLicenciasMB: "+ e.getLocalizedMessage());
		                        
		                    
		                        
		            }finally{
		            	 logger.debug("Expediente Relacionado eliminado de la baase de datos!");
		            	  dbSession.close();
		                 int size=this.expedientesRelacionados.size();
			 	        	for (int i=0; i<size; i++){
			 	        		if (this.expedientesRelacionados.get(i).getIdrelacion()==idRelacionBorrada){
			 	        			this.expedientesRelacionados.remove(i);
			 	        			break;
			 	        		}
			 	        	}
			 	         size=this.listaExpedientesRelacionados.size();
				 	        	for (int i=0; i<size; i++){
				 	        		if (this.listaExpedientesRelacionados.get(i).getId()==expediente.getId()){
				 	        			this.listaExpedientesRelacionados.remove(i);
				 	        			break;
				 	        		}
				 	        	}
		                 
		    		}

		        }else{
			    	System.out.println("eliminar Expediente Relacionado, sesion de base de datos nula ");
			  }
			  }else{
				  //si el id de la relación es negativo es un id temporal, y significa que se ha creado en la vista, no recuperado de la BBDD
				  logger.debug("Expediente Relacionado eliminado del objeto, aún no se había guardado en la BBDD!");
				  int size=this.expedientesRelacionados.size();
	 	        	for (int i=0; i<size; i++){
	 	        		if (this.expedientesRelacionados.get(i).getIdrelacion()==idRelacionBorrada){
	 	        			this.expedientesRelacionados.remove(i);
	 	        			break;
	 	        		}
	 	        	}
				  	size=this.listaExpedientesRelacionados.size();
		        	for (int i=0; i<size; i++){
		        		if (this.listaExpedientesRelacionados.get(i).getId()==expediente.getId()){
		        			this.listaExpedientesRelacionados.remove(i);
		        			break;
		        		}
		        	}
			  }
			  this.activarActualizar2=true;
		 }
		 
		 public void actualizarExpedientesRelacionadosExpediente(){
			   	logger.debug("Actualizando Expedientes Relacionados... "); 
			  	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
				ExpedienteRelacionadoMapper service = null;
				int nuevos = 0;
				int editados = 0;
				int generatedID;
				
		        if (dbSession!=null){
		            try {
		              service = dbSession.getMapper(ExpedienteRelacionadoMapper.class);
		              ExpedienteRelacionado re = new ExpedienteRelacionado();
		              for (int p=0;p<this.expedientesRelacionados.size();p++){
		              		re = this.expedientesRelacionados.get(p);
		              		
		              		if (re.getIdrelacion()<0){
		              			//expediente relacionado nuevo, insertar
		              			nuevos=nuevos+service.insert(re);
			              		generatedID = re.getIdrelacion();
			              		this.expedientesRelacionados.get(p).setIdrelacion(generatedID);
		              		}else{
		              			//editar trámite
		              			 editados = editados+ service.updateByPrimaryKey(re);
		              		}
		            }
	              	
	              	logger.debug("Actualizando el Expedientes Relacionados (pendiente de commit), "+this.selected.getExpediente()
	              		+ " - "+ this.selected.getAnyo() +" con número de expedientes relacionados editados  "+editados + " y nuevos: "+ nuevos);
	              		
	              	dbSession.commit(true);
		            } catch (Exception e) {
						
		            	logger.error("Error en funcion (actualizarExpedientesRelacionadosExpediente) ViewLicenciasMB: "+ e.getLocalizedMessage());
		                        
		            }finally{
		            	 logger.debug("Expedientes Relacionados Actualizados!");
		            	 
		            	 FacesMessage msg1 = new FacesMessage("Expediente Actualizado...: " +(selected.getExpediente()+" - "+selected.getAnyo() +
		            			 " Expedientes relacionados actualizados: "+ editados + " Nuevos: " + nuevos));
		                 FacesContext.getCurrentInstance().addMessage(null, msg1);
		                 dbSession.close();
		                 //extraerExpedientesBBDD();
		    		}

		        }else{
			    	System.out.println("Actualizar Expediente, sesion de base de datos nula ");
			    }
			    
		  }
	  
	  public void closeTramitesForm(){
		 
          RequestContext.getCurrentInstance().closeDialog("tramites-expediente");
	  }
	  public void onCloseTramitesForm(){
		  this.tramitesExpediente= null;
	  }
	  	
	  public void prepararViaje() {
		String tipo = this.selected.getTipoExpediente();
		
		System.out.println("Vamos ver un Expddiente de tipo  " + tipo);
        FacesMessage msg = new FacesMessage("Tipo de Expediente", tipo);
        FacesContext.getCurrentInstance().addMessage(null, msg);
		
       //Obtener la licencia de Obra Mayor
    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
        ExpedienteMapper expMapper = null;
        
        
        String destino=null;
        if (dbSession!=null){
            try {
              System.out.println("Preparar Viaje al expediente, la sesion no es nula" + dbSession.toString());
              expMapper = dbSession.getMapper(ExpedienteMapper.class);
              ExpedienteExample expExample = new ExpedienteExample();
              expExample.createCriteria().andIdEqualTo(this.selected.getId());
              expMapper.selectByExample(expExample);
              this.current =  (Expediente)  expMapper.selectByExample(expExample).get(0);
              System.out.println("el estado del expediente es " + this.current.getEstadoExpediente());
              FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("expediente", this.current);
                        
            }catch(Exception e){
            	logger.error("Error: ",e);
                        
            }finally{
            	
              dbSession.close();
             
    		}
        }else{
        	System.out.println("Trámites expediente, sesion de base de datos nula ");
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
				if(listaIds.size()>0){
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
		
		openPDF("modals/imprimir-pdf-emodal");
		
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
	public void onClosePDF(){
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Volver", " ... " );
        FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	public void openEditExpediente(Expediente expediente){
		
		String dialog = null;
		String tipo = expediente.getTipoExpediente();
		switch(tipo) {
			case "OBRAMAYOR": 
				dialog = "modals/f-obramayor-modal";
				break;
			case "OBRAmenor":
				dialog = "modals/f-obramenor-modal";
				break;
			case "OCUPACION":
				dialog = "modals/f-ocupacion-modal";
				break;
			case "SEGREGACION":
				dialog = "modals/f-segregacion-modal";
				break;
			case "PLANEAMIENTO":
				dialog = "modals/f-planeamiento-modal";
				break;
			case "CERTIFICADO":
				dialog = "modals/f-certificado-modal";
				break;
			default:
				dialog = "modals/f-otros-modal";
		}
		
		
		try {
			Map<String,Object> options = new HashMap<String, Object>();
			options.put("modal", true);
			options.put("draggable", false);
			options.put("resizable", true);
			options.put("closable", true);
			options.put("contentWidth", 1280);
			options.put("contentHeight", 500);
			Map<String,List<String>> params = new HashMap<String,List<String>>();
		        List<String> paramList = new ArrayList<String>();
		        paramList.add("editar");
		        params.put("modo", paramList);
		        List<String> paramList2 = new ArrayList<String>();
		        paramList2.add(String.valueOf(expediente.getId()));
		        params.put("expediente", paramList2);
			RequestContext.getCurrentInstance().openDialog(dialog,options,params);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Editando Expediente", " ... " );
			FacesContext.getCurrentInstance().addMessage(null, message);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onCloseEditExpediente(){
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Expediente Editado", " ... " );
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	public void openElegirExpedienteRelacionado() {
	        Map<String,Object> options = new HashMap<String, Object>();
	        options.put("modal", true);
			options.put("draggable", false);
			options.put("resizable", true);
			options.put("closable", true);
			options.put("contentWidth", 1280);
			options.put("contentHeight", 600);
			
			Map<String,List<String>> params = new HashMap<String,List<String>>();
	        List<String> paramList = new ArrayList<String>();
	        if (this.expedientesRelacionados.size()>0){
		        Iterator<ExpedienteRelacionado> it1 = this.expedientesRelacionados.iterator();
				ExpedienteRelacionado expR = new ExpedienteRelacionado();
				while(it1.hasNext()){
					expR = it1.next();
					//añadimos a la lista el id expediente que no coincida con selected, que será el relacionado.
				  	if (expR.getIdexpedientea()==this.selected.getId()){
				  		paramList.add(String.valueOf(expR.getIdexpedienteb()));
				  	}else{
				  		paramList.add(String.valueOf(expR.getIdexpedientea()));
				  	}
					
				}
					
			}
			//añadimos el expediente actual
				paramList.add(String.valueOf(this.selected.getId()));
			//pasamos una lista de ids como parámetro para que los excluya de los expedientes a seleccionar
			
	        params.put("ids", paramList);
	        String dialog = "modals/seleccionar-expediente";
	        RequestContext.getCurrentInstance().openDialog(dialog, options, params);
	        
	}
	
	public void onExpedienteChosen(SelectEvent event) {
        Expediente exp = (Expediente) event.getObject();
        //Generamos la en la tabla de expedientes relacionados.
        ExpedienteRelacionado expR = new ExpedienteRelacionado();
      
    	Random rand = new Random();
        // nextInt excludes the top value so we have to add 1 to include the top value
    	// nextInt(n) genera numeros positivos entre 0 y n, si restamos n generará números negativos
        int randomNum = rand.nextInt(1000)-1000;
        System.out.println("Número aleatorio negativo" + randomNum);
        expR.setIdrelacion(randomNum);
        expR.setIdexpedientea(this.selected.getId());
        expR.setIdexpedienteb(exp.getId());
        this.expedientesRelacionados.add(expR);
        
        //ahora en la tabla de expedientes.
        this.listaExpedientesRelacionados.add(exp);
        
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Expediente Seleccionado", "Id:" + exp.getId());
        FacesContext.getCurrentInstance().addMessage(null, message);
        this.activarActualizar2=true;
    }
	
	public void onCloseRelacionadosForm(){
		this.expedientesRelacionados = null;
		this.listaExpedientesRelacionados = null;
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


	public String verExpediente() {
		
		
		return this.selected.getTipoExpediente();
		
	}
	public String editarExpediente() {
		FacesContext.getCurrentInstance().getExternalContext()
        .getRequestMap().put("expediente", this.selected);
		
		return this.selected.getTipoExpediente();
		
	}
	
	public String prepareCreate() {
        setCurrent(new Expediente());
        selectedItemIndex = -1;
       
        return "Create";
    }
	
	  public Expediente getSelected() {
	        if (selected == null) {
	        	
	            selected = new Expediente();
	            selectedItemIndex = -1;
	        }
	      
	        return selected;
	    }
	    
	 
	    public void setSelected(Expediente expediente) {
	        this.selected = expediente;
	    }
	 
	    
	     
	    public void onRowSelect(SelectEvent event) {
	    	
	    	System.out.println("Seleccionado registro " + ((Expediente) event.getObject()).getExpediente().toString());
	    	System.out.println("Valor de Selected " + this.selected.getId());
	        FacesMessage msg = new FacesMessage("Estado del Expediente", ((Expediente) event.getObject()).getEstadoExpediente());
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	    }
	 
	    public void onRowUnselect(UnselectEvent event) {
	    	
	        FacesMessage msg = new FacesMessage("Ningún registro seleccionado", "...");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	    }
	    public void onCellEdit(CellEditEvent event) {
	        Object oldValue = event.getOldValue();
	        Object newValue = event.getNewValue();
	        System.out.println("modificando tramite ....");
	         
	        if(newValue != null && !newValue.equals(oldValue)) {
	            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Valor Cambiado", "Anterior: " + oldValue + ", Nuevo:" + newValue);
	            FacesContext.getCurrentInstance().addMessage(null, msg);
	        }
	    }
	    
		public List<Expediente> getExpedientes() {
			return expedientes;
		}

		public void setExpedientes(List<Expediente> expedientes) {
			this.expedientes = expedientes;
		}
		public Expediente getCurrent() {
			return current;
		}

		public void setCurrent(Expediente current) {
			this.current = current;
		}
		public List<Expediente> getFilteredExpedientes() {
			return filteredExpedientes;
		}

		public void setFilteredExpedientes(List<Expediente> filteredExpedientes) {
			this.filteredExpedientes = filteredExpedientes;
		}
		public static long getSerialversionuid() {
			return serialVersionUID;
		}



		public List<String> getTiposExpediente() {
			return Arrays.asList(tiposExpediente);
		}





		public List<TramiteExpediente> getTramitesExpediente() {
			return tramitesExpediente;
		}



		public void setTramitesExpediente(List<TramiteExpediente> tramitesExpediente) {
			this.tramitesExpediente = tramitesExpediente;
		}



		public TramiteExpediente getTramiteSelected() {
			return tramiteSelected;
		}



		public void setTramiteSelected(TramiteExpediente tramiteSelected) {
			this.tramiteSelected = tramiteSelected;
		}

		public List<Puesto> getPuestos() {
			return puestos;
		}

		public void setPuestos(List<Puesto> puestos) {
			this.puestos = puestos;
		}
		public List<SelectItem> getSpuestos() {
			return spuestos;
		}

		public void setSpuestos(List<SelectItem> spuestos) {
			this.spuestos = spuestos;
		}
		public List<String> getEstadosExpediente() {
			return estadosExpediente;
		}

		public void setEstadosExpediente(List<String> estadosExpediente) {
			this.estadosExpediente = estadosExpediente;
		}

		public int getSelectedItemIndex() {
			return selectedItemIndex;
		}

		public void setSelectedItemIndex(int selectedItemIndex) {
			this.selectedItemIndex = selectedItemIndex;
		}

		public static String[] getSestadosexpediente() {
			return sestadosExpediente;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Boolean getInvitado() {
			return invitado;
		}

		public void setInvitado(Boolean invitado) {
			this.invitado = invitado;
		}

		public Boolean getActivarActualizar() {
			return activarActualizar;
		}

		public void setActivarActualizar(Boolean activarActualizar) {
			this.activarActualizar = activarActualizar;
		}

		public List<ExpedienteRelacionado> getExpedientesRelacionados() {
			return expedientesRelacionados;
		}

		public void setExpedientesRelacionados(
				List<ExpedienteRelacionado> expedientesRelacionados) {
			this.expedientesRelacionados = expedientesRelacionados;
		}

		public List<Expediente> getListaExpedientesRelacionados() {
			return listaExpedientesRelacionados;
		}

		public void setListaExpedientesRelacionados(
				List<Expediente> listaExpedientesRelacionados) {
			this.listaExpedientesRelacionados = listaExpedientesRelacionados;
		}

		

		public Boolean getActivarActualizar2() {
			return activarActualizar2;
		}

		public void setActivarActualizar2(Boolean activarActualizar2) {
			this.activarActualizar2 = activarActualizar2;
		}

		public Expediente getExpedienteRelacioadoSelected() {
			return expedienteRelacioadoSelected;
		}

		public void setExpedienteRelacioadoSelected(
				Expediente expedienteRelacioadoSelected) {
			this.expedienteRelacioadoSelected = expedienteRelacioadoSelected;
		}


		
    
}