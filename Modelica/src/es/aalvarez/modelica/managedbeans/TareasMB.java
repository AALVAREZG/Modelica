package es.aalvarez.modelica.managedbeans;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.CheckboxTreeNode;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;

import es.aalvarez.modelica.model.Expediente;
import es.aalvarez.modelica.model.ExpedienteExample;
import es.aalvarez.modelica.model.ExpedienteRelacionado;
import es.aalvarez.modelica.model.ExpedienteRelacionadoExample;
import es.aalvarez.modelica.model.Puesto;
import es.aalvarez.modelica.model.PuestoExample;
import es.aalvarez.modelica.model.TramiteActivoXExpediente;
import es.aalvarez.modelica.model.TramiteActivoXExpedienteExample;
import es.aalvarez.modelica.model.TramiteExpediente;
import es.aalvarez.modelica.model.TramiteExpedienteExample;
import es.aalvarez.modelica.service.ExpedienteMapper;
import es.aalvarez.modelica.service.ExpedienteRelacionadoMapper;
import es.aalvarez.modelica.service.PuestoMapper;
import es.aalvarez.modelica.service.TramiteActivoXExpedienteMapper;
import es.aalvarez.modelica.service.TramiteExpedienteMapper;
import es.aalvarez.modelica.util.CreatePdfIndiceExpediente;
import es.aalvarez.modelica.util.CreatePdfInformeErrores;
import es.aalvarez.modelica.util.CreatePdfResumenExpediente;
import es.aalvarez.modelica.util.MyBatisUtil;
import es.aalvarez.modelica.util.Nodo1;

