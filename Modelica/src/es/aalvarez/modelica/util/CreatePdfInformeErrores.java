package es.aalvarez.modelica.util;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import es.aalvarez.modelica.managedbeans.AsistenteExpedienteObraMayor;
import es.aalvarez.modelica.model.Expediente;
import es.aalvarez.modelica.model.ExpedienteExample;
import es.aalvarez.modelica.model.TramiteActivoXExpediente;
import es.aalvarez.modelica.model.TramiteActivoXExpedienteExample;
import es.aalvarez.modelica.model.TramiteExpediente;
import es.aalvarez.modelica.model.TramiteExpedienteExample;
import es.aalvarez.modelica.service.ExpedienteMapper;
import es.aalvarez.modelica.service.TramiteActivoXExpedienteMapper;
import es.aalvarez.modelica.service.TramiteExpedienteMapper;



public class CreatePdfInformeErrores {
	
	final  Logger logger = LogManager.getLogger(CreatePdfInformeErrores.class);
	
	private List<Expediente> totalExpedientesList = new ArrayList<Expediente>();
	private List<TramiteActivoXExpediente> expedientesConTramiteActivo = new ArrayList<TramiteActivoXExpediente>();
	private List<Expediente> expedientesSinTramiteActivo = new ArrayList<Expediente>();
	
