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
import es.aalvarez.modelica.util.CreatePdfResumenExpediente;
import es.aalvarez.modelica.util.MyBatisUtil;
import es.aalvarez.modelica.util.Nodo1;

@ManagedBean
@ViewScoped
public class DashboardMB implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3332274232369692095L;
	final  Logger logger = LogManager.getLogger(DashboardMB.class);
	
	public static final String TECNICO1 = "M. ÁNGELES";
	public static final String TECNICO2 = "CÉSAR";
	public static final String TECNICO3 = "JORGE";
	public static final String TECNICO4 = "JOSÉ MANUEL";

    private DashboardModel model;
    private TreeNode root1;
    private TreeNode root2;
  
    private List<Puesto> puestos;
    private List<SelectItem> spuestos;
    
    
    private TreeNode selectedNode;
    private TreeNode selectedNode2;
    private TreeNode[] selectedNodes1;
    private TreeNode[] selectedNodes2;
    
    
    private List<TramiteActivoXExpediente> iSubsanacionList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> tSubsanacionList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> tProvidenciaList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> tInfTecnicoList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> tInfJuridicoList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> tResolucionList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> tNotificacionList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> tArchivoList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> finalizadosList = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> expedientesConTramiteActivo = new ArrayList<TramiteActivoXExpediente>();
    private List<TramiteActivoXExpediente> tOtrosList = new ArrayList<TramiteActivoXExpediente>();
    private List<Expediente> totalExpedientesList = new ArrayList<Expediente>();
    private ArrayList<Integer> listaEnTramite = new ArrayList<Integer>();
    private ArrayList<Integer> listaEnSubsanacion = new ArrayList<Integer>();
    private ArrayList<Integer> listaTecnico1 = new ArrayList<Integer>();
    private ArrayList<Integer> listaTecnico2 = new ArrayList<Integer>();
    private ArrayList<Integer> listaTecnico3 = new ArrayList<Integer>();
    private ArrayList<Integer> listaTecnico4 = new ArrayList<Integer>();
    
    private Map<String, ArrayList<Integer>> listaXpuesto = new HashMap<String, ArrayList<Integer>>();
    
    //objetos para la tabla expedientes
    public List<Expediente> expedientes = new ArrayList<Expediente>();
	private List<Expediente> filteredExpedientes;
	private final static String[] tiposExpediente;
	private final static String[] sestadosExpediente;
	
	     

	@PostConstruct
	public void init() {
	        logger.debug("Init dashboardMB..............");
	    	model = new DefaultDashboardModel();
	        DashboardColumn column1 = new DefaultDashboardColumn();
	        DashboardColumn column2 = new DefaultDashboardColumn();
	        
	         
	        column1.addWidget("estados2");
	        column2.addWidget("estados");
	         
	        column1.addWidget("pListaExpedientes");
	               
	 
	        model.addColumn(column1);
	        model.addColumn(column2);
	        this.root1 = crearNodos();
	        this.root2 = crearNodosPuestos();
	       
	        //extraerExpedientesBBDD();
	        
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
		
		public void actualizarNodos(){
			this.root1 = crearNodos();
			
		}
	
	    public TreeNode crearNodos() {
	    	logger.debug("Obtener Tramites para configurar los nodos del árbol ");
			SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			TramiteActivoXExpedienteMapper service = null;
			ExpedienteMapper service2 = null;
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(TramiteActivoXExpedienteMapper.class);
	              service2 = dbSession.getMapper(ExpedienteMapper.class);
	              //Lista de expedientes con trámites activos
	              TramiteActivoXExpedienteExample tExample = new TramiteActivoXExpedienteExample();
	              tExample.createCriteria().andIdIsNotNull();
	              this.expedientesConTramiteActivo=service.selectByExample(tExample);
	              //Lista de expedientes en estado I-SUBSANACIÓN
	              TramiteActivoXExpedienteExample tExample2 = new TramiteActivoXExpedienteExample();
	              tExample2.createCriteria().andEstadoTramiteEqualTo("I-SUBSANACION");
	              this.iSubsanacionList=service.selectByExample(tExample2);
	              //Lista de expedientes en estado T-SUBSANACIÓN
	              TramiteActivoXExpedienteExample tExample3 = new TramiteActivoXExpedienteExample();
	              tExample3.createCriteria().andEstadoTramiteEqualTo("T-SUBSANACION");
	              this.tSubsanacionList=service.selectByExample(tExample3);
	              //Lista de expedientes en estado T-PROVIDENCIA
	              TramiteActivoXExpedienteExample tExample4 = new TramiteActivoXExpedienteExample();
	              tExample4.createCriteria().andEstadoTramiteEqualTo("T-PROVIDENCIA");
	              this.tProvidenciaList=service.selectByExample(tExample4);
	              //Lista de expedientes en estado T-INFTECNICO
	              TramiteActivoXExpedienteExample tExample5 = new TramiteActivoXExpedienteExample();
	              tExample5.createCriteria().andEstadoTramiteEqualTo("T-INFTECNICO");
	              this.tInfTecnicoList=service.selectByExample(tExample5);
	              //Lista de expedientes en estado T-INFORMEJURIDICO
	              TramiteActivoXExpedienteExample tExample6 = new TramiteActivoXExpedienteExample();
	              tExample6.createCriteria().andEstadoTramiteEqualTo("T-INFORMEJURIDICO");
	              this.tInfJuridicoList=service.selectByExample(tExample6);
	              //Lista de expedientes en estado T-RESOLUCIÓN
	              TramiteActivoXExpedienteExample tExample7 = new TramiteActivoXExpedienteExample();
	              tExample7.createCriteria().andEstadoTramiteEqualTo("F-RESOLUCION");
	              this.tResolucionList=service.selectByExample(tExample7);
	              //Lista de expedientes en estado T-NOTIFICACIÓN
	              TramiteActivoXExpedienteExample tExample8 = new TramiteActivoXExpedienteExample();
	              tExample8.createCriteria().andEstadoTramiteEqualTo("F-NOTIFICACION");
	              this.tNotificacionList=service.selectByExample(tExample8);
	              //Lista de expedientes en estado T-ARCHIVO
	              TramiteActivoXExpedienteExample tExample9 = new TramiteActivoXExpedienteExample();
	              tExample9.createCriteria().andEstadoTramiteEqualTo("F-ARCHIVO");
	              this.tArchivoList=service.selectByExample(tExample9);
	              //Lista de expedientes en estado T-FINALIZADO
	              TramiteActivoXExpedienteExample tExample10 = new TramiteActivoXExpedienteExample();
	              tExample10.createCriteria().andEstadoTramiteEqualTo("FINALIZADO");
	              this.finalizadosList=service.selectByExample(tExample10);
	              TramiteActivoXExpedienteExample tExample11 = new TramiteActivoXExpedienteExample();
	              tExample11.createCriteria().andEstadoTramiteEqualTo("T-OTROS");
	              this.tOtrosList=service.selectByExample(tExample11);
	              
	              //Lista de todos los expedientes (tabla expedientes, para comparar con el resultado de tabla trámites)
	              ExpedienteExample eExample = new ExpedienteExample();
	              eExample.createCriteria().andIdIsNotNull();
	              this.totalExpedientesList=service2.selectByExample(eExample);
	                                     
	            }catch(Exception e){
	            	logger.error("Error: ",e);
	                        
	            }finally{
	            	
	              dbSession.close();
	             
	    		}
		    }else{
		    	logger.error("Obtener trámites del expediente, sesion de base de datos nula ");
		    }
	        //Total Absoluto de Expedientes
	        Integer totalAbsoluto = this.totalExpedientesList.size();
	        //Total Expedientes con trámites activos
	        Integer totalExpedientes = this.expedientesConTramiteActivo.size();
	        //Total Expedientes en Trámite
	        Integer totalExpedientesEnTramite =  this.iSubsanacionList.size()+ 
	        		+ this.tSubsanacionList.size() + this.tProvidenciaList.size() + 
	        		+ this.tInfTecnicoList.size() + this.tInfJuridicoList.size() + 
	        		+ this.tResolucionList.size() + this.tNotificacionList.size() + 
	        		+ this.tArchivoList.size() + this.tOtrosList.size();
	        Integer totalExpedientesFinalizados = this.finalizadosList.size();
	        Integer totalExpedientesPtesSubsanacion = this.iSubsanacionList.size()+ 
	        		+ this.tSubsanacionList.size(); 
	        Integer totalExpedientesProvidencia = this.tProvidenciaList.size();
	        Integer totalExpedientesInfTecnico = this.tInfTecnicoList.size();
	        Integer totalExpedientesInfJuridico = this.tInfJuridicoList.size();
	        Integer totalExpedientesResolucion = this.tResolucionList.size();
	        Integer totalExpedientesNotificacion = this.tNotificacionList.size();
	        Integer totalExpedientesArchivo = this.tArchivoList.size();
	        
	        Integer totalExpedientesSubsanacionInicio = this.iSubsanacionList.size();
	        Integer totalExpedientesSubsanacionTramite = this.tSubsanacionList.size();
	        
	        Integer totalExpedientesOtros = this.tOtrosList.size();
	        
	        Integer totalExpedientesTecnico1=0;
	        Integer totalExpedientesTecnico2=0;
	        Integer totalExpedientesTecnico3=0;
	        Integer totalExpedientesTecnico4=0;
	        
	        for(int i=0; i<totalExpedientesSubsanacionInicio;i++){
	        	this.listaEnSubsanacion.add(this.iSubsanacionList.get(i).getCodModlicExpediente());
	        }
	        for(int i=0; i<totalExpedientesSubsanacionTramite;i++){
	        	this.listaEnSubsanacion.add(this.tSubsanacionList.get(i).getCodModlicExpediente());
	        }
	        
	        
	        
	        for(int i=0; i<totalExpedientesInfTecnico;i++){
	        	String tecnico = this.tInfTecnicoList.get(i).getTramiteAsignadoA();
	        	logger.debug(tecnico);
	            	if (tecnico.contains(TECNICO1)){
	            		this.listaTecnico1.add(this.tInfTecnicoList.get(i).getCodModlicExpediente());
	            		totalExpedientesTecnico1++;
	            	}else if (tecnico.contains(TECNICO2)){
	            		this.listaTecnico2.add(this.tInfTecnicoList.get(i).getCodModlicExpediente());
	            		totalExpedientesTecnico2++;
	            	}else if (tecnico.contains(TECNICO3)){
	            		this.listaTecnico3.add(this.tInfTecnicoList.get(i).getCodModlicExpediente());
	            		totalExpedientesTecnico3++;
	            	}else if (tecnico.contains(TECNICO4)){
	            		this.listaTecnico4.add(this.tInfTecnicoList.get(i).getCodModlicExpediente());
	            		totalExpedientesTecnico4++;
	            	}
	        }
	       /* sestadosExpediente[0] = "I-SUBSANACION"; //PENDIENTE DE SUBSANACIÓN INICIAL
	        sestadosExpediente[1] = "T-PROVIDENCIA"; //PENDIENTE DE HACER PROVIDENCIA
	        sestadosExpediente[2] = "T-INFTECNICO";  //PENDIENTE DE INFORME TÉCNICO
	        sestadosExpediente[3] = "T-INFORMEJURIDICO"; //PENDIENTE DE INFORME JURÍDICO
	        sestadosExpediente[4] = "T-SUBSANACION"; //PENDIENTE DE SUBSANACIÓN TRAS INFORMES 
	        sestadosExpediente[5] = "F-RESOLUCION";  //PENDIENTE DE RESOLUCIÓN
	        sestadosExpediente[6] = "F-NOTIFICACION"; //PENDIENTE DE NOTIFICACIÓN
	        sestadosExpediente[7] = "F-ARCHIVO";     //PENDIENTE DE ARCHIVO
	        sestadosExpediente[8] = "FINALIZADO";    //EXPEDIENTE FINALIZADO Y ARCHIVADO
	        */	        
	        TreeNode root = new CheckboxTreeNode(new Nodo1("Expedientes",0,"",""), null);
	        
	        TreeNode enTramite = new CheckboxTreeNode(new Nodo1("En Tramite...",totalExpedientesEnTramite,"NOFINALIZADOS","FINALIZADO"), root);
	        enTramite.setExpanded(true);
	        TreeNode finalizados = new CheckboxTreeNode(new Nodo1("Finalizados...",totalExpedientesFinalizados,"estado","FINALIZADO"), root);
	        
	        TreeNode total = new CheckboxTreeNode(new Nodo1("Total...("+String.valueOf(totalExpedientes)+")..",totalAbsoluto,"ALL","ALL"), root);
	         
	        //Nodos En tramite
	        TreeNode subsanacion = new CheckboxTreeNode(new Nodo1("Pte. Subsanación...",totalExpedientesPtesSubsanacion,"subsanacion","SUBSANACION"), enTramite);
	        TreeNode providencia = new CheckboxTreeNode(new Nodo1("Pte. Providencia...",totalExpedientesProvidencia,"subestado","T-PROVIDENCIA"), enTramite);
	        TreeNode inftecnico = new CheckboxTreeNode(new Nodo1("En Informe Técnico...",totalExpedientesInfTecnico,"subestado","T-INFTECNICO"), enTramite);
	        TreeNode infjuridico = new CheckboxTreeNode(new Nodo1("En Informe Jurídico...",totalExpedientesInfJuridico,"subestado","T-INFORMEJURIDICO"), enTramite);
	        TreeNode resolucion = new CheckboxTreeNode(new Nodo1("Pte. Resolución...",totalExpedientesResolucion,"subestado","F-RESOLUCION"), enTramite);
	        TreeNode notificacion = new CheckboxTreeNode(new Nodo1("Pte. Notificación...",totalExpedientesNotificacion,"subestado","F-NOTIFICACION"), enTramite);
	        TreeNode archivo = new CheckboxTreeNode(new Nodo1("Pte. Archivo...",totalExpedientesArchivo,"subestado","F-ARCHIVO"), enTramite);
	        TreeNode otros = new CheckboxTreeNode(new Nodo1("Otros...",totalExpedientesOtros,"subestado","T-OTROS"),enTramite);	
	        //Nodos En Subsanacion
	        TreeNode subsanacionInicial = new CheckboxTreeNode(new Nodo1("Subsanación Inicial...",totalExpedientesSubsanacionInicio,"subestado","I-SUBSANACION"), subsanacion);
	        TreeNode subsanacionTramitacion = new CheckboxTreeNode(new Nodo1("Subsanación En Trámite...",totalExpedientesSubsanacionTramite,"subestado","T-SUBSANACION"), subsanacion);
	        
	        //Nodos En Informe Tecnico
	        TreeNode inftecnico1 = new CheckboxTreeNode(new Nodo1(TECNICO1+"...",totalExpedientesTecnico1,"puesto",TECNICO1), inftecnico);
	        TreeNode inftecnico2 = new CheckboxTreeNode(new Nodo1(TECNICO2+"...",totalExpedientesTecnico2,"puesto",TECNICO2),inftecnico);
	        TreeNode inftecnico3 = new CheckboxTreeNode(new Nodo1(TECNICO3+"...",totalExpedientesTecnico3,"puesto",TECNICO3), inftecnico);
	        TreeNode inftecnico4 = new CheckboxTreeNode(new Nodo1(TECNICO4+"...",totalExpedientesTecnico4,"puesto",TECNICO4), inftecnico);
	       
	        return root;
	    }
	    
	    
	    public TreeNode crearNodosPuestos() {
	    	obtenerPuestosDeTrabajo();
	    	logger.debug("Obtener Tramites para configurar los nodos del árbol de puestos ");
			SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			TramiteActivoXExpedienteMapper service = null;
			ExpedienteMapper service2 = null;
			
			TreeNode root = new CheckboxTreeNode(new Nodo1("Expedientes",0,"",""), null);
	         
			ArrayList<Integer> tmpPuestoListInteger= null;
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(TramiteActivoXExpedienteMapper.class);
	              service2 = dbSession.getMapper(ExpedienteMapper.class);
	              String puesto = null;
	              String codPuesto = null;
	              String departamento = null;
	              
	              Map<String, TreeNode> nodosDptos = new HashMap<String, TreeNode>();
	              TreeNode tmpDptoNode = null;
	              //por cada uno de los puestos, obtener los expedientes asignados en ese momento
	              for (int i=0;i<this.puestos.size();i++){
	            	puesto = this.puestos.get(i).getPuesto();
	            	codPuesto = this.puestos.get(i).getCodpuesto();
	            	departamento = this.puestos.get(i).getDepartamento();
	            	
	            	if (nodosDptos.containsKey(departamento)){
	            		tmpDptoNode = nodosDptos.get(departamento);
	            	}else{
	            		tmpDptoNode = new CheckboxTreeNode(new Nodo1(departamento,0,"departamento",departamento),root);
			            tmpDptoNode.setExpanded(true);
			            nodosDptos.put(departamento, tmpDptoNode);
	            	}
	            		            	
	            	TramiteActivoXExpedienteExample tExample2 = new TramiteActivoXExpedienteExample();
		            tExample2.createCriteria().andTramiteAsignadoAEqualTo(puesto);
		            List<TramiteActivoXExpediente> tmpPuestoList = new ArrayList<TramiteActivoXExpediente>();
		            tmpPuestoList=service.selectByExample(tExample2);
		            TreeNode tmpNode = new CheckboxTreeNode(new Nodo1(puesto,tmpPuestoList.size(),"puesto",codPuesto), tmpDptoNode);
		            //ahora, desde la lista de tramites, extraemos los id haciendo un recorrido de la misma.
		            //este proceso es costoso, puesto que tenemos dos if anidados, se puede optimizar creando un select a la BBDD
		            //que recupere solo los id de cada registro.
		            String mensaje = "Expedientes para el puesto "+puesto+": "+tmpPuestoList.size();
		            logger.debug(mensaje);
		            tmpPuestoListInteger=new ArrayList<Integer>();
		            for (int j=0;j<tmpPuestoList.size();j++){
		            	tmpPuestoListInteger.add(tmpPuestoList.get(j).getId());
		            	//logger.debug(tmpPuestoList.get(j).getId());
		            }
		            this.listaXpuesto.put(codPuesto, tmpPuestoListInteger);
	              }
	              
	             	              
	                                     
	            }catch(Exception e){
	            	logger.error("Error: ",e);
	                        
	            }finally{
	            	
	              dbSession.close();
	             
	    		}
		    }else{
		    	logger.error("Obtener trámites del expediente, sesion de base de datos nula ");
		    }
	        //Total Absoluto de Expedientes
	        Integer totalAbsoluto = this.totalExpedientesList.size();
	        //Total Expedientes con trámites activos
	        Integer totalExpedientes = this.expedientesConTramiteActivo.size();
	        //Total Expedientes en Trámite

	       
	        return root;
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
		
		public void displaySelectedSingle(NodeSelectEvent event) {
			this.filteredExpedientes=null;
	    	this.selectedNode=event.getTreeNode();
	        if(selectedNode != null) {
	        	Nodo1 selected = (Nodo1) selectedNode.getData();
	        	logger.debug("Nodo seleccionado "+selected.getName() + "  tipo: "+selected.getType());
	        	if ((selected.getType().contains("estado")) || (selected.getType().contains("subestado"))){
	        		logger.debug("Seleccionar por Estado: " + selected.getName() + ". Select String:"+selected.getSelectString());
	        		extraerExpedientesBBDD(selected.getSelectString());
	        	}else if (selected.getType().equalsIgnoreCase("puesto")){
	        		logger.debug("Seleccionar por puesto: " + selected.getName() + ". Select String:"+selected.getSelectString());
	        		switch (selected.getSelectString()) {
		  	         		case TECNICO1:
								if (listaTecnico1.size()>0) {
									extraerExpedientesBBDD(listaTecnico1);
								}else{
									this.expedientes= new  ArrayList<Expediente>();
								}
								break;
		  	         		case TECNICO2:
								if (listaTecnico2.size()>0) {
									extraerExpedientesBBDD(listaTecnico2);
								}else{
									this.expedientes= new  ArrayList<Expediente>();
								}
								break;
		  	         		case TECNICO3:
								if (listaTecnico3.size()>0) {
									extraerExpedientesBBDD(listaTecnico3);
								}else{
									this.expedientes= new  ArrayList<Expediente>();
								}
								break;
		  	         		case TECNICO4:
								if (listaTecnico4.size()>0) {
									extraerExpedientesBBDD(listaTecnico4);
								}else{
									this.expedientes= new  ArrayList<Expediente>();
								}
								break;
		  	         		default:
		  		               throw new IllegalArgumentException("Invalid TECNICO: " + selected.getSelectString());
	        		}  			
	        	}else if (selected.getType().equalsIgnoreCase("subsanacion")){
	        		logger.debug("Seleccionar por varios estados: " + selected.getName());
	        		extraerExpedientesBBDD(listaEnSubsanacion);
	        	}else if (selected.getType().equalsIgnoreCase("ALL")){
	        		logger.debug("Seleccionar TODOS: " + selected.getName());
	        		extraerExpedientesBBDD();
	        	}else if (selected.getType().equalsIgnoreCase("NOFINALIZADOS")){
	        		logger.debug("Seleccionar NO FINALIZADOS: " + selected.getName());
	        		extraerExpedientesNoFinalizados(selected.getSelectString());
	        	}
	        	
	            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, selected.getName(), selected.getSelectString());
	            FacesContext.getCurrentInstance().addMessage(null, message);
	        }
	        //actualizarNodos();
	    }
	    
		public void displaySelectedSinglePuestos(NodeSelectEvent event) {
			this.filteredExpedientes=null;
	    	this.selectedNode2=event.getTreeNode();
	        if(selectedNode2 != null) {
	        	Nodo1 selected = (Nodo1) selectedNode2.getData();
	        	String codPuesto = selected.getSelectString();
	        	logger.debug("Nodo seleccionado "+selected.getName() + "  cod: "+codPuesto);
	        	ArrayList<Integer> listaIds = new ArrayList<Integer>();
	        	listaIds = this.listaXpuesto.get(codPuesto);
	        	logger.debug("Tamaño de lista obtenida de HashMap" + listaIds.size());
	        	if (listaIds.size()>0) {
					extraerExpedientesBBDD(listaIds);
				}else{
					this.expedientes= new  ArrayList<Expediente>();
				}
	            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, selected.getName(), selected.getSelectString());
	            FacesContext.getCurrentInstance().addMessage(null, message);
	        }
	        //actualizarNodos();
	    }
		
	    public void displaySelectedMultiple(TreeNode[] nodes) {
	        if(nodes != null && nodes.length > 0) {
	            StringBuilder builder = new StringBuilder();
	 
	            for(TreeNode node : nodes) {
	                builder.append(node.getData().toString());
	                builder.append("<br />");
	            }
	 
	            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", builder.toString());
	            FacesContext.getCurrentInstance().addMessage(null, message);
	        }
	    }
	    
	    public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {
	    	logger.debug("Procesando pdf ... ");
	        Document pdf = (Document) document;
	        pdf.setPageSize(PageSize.A4.rotate());
	        pdf.open();
	        pdf.newPage();
	        pdf.setMargins(1, 1, 1, 1);
	        pdf.addCreationDate();
	        DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	        String tFecha = df2.format(new Date());
	        pdf.add(new Phrase("Ayuntamiento de Casariche - "));
	        Nodo1 selected = null;
	        if (this.selectedNode2==null){
	        	selected = (Nodo1) this.selectedNode.getData();
	        }else{
	        	selected = (Nodo1) this.selectedNode2.getData();
	        }
	        pdf.add(new Phrase("Listado de expedienes: -" +selected.getSelectString() +"- Generado el "+ tFecha));
	 
	        
	    }
	    
		public void handleReorder(DashboardReorderEvent event) {
	        FacesMessage message = new FacesMessage();
	        message.setSeverity(FacesMessage.SEVERITY_INFO);
	        message.setSummary("Reordered: " + event.getWidgetId());
	        message.setDetail("Item index: " + event.getItemIndex() + ", Column index: " + event.getColumnIndex() + ", Sender index: " + event.getSenderColumnIndex());
	         
	        addMessage(message);
	    }
	     
	    public void handleClose(CloseEvent event) {
	        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Panel Closed", "Closed panel id:'" + event.getComponent().getId() + "'");
	         
	        addMessage(message);
	    }
	     
	    public void handleToggle(ToggleEvent event) {
	        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, event.getComponent().getId() + " toggled", "Status:" + event.getVisibility().name());
	         
	        addMessage(message);
	    }
	     
	    private void addMessage(FacesMessage message) {
	        FacesContext.getCurrentInstance().addMessage(null, message);
	    }
	     
	    public DashboardModel getModel() {
	        return model;
	    }
	    public TreeNode getRoot1() {
	        return root1;
	    }
	 
	   
	 
	    public TreeNode getSelectedNode() {
	        return selectedNode;
	    }
	 
	    public void setSelectedNode(TreeNode selectedNode) {
	        this.selectedNode = selectedNode;
	    }
	 
	    public TreeNode[] getSelectedNodes1() {
	        return selectedNodes1;
	    }
	 
	    public void setSelectedNodes1(TreeNode[] selectedNodes1) {
	        this.selectedNodes1 = selectedNodes1;
	    }
	 
	    public TreeNode[] getSelectedNodes2() {
	        return selectedNodes2;
	    }
	 
	    public void setSelectedNodes2(TreeNode[] selectedNodes2) {
	        this.selectedNodes2 = selectedNodes2;
	    }
	 
	    
	 
	    
		public List<TramiteActivoXExpediente> gettSubsanacionList() {
			return tSubsanacionList;
		}

		public void settSubsanacionList(List<TramiteActivoXExpediente> tSubsanacionList) {
			this.tSubsanacionList = tSubsanacionList;
		}

		public List<TramiteActivoXExpediente> gettProvidenciaList() {
			return tProvidenciaList;
		}

		public void settProvidenciaList(List<TramiteActivoXExpediente> tProvidenciaList) {
			this.tProvidenciaList = tProvidenciaList;
		}

		public List<TramiteActivoXExpediente> gettInfTecnicoList() {
			return tInfTecnicoList;
		}

		public void settInfTecnicoList(List<TramiteActivoXExpediente> tInfTecnicoList) {
			this.tInfTecnicoList = tInfTecnicoList;
		}

		public List<TramiteActivoXExpediente> gettInfJuridicoList() {
			return tInfJuridicoList;
		}

		public void settInfJuridicoList(List<TramiteActivoXExpediente> tInfJuridicoList) {
			this.tInfJuridicoList = tInfJuridicoList;
		}

		public List<TramiteActivoXExpediente> gettResolucionList() {
			return tResolucionList;
		}

		public void settResolucionList(List<TramiteActivoXExpediente> tResolucionList) {
			this.tResolucionList = tResolucionList;
		}

		public List<TramiteActivoXExpediente> gettNotificacionList() {
			return tNotificacionList;
		}

		public void settNotificacionList(
				List<TramiteActivoXExpediente> tNotificacionList) {
			this.tNotificacionList = tNotificacionList;
		}

		public List<TramiteActivoXExpediente> gettArchivoList() {
			return tArchivoList;
		}

		public void settArchivoList(List<TramiteActivoXExpediente> tArchivoList) {
			this.tArchivoList = tArchivoList;
		}

		public List<TramiteActivoXExpediente> getFinalizadosList() {
			return finalizadosList;
		}

		public void setFinalizadosList(List<TramiteActivoXExpediente> finalizadosList) {
			this.finalizadosList = finalizadosList;
		}

		public List<TramiteActivoXExpediente> getExpedientesConTramiteActivo() {
			return expedientesConTramiteActivo;
		}

		public void setExpedientesConTramiteActivo(
				List<TramiteActivoXExpediente> expedientesConTramiteActivo) {
			this.expedientesConTramiteActivo = expedientesConTramiteActivo;
		}

		public List<Expediente> getTotalExpedientesList() {
			return totalExpedientesList;
		}

		public void setTotalExpedientesList(List<Expediente> totalExpedientesList) {
			this.totalExpedientesList = totalExpedientesList;
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

		public List<String> getTiposExpediente() {
			return Arrays.asList(tiposExpediente);
		}

		public ArrayList<Integer> getListaTecnico1() {
			return listaTecnico1;
		}

		public void setListaTecnico1(ArrayList<Integer> listaTecnico1) {
			this.listaTecnico1 = listaTecnico1;
		}

		public ArrayList<Integer> getListaTecnico2() {
			return listaTecnico2;
		}

		public void setListaTecnico2(ArrayList<Integer> listaTecnico2) {
			this.listaTecnico2 = listaTecnico2;
		}

		public ArrayList<Integer> getListaTecnico3() {
			return listaTecnico3;
		}

		public void setListaTecnico3(ArrayList<Integer> listaTecnico3) {
			this.listaTecnico3 = listaTecnico3;
		}

		public ArrayList<Integer> getListaTecnico4() {
			return listaTecnico4;
		}

		public void setListaTecnico4(ArrayList<Integer> listaTecnico4) {
			this.listaTecnico4 = listaTecnico4;
		}

		public ArrayList<Integer> getListaEnTramite() {
			return listaEnTramite;
		}

		public void setListaEnTramite(ArrayList<Integer> listaEnTramite) {
			this.listaEnTramite = listaEnTramite;
		}

		public ArrayList<Integer> getListaEnSubsanacion() {
			return listaEnSubsanacion;
		}

		public void setListaEnSubsanacion(ArrayList<Integer> listaEnSubsanacion) {
			this.listaEnSubsanacion = listaEnSubsanacion;
		}
		public static String[] getSestadosexpediente() {
			return sestadosExpediente;
		}

		public TreeNode getRoot2() {
			return root2;
		}

		public void setRoot2(TreeNode root2) {
			this.root2 = root2;
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

		public TreeNode getSelectedNode2() {
			return selectedNode2;
		}

		public void setSelectedNode2(TreeNode selectedNode2) {
			this.selectedNode2 = selectedNode2;
		}

		public Map<String, ArrayList<Integer>> getListaXpuesto() {
			return listaXpuesto;
		}

		public void setListaXpuesto(Map<String, ArrayList<Integer>> listaXpuesto) {
			this.listaXpuesto = listaXpuesto;
		}

		public List<TramiteActivoXExpediente> gettOtrosList() {
			return tOtrosList;
		}

		public void settOtrosList(List<TramiteActivoXExpediente> tOtrosList) {
			this.tOtrosList = tOtrosList;
		}

		

		

		



}
