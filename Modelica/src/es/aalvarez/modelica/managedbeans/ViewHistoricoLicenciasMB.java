
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
import es.aalvarez.modelica.util.CreatePdfIndiceExpediente;
import es.aalvarez.modelica.util.CreatePdfResumenExpediente;
import es.aalvarez.modelica.util.MyBatisUtil;

@ManagedBean
@ViewScoped
public class ViewHistoricoLicenciasMB implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6051818210828096037L;
	final  Logger logger = LogManager.getLogger(ViewHistoricoLicenciasMB.class); 
	
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
   
    private List<Puesto> puestos;
    private List<SelectItem> spuestos;
    
    private String password;
    private Boolean invitado;
    private Boolean activarActualizar;

	public ViewHistoricoLicenciasMB() {
 
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
    	logger.debug("init ViewHistoricoLicencias MB (buscar histórico expedientes)");
    	
    	this.estadosExpediente = new ArrayList<String>();
    	for (int d=0;d<11;d++){
    		this.estadosExpediente.add(sestadosExpediente[d]);
    	}
    	extraerHistoricoExpedientesBBDD();
    
    	//los trámites se extraen e inicializan cada vez que se abre el modal en sus corresp. métodos
    	this.tramiteSelected = new TramiteExpediente();
    	this.invitado=true;
    	this.activarActualizar=false;
    	
    }

    public void extraerHistoricoExpedientesBBDD(){
		
		 
    	//Extraer Expedientes EN TRÁMITE base de datos
    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
        ExpedienteMapper expMapper = null;
        
        
        String destino=null;
//        request.setAttribute("mensaje", "Antes de entrar en faena");
        if (dbSession!=null){
            try {
              expMapper = dbSession.getMapper(ExpedienteMapper.class);
              ExpedienteExample expExample = new ExpedienteExample();
              expExample.createCriteria().andIdIsNotNull().andEstadoExpedienteEqualTo("FINALIZADO");
              this.expedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
                                   
            } catch (Exception e) {
				
				logger.error("Error en funcion (Extraer Expedientes (FINALIZADOS) BBDD  ViewLicenciasMB: "+ e.getLocalizedMessage());
                        
            }finally{
            	logger.debug("Expedientes recuperados de la base de datos, total: "+this.expedientes.size());
                dbSession.close();
            }
        }else{
        	logger.error("Extraer expedientes (FINALIZADOS) Base de Datos, sesion de base de datos nula ");
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
	  public void desarchivarExpediente(Expediente expediente){
	  		
	  		 logger.debug("Desarchivar expediente, estableciendo su estado a pendiente de archivo" +expediente.getExpediente()+"-"+expediente.getAnyo() );
			  HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
				String ip = request.getHeader("X-FORWARDED-FOR");
			  String datos = "DESARCHIVANDO EXPEDIENTE: "+expediente.getId() + expediente.getInteresado()
					  + expediente.getActuacion() + " by: "  + request.getRemoteUser() + " from: " + ip; 
				logger.debug(datos);	  
			 
				SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
				ExpedienteMapper service = null;
				int resultado = 0;
		        String destino=null;
		        Expediente target = null;
//		        request.setAttribute("mensaje", "Antes de entrar en faena");
		        if (dbSession!=null){
		            try {
		              
		              service = dbSession.getMapper(ExpedienteMapper.class);
		              ExpedienteExample eExample = new ExpedienteExample();
		              eExample.createCriteria().andIdEqualTo(expediente.getId());
		               target= (Expediente) service.selectByExample(eExample).get(0);
		              target.setEstadoExpediente("F-ARCHIVO");
		              resultado = service.updateByPrimaryKey(target);
		              dbSession.commit(true);
		             
		            }catch(Exception e){
		            	logger.error("Error actualizarEstadoLicencia desarchivar expediente",e);           
		            }finally{
		            	
		            	FacesMessage msg1 = new FacesMessage("Expediente Desarchivado", target.getEstadoExpediente() + " Expte: " +(target.getExpediente()+" - "+target.getAnyo() +
		            		 " Registro/s afectado/s: "+ resultado));
		            	FacesContext.getCurrentInstance().addMessage(null, msg1);
		            	
		            	try {
		            	     		
		            	 
		            	TramiteExpediente tramiteProvidencia = new TramiteExpediente();
			  				tramiteProvidencia.setCodModlicExpediente(target.getId());
			  				tramiteProvidencia.setFechaTramite(new Date());
			  				DateFormat df2 = DateFormat.getDateInstance(DateFormat.MEDIUM);
			  				String tFecha = df2.format(target.getProvidenciaFecha());
			  			tramiteProvidencia.setDescripcionTramite("Expediente desarchivado ");
			  			tramiteProvidencia.setTramiteAsignadoA("ADMINISTRATIVOS SECRETARÍA");
			  			tramiteProvidencia.setEstadoTramite("F-ARCHIVO");
			  			tramiteProvidencia.setTramiteActivo(true);
			  			tramiteProvidencia.setIncluirEnIndice(true);
			  			//ACTUALIZAMOS EL TRAMITE ARCHIVO Y LO ESTABLECEMOS COMO "NO ACTIVO".
			  			 TramiteExpedienteMapper service2 = dbSession.getMapper(TramiteExpedienteMapper.class);
			  			 TramiteExpedienteExample tramiteExample = new TramiteExpedienteExample();
			  			 tramiteExample.createCriteria().andCodModlicExpedienteEqualTo(target.getId()).andEstadoTramiteEqualTo("FINALIZADO");
			  			 TramiteExpediente tramiteArchivo = new TramiteExpediente();
			  			 tramiteArchivo = service2.selectByExample(tramiteExample).get(0);
			  			 tramiteArchivo.setTramiteActivo(false);
			  			 service2.updateByExample(tramiteArchivo, tramiteExample);
			  			
			  			 dbSession.commit(true);
			  			        
			  			 insertarTramiteEnLicencia(tramiteProvidencia);
			  			 
		            	}catch(Exception e){
				            	logger.error("Error actualizarEstadoLicencia desarchivar expediente",e);           
				        }finally{
				        	 FacesMessage msg2 = new FacesMessage("Actualizados trámites del expediente", "Estado Expediente F-ARCHIVO");
			            	 FacesContext.getCurrentInstance().addMessage(null, msg1);
				        }				         }
		               
			  }else{
			  	logger.error("Desarchivar Expediente, sesion de base de datos nula ");
			  }
		        dbSession.close();
		        extraerHistoricoExpedientesBBDD();
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
					
					logger.error("Error en funcion (insertarTramiteEnLicencia) ViewHistoricoLicencias: "+ e.getLocalizedMessage());
	            
	            }finally{
	            	
	            	 
	                                        
	    		}
	        }
			
	        if (dbSession!=null){dbSession.close();} //por si no se ejecutara la operación y quedara la conexión abierta
	        //return resultado = "success";
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
		
		openPDF("modals/imprimir-pdf-emodal");
		
	}
	public void openIndiceExpediente(Expediente expediente){
		
		CreatePdfIndiceExpediente informe = new CreatePdfIndiceExpediente();
		List<TramiteExpediente> listaTramites = new ArrayList<TramiteExpediente>();
		listaTramites = obtenerTramitesExpediente(expediente);
		informe.generaPDF(expediente, listaTramites);
		logger.debug("Generando Índice del expediente "+expediente.getId());
		openPDF("modals/imprimir-pdf02-emodal");
		
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
			case "OBRAMENOR":
				dialog = "modals/f-obramenor-modal";
				break;
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





		
    
}