	 public String generaPDF(){
	    	
	    	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
	                .getExternalContext().getContext();
	    
	    	String realContextPath = ctx.getRealPath("/");
	    	SimpleDateFormat format =new SimpleDateFormat("MMddy-HHmmss");
	        String tFecha = format.format(new Date());
	        String relativefilepath ="/docs/REPORTS"+  "/statusReport_"+tFecha +".pdf";
	        String outputfilepath = realContextPath+relativefilepath;
			System.out.println(realContextPath);
	    	
			/** Path to the resulting PDF file. */
			
			logger.debug("--------------------------------------------------------------------------");
	        logger.debug("GENERANDO INFORME DE CONSISTENCIA ... en el archivo .. " + outputfilepath );
	        logger.debug("--------------------------------------------------------------------------");
	        
	        PdfWriter writer = null;
	        // step 1
	        	
	        // Specifying the page size
	        	Document document = new Document(PageSize.A4);
	        
	        
	        // step 2
	            try {
	            	 writer = PdfWriter.getInstance(document, new FileOutputStream(outputfilepath));
	            	// Añadir encabezado y pie
	 	            HeaderFooter event = new HeaderFooter("Informe de estado :");
	 	            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
	 	            writer.setPageEvent(event);
				} catch (FileNotFoundException | DocumentException e) {
					
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        // step 3
	            document.open();
	        // step 4
	            try {
	            	//añadimos los datos 
	            	contarExpedientes(document);
				 	//addDatosExpediente(document, expediente);
				   	document.add(new Paragraph(" "));
				   	//añadimos las líneas los trámites
				    //document.add(getTableTramites(listaTramites));
				    
				    //Añadimos expedientes relacionados
				    document.add(new Paragraph(" "));
				    document.add(new Paragraph(" "));
				    document.add(new Paragraph(" "));
				    
				    /*PdfPTable auxDataHeader = new PdfPTable(new float[] {5});
		            auxDataHeader.setWidthPercentage(85);
		            auxDataHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		            Font font5 = new Font(FontFamily.UNDEFINED, 9, Font.BOLD, BaseColor.BLACK);
				    
				    
				    //añadimos leyenda
				    document.add(new Paragraph(" "));
				    document.add(new Paragraph(" "));
				    document.add(new Paragraph(" "));
				    addLeyenda(document);
				    
				    //añadimos el esquema de tramitación
				    //addEsquemaTramitacion(document,listaTramites,writer);
				    //document.add(new Paragraph(" "));
				    
				   
				    document.add(new Paragraph(" "));*/
				    
				    Paragraph fin = new Paragraph(" ----- ");
				    fin.setAlignment(Element.ALIGN_CENTER);
				    document.add(fin);
				    
				    } catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				    }
		      // step 5
	            document.close();
	        
			return relativefilepath;
		
	    }
	    /**
	     * Add a header table to the document
	     * @param document The document to which you want to add a header table
	     * @param day The day that needs to be shown in the header table
	     * @param page The page number that has to be shown in the header
	     * @return the height of the resulting header table
	     * @throws DocumentException
	     */
	    public float contarExpedientes(Document document) throws DocumentException{
	    
	    	SqlSession dbSession =MyBatisUtil.getSqlSessionFactory().openSession();
			TramiteActivoXExpedienteMapper service = null;
			ExpedienteMapper service2 = null;
	        if (dbSession!=null){
	            try {
	              service = dbSession.getMapper(TramiteActivoXExpedienteMapper.class);
	              service2 = dbSession.getMapper(ExpedienteMapper.class);
	    	
				//Lista de todos los expedientes (tabla expedientes, para comparar con el resultado de tabla trámites)
		        ExpedienteExample eExample = new ExpedienteExample();
		        eExample.createCriteria().andIdIsNotNull();
		        this.totalExpedientesList=service2.selectByExample(eExample);
		      //Lista de expedientes con trámites activos
	              TramiteActivoXExpedienteExample tExample = new TramiteActivoXExpedienteExample();
	              tExample.createCriteria().andIdIsNotNull();
	              this.expedientesConTramiteActivo=service.selectByExample(tExample);
	              List<Integer> lecT = new ArrayList<Integer>();
	              for (int i=0;i<this.expedientesConTramiteActivo.size();i++){
	            	  lecT.add(this.expedientesConTramiteActivo.get(i).getId());
	              }
	          //Lista de expedientes que no tienen trámite activo
	              ExpedienteExample eExample2 = new ExpedienteExample();
	              eExample2.createCriteria().andIdNotIn(lecT);
	              this.expedientesSinTramiteActivo=service2.selectByExample(eExample2);
	              
	         }catch(Exception e){
	           	logger.error("Error: ",e);
	                        
	         }finally{
	          	
	             dbSession.close();
	             
	    	}
	        }
	        //Total Absoluto de Expedientes
	        Integer totalAbsoluto = this.totalExpedientesList.size();
	        //Total Expedientes con trámites activos
	        Integer totalExpedientesActivos = this.expedientesConTramiteActivo.size();
	        //Total Expedientes en Trámite
	        
	            
		    PdfPTable mainData = new PdfPTable(new float[] { 2, 5});
		    mainData.setWidthPercentage(100);
		    mainData.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		    Font font = new Font(FontFamily.UNDEFINED, 12, Font.NORMAL, BaseColor.BLACK);
		    Font font2 = new Font(FontFamily.UNDEFINED, 12, Font.BOLD, BaseColor.BLACK);
		    mainData.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
		    Phrase p = null;
		            p = new Phrase("TOTAL DE EXPEDIENTES " , font);
		            mainData.addCell(p);
		            mainData.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));
		            p = new Phrase(String.valueOf(totalAbsoluto), font2);
		            mainData.addCell(p);
		            mainData.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
		            p = new Phrase("Expedientes con trámite activo", font);
		            mainData.addCell(p);
		            mainData.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));
		            p = new Phrase(String.valueOf(totalExpedientesActivos), font2);
		            mainData.addCell(p);
		           
		            document.add(mainData);
		            		          
		            document.add(new Paragraph(" "));
		            PdfPTable expedientesSinTramiteActivo = new PdfPTable(new float[] {5});
				    expedientesSinTramiteActivo.setWidthPercentage(100);
				    expedientesSinTramiteActivo.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
				    expedientesSinTramiteActivo.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
				    p = new Phrase("EXPEDIENTES SIN TRÁMITE ACTIVO: "+this.expedientesSinTramiteActivo.size() , font);
				            expedientesSinTramiteActivo.addCell(p);
				            expedientesSinTramiteActivo.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));
				            Expediente e = new Expediente();
				            for (int i=0; i<this.expedientesSinTramiteActivo.size();i++){
				            	e = this.expedientesSinTramiteActivo.get(i);
				            	String texto = "Interesado: "+e.getInteresado()+" - Tipo de Expediente: "+e.getTipoExpediente()+" - Actuación: "+e.getActuacion();
				            	p = new Phrase(texto, font);
				            	expedientesSinTramiteActivo.addCell(p);
				            	
				            }
				           
				            document.add(expedientesSinTramiteActivo);
		            	            
		            return 0;
	    }

	    public float addLeyenda(Document document)
	            throws DocumentException {
	           
	            
	            
	    	 	Font font5 = new Font(FontFamily.UNDEFINED, 9, Font.BOLD, BaseColor.BLACK);
	            PdfPTable auxDataHeader = new PdfPTable(new float[] {5});
	            auxDataHeader.setWidthPercentage(85);
	            auxDataHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
	            Phrase p  = new Phrase("TABLA DE ESTADOS" , font5);
	            auxDataHeader.addCell(p);
	            document.add(auxDataHeader);
	            Font font3 = new Font(FontFamily.UNDEFINED, 7, Font.BOLD, BaseColor.BLACK);
	            Font font4 = new Font(FontFamily.UNDEFINED, 7, Font.NORMAL, BaseColor.BLACK);
	            PdfPTable auxData = new PdfPTable(new float[] { 2, 4,2,4});
	            auxData.setWidthPercentage(95);
	            auxData.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
	            
	            p  = new Phrase("I-SUBSANACION" , font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE SUBSANACIÓN INICIAL", font4);
	            auxData.addCell(p);
	            p = new Phrase("T-PROVIDENCIA " , font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE HACER PROVIDENCIA", font4);
	            auxData.addCell(p);
	            p = new Phrase("T-INFTECNICO ", font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE INFORME TÉCNICO", font4);
	            auxData.addCell(p);
	            p = new Phrase("T-INFORMEJURIDICO ", font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE INFORME JURÍDICO", font4);
	            auxData.addCell(p);
	            p = new Phrase("T-SUBSANACION ", font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE SUBSANACIÓN DESPUÉS DE INFORMES TÉCNICO Y JURÍDICO", font4);
	            auxData.addCell(p);
	            p = new Phrase("F-RESOLUCION ", font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE RESOLUCIÓN", font4);
	            auxData.addCell(p);
	            p = new Phrase("F-NOTIFICACION ", font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE NOTIFICACIÓN", font4);
	            auxData.addCell(p);
	            p = new Phrase("F-NOTIFICACION ", font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE NOTIFICACIÓN", font4);
	            auxData.addCell(p);
	            p = new Phrase("F-ARCHIVO ", font3);
	            auxData.addCell(p);
	            p = new Phrase("PENDIENTE DE ARCHIVO", font4);
	            auxData.addCell(p);
	            p = new Phrase("FINALIZADO ", font3);
	            auxData.addCell(p);
	            p = new Phrase("EXPEDIENTE FINALIZADO Y ARCHIVADO", font4);
	            auxData.addCell(p);
	            p = new Phrase("T-OTROS ", font3);
	            auxData.addCell(p);
	            p = new Phrase("Otros Estados, p.e. pendiente de informe sectorial", font4);
	            auxData.addCell(p);
	            
	            document.add(auxData);
	            	            
	            return auxData.getTotalHeight()+auxData.getFooterHeight();
	           /* sestadosExpediente = new String[10];
	            sestadosExpediente[0] = "I-SUBSANACION"; //PENDIENTE DE SUBSANACIÓN INICIAL
	            sestadosExpediente[1] = "T-PROVIDENCIA"; //PENDIENTE DE HACER PROVIDENCIA
	            sestadosExpediente[2] = "T-INFTECNICO";  //PENDIENTE DE INFORME TÉCNICO
	            sestadosExpediente[3] = "T-INFORMEJURIDICO"; //PENDIENTE DE INFORME JURÍDICO
	            sestadosExpediente[4] = "T-SUBSANACION"; //PENDIENTE DE SUBSANACIÓN TRAS INFORMES 
	            sestadosExpediente[5] = "F-RESOLUCION";  //PENDIENTE DE RESOLUCIÓN
	            sestadosExpediente[6] = "F-NOTIFICACION"; //PENDIENTE DE NOTIFICACIÓN
	            sestadosExpediente[7] = "F-ARCHIVO";     //PENDIENTE DE ARCHIVO
	            sestadosExpediente[8] = "FINALIZADO";    //EXPEDIENTE FINALIZADO Y ARCHIVADO
	            sestadosExpediente[9] = "seleccionar ..."; */
	    }
	    
	    public float addEsquemaTramitacion(Document document,List<TramiteExpediente> tse, PdfWriter writer ){
	    	 
	    		   
	    		  PdfContentByte canvas = writer.getDirectContent();
	    	        // draw squares
	    	        createSquares(canvas, 50, 720, 80, 20);
	    	        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
	    	            new Phrase(
	    	                "Methods moveTo(), lineTo(), stroke(), closePathStroke(), fill(), and closePathFill()"),
	    	                50, 700, 0);
	    	        // draw Bezier curves
	    	        createBezierCurves(canvas, 70, 600, 80, 670, 140, 690, 160, 630, 160);
	    	        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
	    	            new Phrase("Different curveTo() methods, followed by stroke()"), 50, 580, 0);
	    	       
	    	        // draw different shapes using convenience methods
	    	        canvas.saveState();
	    	        canvas.setColorStroke(new GrayColor(0.2f));
	    	        canvas.setColorFill(new GrayColor(0.9f));
	    	        canvas.arc(50, 270, 150, 330, 45, 270);
	    	        canvas.ellipse(170, 270, 270, 330);
	    	        canvas.circle(320, 300, 30);
	    	        canvas.roundRectangle(370, 270, 80, 60, 20);
	    	        canvas.fillStroke();
	    	        canvas.restoreState();
	    	        Rectangle rect = new Rectangle(470, 270, 550, 330);
	    	        rect.setBorderWidthBottom(10);
	    	        rect.setBorderColorBottom(new GrayColor(0f));
	    	        rect.setBorderWidthLeft(4);
	    	        rect.setBorderColorLeft(new GrayColor(0.9f));
	    	        rect.setBackgroundColor(new GrayColor(0.4f));
	    	        canvas.rectangle(rect);
	    	        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
	    	            new Phrase("Convenience methods"), 50, 250, 0);
	    		 
	    	 
	    	
	    	
	    	
	    	return 0;
	    	
	    
	    }
	    
	     private void createBezierCurves(PdfContentByte canvas, int i, int j,
				int k, int l, int m, int n, int o, int p, int q) {
			// TODO Auto-generated method stub
			
		}
		public PdfPTable getTableTramites(List<TramiteExpediente> tse)
	            throws DocumentException, IOException {
	            PdfPTable table = new PdfPTable(new float[] { 1, 5, 2,2, 1 });
	            table.setWidthPercentage(100f);
	            table.getDefaultCell().setUseAscender(true);
	            table.getDefaultCell().setUseDescender(true);
	            Font font = new Font(FontFamily.UNDEFINED, 8, Font.BOLD, BaseColor.BLACK);
	            table.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));          
	            for (int i = 0; i < 2; i++) {
	                table.addCell(new Phrase("FECHA" , font));
	                table.addCell(new Phrase("TRAMITE" , font));
	                table.addCell(new Phrase("DESTINO" , font));
	                table.addCell(new Phrase("ESTADO" , font));
	                table.addCell(new Phrase("ACTIVO" , font));
	            }
	            
	            table.getDefaultCell().setBackgroundColor(null);
	            table.getDefaultCell().setBorder(Rectangle.BOTTOM);
	            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
	            table.setHeaderRows(2);
	            table.setFooterRows(1);
	            
	            font = new Font(FontFamily.UNDEFINED, 8, Font.NORMAL, BaseColor.BLACK);
	            String activoStr = "NO";
	            for (TramiteExpediente tramite : tse) {
	            	if(tramite.getTramiteActivo()==true){
	            		table.getDefaultCell().setBackgroundColor(new BaseColor(220, 220, 220) );
	            		activoStr="SÍ";
	            	}
	            	DateFormat df2 = DateFormat.getDateInstance(DateFormat.MEDIUM);
		            String tFecha = df2.format(tramite.getFechaTramite());
	            	table.addCell(new Phrase(tFecha,font));
	            	table.addCell(new Phrase(tramite.getDescripcionTramite(),font));
	                table.addCell(new Phrase(tramite.getTramiteAsignadoA(),font));
	                table.addCell(new Phrase(tramite.getEstadoTramite(),font));
	                table.addCell(new Phrase(activoStr,font));
	                             
	            }
	            
	            return table;
	        }
			public PdfPTable getTableExpedientesRelacionados(List<Expediente> tse)
	            throws DocumentException, IOException {
				
				
				PdfPTable table = new PdfPTable(new float[] { 1, 1, (float) 1.5, 1, 1, (float) 1.5, (float) 3.5, 2, (float) 2.5});
	            table.setWidthPercentage(100f);
	            table.getDefaultCell().setUseAscender(true);
	            table.getDefaultCell().setUseDescender(true);
	            Font font = new Font(FontFamily.UNDEFINED, 8, Font.BOLD, BaseColor.BLACK);
	            table.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));          
	            for (int i = 0; i < 1; i++) {
	            	table.addCell(new Phrase("ID" , font));
	            	table.addCell(new Phrase("ENTRADA" , font));
	                table.addCell(new Phrase("FECHA" , font));
	                table.addCell(new Phrase("EXPEDIENTE" , font));
	                table.addCell(new Phrase("AÑO" , font));
	                table.addCell(new Phrase("DNI" , font));
	                table.addCell(new Phrase("INTERESADO" , font));
	                table.addCell(new Phrase("TIPO EXPTE" , font));
	                table.addCell(new Phrase("ESTADO EXPTE" , font));
	            }
	            
	            table.getDefaultCell().setBackgroundColor(null);
	            table.getDefaultCell().setBorder(Rectangle.BOTTOM);
	            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
	            table.setHeaderRows(1);
	            table.setFooterRows(0);
	            
	            font = new Font(FontFamily.UNDEFINED, 8, Font.NORMAL, BaseColor.BLACK);
	            String activoStr = "NO";
	            for (Expediente expediente : tse) {
	            	
	            	DateFormat df2 = DateFormat.getDateInstance(DateFormat.MEDIUM);
		            String tFecha = df2.format(expediente.getFechaEntrada());
		            table.addCell(new Phrase(String.valueOf(expediente.getId()),font));
		            table.addCell(new Phrase(String.valueOf(expediente.getNumEntrada()),font));
	            	table.addCell(new Phrase(tFecha,font));
	            	table.addCell(new Phrase(String.valueOf(expediente.getExpediente()),font));
	            	table.addCell(new Phrase(String.valueOf(expediente.getAnyo()),font));
	                table.addCell(new Phrase(expediente.getNifInteresado(),font));
	                table.addCell(new Phrase(expediente.getInteresado(),font));
	                table.addCell(new Phrase(expediente.getTipoExpediente(),font));
	                table.addCell(new Phrase(expediente.getEstadoExpediente(),font));
	                             
	            }
	            
	            return table;
	        }
	     
	     public void createSquares(PdfContentByte canvas,
	    	        float x, float y, float side, float gutter) {
	    	        canvas.saveState();
	    	        canvas.setColorStroke(new GrayColor(0.2f));
	    	        canvas.setColorFill(new GrayColor(0.9f));
	    	        canvas.moveTo(x, y);
	    	        canvas.lineTo(x + side, y);
	    	        canvas.lineTo(x + side, y + side);
	    	        canvas.lineTo(x, y + side);
	    	        canvas.stroke();
	    	        x = x + side + gutter;
	    	        canvas.moveTo(x, y);
	    	        canvas.lineTo(x + side, y);
	    	        canvas.lineTo(x + side, y + side);
	    	        canvas.lineTo(x, y + side);
	    	        canvas.closePathStroke();
	    	        x = x + side + gutter;
	    	        canvas.moveTo(x, y);
	    	        canvas.lineTo(x + side, y);
	    	        canvas.lineTo(x + side, y + side);
	    	        canvas.lineTo(x, y + side);
	    	        canvas.fill();
	    	        x = x + side + gutter;
	    	        canvas.moveTo(x, y);
	    	        canvas.lineTo(x + side, y);
	    	        canvas.lineTo(x + side, y + side);
	    	        canvas.lineTo(x, y + side);
	    	        canvas.fillStroke();
	    	        x = x + side + gutter;
	    	        canvas.moveTo(x, y);
	    	        canvas.lineTo(x + side, y);
	    	        canvas.lineTo(x + side, y + side);
	    	        canvas.lineTo(x, y + side);
	    	        canvas.closePathFillStroke();
	    	        canvas.restoreState();
	    	    }
	     
	     class HeaderFooter extends PdfPageEventHelper {
	     
	    	 Phrase[] header = new Phrase[2];
	         /** Current page number (will be reset for every chapter). */
	         
	         int pagenumber;
	         String data = null;
	         Font font = new Font(FontFamily.UNDEFINED, 7, Font.BOLDITALIC, BaseColor.DARK_GRAY);
		     
	         public HeaderFooter(String datos){
		    	 this.data=datos;
		     }
	         /** Alternating phrase for the header. */
	         
	         /**
	          * Initialize one of the headers.
	          * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
	          *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
	          */
	         public void onOpenDocument(PdfWriter writer, Document document) {
	        	 Date ahora = new Date();
	        	 DateFormat df2 = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
	             header[0] = new Phrase(this.data+". -- Documento generado en "+df2.format(ahora),this.font);
	             pagenumber = 0;
	         }
	  
	         /**
	          * Initialize one of the headers, based on the chapter title;
	          * reset the page number.
	          * @see com.itextpdf.text.pdf.PdfPageEventHelper#onChapter(
	          *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, float,
	          *      com.itextpdf.text.Paragraph)
	          */
	         public void onChapter(PdfWriter writer, Document document,
	                 float paragraphPosition, Paragraph title) {
	             header[1] = new Phrase(title.getContent());
	             pagenumber = 1;
	         }
	  
	         /**
	          * Increase the page number.
	          * @see com.itextpdf.text.pdf.PdfPageEventHelper#onStartPage(
	          *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
	          */
	         public void onStartPage(PdfWriter writer, Document document) {
	             pagenumber++;
	         }
	  
	         /**
	          * Adds the header and the footer.
	          * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
	          *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
	          */
	         public void onEndPage(PdfWriter writer, Document document) {
	             Rectangle rect = writer.getBoxSize("art");
	             switch(writer.getPageNumber() % 2) {
	             case 0:
	                 ColumnText.showTextAligned(writer.getDirectContent(),
	                         Element.ALIGN_RIGHT, header[0],
	                         rect.getRight()+28, rect.getTop()+40,0);
	                 break;
	             case 1:
	                 ColumnText.showTextAligned(writer.getDirectContent(),
	                         Element.ALIGN_LEFT, header[0],
	                         rect.getLeft()+28, rect.getTop()+40, 0);
	                 break;
	             }
	             ColumnText.showTextAligned(writer.getDirectContent(),
	                     Element.ALIGN_CENTER, new Phrase(String.format("Página %d", pagenumber),this.font),
	                     (rect.getLeft() + rect.getRight()) / 2, rect.getBottom() - 18, 0);
	         	}
	     	}

		}