@ManagedBean
@ViewScoped
public class TareasMB implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3332274232369692095L;
	final  Logger logger = LogManager.getLogger(TareasMB.class);
	

  
    private List<Puesto> puestos;
    private List<SelectItem> spuestos;
    private String puesto;
    
    
 
    
    private Map<String, ArrayList<Integer>> listaXpuesto = new HashMap<String, ArrayList<Integer>>();
    
    //objetos para la tabla expedientes
    public List<Expediente> expedientes = new ArrayList<Expediente>();
	private List<Expediente> filteredExpedientes;
	
	
	     

	@PostConstruct
	public void init() {
	        logger.debug("Init tareas..............");
	        this.puesto="Administrativos Sec.";
	        crearNodosPuesto();
	    }

			
		

	    
	    public void crearNodosPuesto() {
	    	obtenerPuestosDeTrabajo();
	    	logger.debug("Obtener Tramites del puesto ");
			SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			TramiteActivoXExpedienteMapper service = null;
			ExpedienteMapper service2 = null;
			
				         
			ArrayList<Integer> tmpPuestoListInteger= null;
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(TramiteActivoXExpedienteMapper.class);
	              service2 = dbSession.getMapper(ExpedienteMapper.class);
	              String puesto = null;
	              String codPuesto = null;
	              String departamento = null;
	              
	             
	            for (int i=0;i<this.puestos.size();i++){
	            	puesto = this.puestos.get(i).getPuesto();
	            	codPuesto = this.puestos.get(i).getCodpuesto();
	            	departamento = this.puestos.get(i).getDepartamento();
	            	//para el puesto advo secretaría crear un mensaje por cada trámite pendiente	
	            	if (puesto.equals("ADMINISTRATIVOS SECRETARÍA")){
	            		TramiteActivoXExpedienteExample tExample2 = new TramiteActivoXExpedienteExample();
			            tExample2.createCriteria().andTramiteAsignadoAEqualTo(puesto);
			            tExample2.setOrderByClause("fecha_tramite");
			            List<TramiteActivoXExpediente> tmpPuestoList = new ArrayList<TramiteActivoXExpediente>();
			            tmpPuestoList=service.selectByExample(tExample2);
			            String mensaje = "Expedientes para el puesto "+puesto+": "+tmpPuestoList.size();
			            this.puesto = "Administrativos Sec.: "+tmpPuestoList.size() +" trámites pendientes";
			            logger.debug(mensaje);
			            tmpPuestoListInteger=new ArrayList<Integer>();
			            for (int j=0;j<tmpPuestoList.size();j++){
			            	
			            	String id = tmpPuestoList.get(j).getId().toString();
			            	String mensaje0 = tmpPuestoList.get(j).getActuacion();
			            	String mensaje1 = tmpPuestoList.get(j).getDescripcionTramite();
			            	String mensaje2 = tmpPuestoList.get(j).getTipoExpediente();
			            	String mensaje3 = tmpPuestoList.get(j).getInteresado();
			            	DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			     	        String tFecha = df2.format(tmpPuestoList.get(j).getFechaTramite());
			            	String sumary ="Desde:"+ tFecha + " - "+mensaje2 + " de " + mensaje3;
			            	String detail = mensaje0 + "--->" + mensaje1;
			            	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, sumary,detail ));
			            	
			            }
	            		
	            	}else{
	            		break;
	            	}
	            		            	
	            	
	              }
	              
	             	              
	                                     
	            }catch(Exception e){
	            	logger.error("Error: ",e);
	                        
	            }finally{
	            	
	              dbSession.close();
	             
	    		}
		    }else{
		    	logger.error("Obtener tramites del puesto, sesion de base de datos nula ");
		    }
	    

	       
	        
	    }
	    
	    public void obtenerPuestosDeTrabajo(){
			
			logger.debug("Obtener puestos de trabajo....");
			SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			PuestoMapper service = null;
	        this.puestos = new ArrayList<Puesto>();
	        this.spuestos = new ArrayList<SelectItem>();
	        String destino=null;
//	      request.setAttribute("mensaje", "Antes de entrar en faena");
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
	    public void extraerExpedientesBBDD(){
			
			 
	    	//Extraer Expedientes bases de datos
	    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
	        ExpedienteMapper expMapper = null;
	        
	        
	        String destino=null;
//	        request.setAttribute("mensaje", "Antes de entrar en faena");
	        if (dbSession!=null){
	            try {
	            System.out.println("doPost de LicenciaesController, la sesion no es nula" + dbSession.toString());
	              expMapper = dbSession.getMapper(ExpedienteMapper.class);
	              ExpedienteExample expExample = new ExpedienteExample();
	              expExample.createCriteria().andIdIsNotNull();
	              this.expedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
	            
	                        
	            }catch(Exception e){
	            	logger.error("Error: ",e);
	                        
	            }finally{
	                 dbSession.close();
	            }
	        }else{
	    	System.out.println("Extraer expedientes Base de Datos, sesion de base de datos nula ");
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
				if (listaIds.size()>0){
			     try {
			          expMapper = dbSession.getMapper(ExpedienteMapper.class);
			          ExpedienteExample expExample = new ExpedienteExample();
			          expExample.createCriteria().andIdIn(listaIds);
			          listaExpedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
			     } catch (Exception e) {
			    	 logger.error("Error en funcion (crearListaExpedientesRelacionados)  DashboardMB: "+ e.getLocalizedMessage());
			     
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
		public void extraerExpedientesBBDD(String estado){
			
			 
	    	//Extraer Expedientes bases de datos
	    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
	        ExpedienteMapper expMapper = null;
	              
	        if (dbSession!=null){
	            try {
	            
	              expMapper = dbSession.getMapper(ExpedienteMapper.class);
	              ExpedienteExample expExample = new ExpedienteExample();
	              expExample.createCriteria().andEstadoExpedienteEqualTo(estado);
	              
	              this.expedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
	            
	                        
	            }catch(Exception e){
	            	logger.error("Error: ",e);
	                        
	            }finally{
	                 dbSession.close();
	            }
	        }else{
	    	System.out.println("Extraer expedientes Base de Datos, sesion de base de datos nula ");
	    	}
	    }
		public void extraerExpedientesNoFinalizados(String estado){
			
			 
	    	//Extraer Expedientes bases de datos
	    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
	        ExpedienteMapper expMapper = null;
	        
	        
	        //String destino=null;
//	        request.setAttribute("mensaje", "Antes de entrar en faena");
	        if (dbSession!=null){
	            try {
	            System.out.println("doPost de LicenciaesController, la sesion no es nula" + dbSession.toString());
	              expMapper = dbSession.getMapper(ExpedienteMapper.class);
	              ExpedienteExample expExample = new ExpedienteExample();
	              expExample.createCriteria().andEstadoExpedienteNotEqualTo(estado);
	              this.expedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
	                                    
	            }catch(Exception e){
	            	logger.error("Error: ",e);
	                        
	            }finally{
	                 dbSession.close();
	            }
	        }else{
	    	System.out.println("Extraer expedientes Base de Datos, sesion de base de datos nula ");
	    	}
	    }
		
		public void extraerExpedientesBBDD(List<Integer> listaIdExpedientes){
			
			 
	    	//Extraer Expedientes bases de datos
	    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
	        ExpedienteMapper expMapper = null;
	        
	        int size = listaIdExpedientes.size();
	        logger.debug("Extraer expedientes desde lista, con "+size+" elementos." );
	        if (size>0){
	        	if (dbSession!=null){
	        		try {
	            	  logger.debug("La lista de expedientes contiene "+size+ " elementos." );
		              expMapper = dbSession.getMapper(ExpedienteMapper.class);
		              ExpedienteExample expExample = new ExpedienteExample();
		              expExample.createCriteria().andIdIn(listaIdExpedientes);
		              this.expedientes= (List<Expediente>)(expMapper.selectByExample(expExample));
	                       
			            }catch(Exception e){
			            	logger.error("Error: ",e);
			                        
			            }finally{
			                 dbSession.close();
			            }
		        }else{
		    	System.out.println("Extraer expedientes Base de Datos, sesion de base de datos nula ");
		    	}
	        }else{
          	  logger.debug("La lista de expedientes contiene "+size+ " elementos." );
            }	
	    }
		
		public void vaciarListaExpedientesFiltrados(){
			this.filteredExpedientes=null;
		}
		
		
		
	    
	    
	    public void generaInformeEstado(){
			
			CreatePdfInformeErrores informe = new CreatePdfInformeErrores();
			String informeGenerado = null;
			informeGenerado= informe.generaPDF();
			logger.debug("Generado Informe de Estado Expedientes, en el archivo "+informeGenerado+" abriendo diálogo");
			openPDFEspecifico("modals/imprimir-pdf03-emodal",informeGenerado);
			
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
		public void openPDFEspecifico(String dialog,String file) {
	    	
	    	try {
				Map<String,Object> options = new HashMap<String, Object>();
				options.put("modal", true);
				options.put("draggable", false);
				options.put("resizable", true);
				options.put("closable", true);
				options.put("contentWidth", 1280);
				Map<String,List<String>> params = new HashMap<String,List<String>>();
			        List<String> paramList = new ArrayList<String>();
			        paramList.add(file);
			        params.put("file", paramList);
			       
			    	
			        
				RequestContext.getCurrentInstance().openDialog(dialog,options,params);
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Abriendo Informe del expediente", " ... " );
				FacesContext.getCurrentInstance().addMessage(null, message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    }
		
	     
	    public void handleClose(CloseEvent event) {
	        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Panel Closed", "Closed panel id:'" + event.getComponent().getId() + "'");
	         
	        addMessage(message);
	    }
	     
	    public void handleToggle(ToggleEvent event) {
	    	crearNodosPuesto();
	    }
	     
	    private void addMessage(FacesMessage message) {
	        FacesContext.getCurrentInstance().addMessage(null, message);
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

		

		public Map<String, ArrayList<Integer>> getListaXpuesto() {
			return listaXpuesto;
		}

		public void setListaXpuesto(Map<String, ArrayList<Integer>> listaXpuesto) {
			this.listaXpuesto = listaXpuesto;
		}

		

		public String getPuesto() {
			return puesto;
		}

		public void setPuesto(String puesto) {
			this.puesto = puesto;
		}

		

		

		



}
