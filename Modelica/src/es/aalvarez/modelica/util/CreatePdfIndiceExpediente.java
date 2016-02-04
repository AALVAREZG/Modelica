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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

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

import es.aalvarez.modelica.model.Expediente;
import es.aalvarez.modelica.model.TramiteExpediente;



public class CreatePdfIndiceExpediente {
	
	
	
	
	 public String generaPDF(Expediente expediente, List<TramiteExpediente> listaTramites){
	    	
	    	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
	                .getExternalContext().getContext();
	    
	    	String realContextPath = ctx.getRealPath("/");
			
			String outputfilepath = realContextPath+"/docs/REPORTS"+  "/report_02.pdf";
			System.out.println(realContextPath);
	    	
			/** Path to the resulting PDF file. */
			
	        final String RESULT  = outputfilepath;
	        System.out.println("generando pdf ÍNDICE del EXPEDIENTE ..." +expediente.getId() + " en el archivo .. " + RESULT );
	        
	        PdfWriter writer = null;
	        // step 1
	        	
	        // Specifying the page size
	        	Document document = new Document(PageSize.A4);
	        
	        
	        // step 2
	            try {
	            	 writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
	            	// Añadir encabezado y pie
	 	            HeaderFooter event = new HeaderFooter(expediente);
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
	            	Paragraph inicio = new Paragraph("Índice del Expediente");
	            	inicio.setAlignment(Element.ALIGN_CENTER);
	            	Font font = new Font(FontFamily.UNDEFINED, 12, Font.BOLD, new BaseColor(61,70,86));
	            	inicio.setFont(font);
	            	document.add(inicio);
	            	 document.add(new Paragraph(" "));
	            	//añadimos los datos del EXPEDIENTE
				 	addDatosExpediente(document, expediente);
				   	document.add(new Paragraph(" "));
				   	//añadimos las líneas los trámites
				    document.add(getTable(listaTramites));
				    document.add(new Paragraph(" "));
				    //addLeyenda(document);
				    //añadimos el esquema de tramitación
				    //addEsquemaTramitacion(document,listaTramites,writer);
				    //document.add(new Paragraph(" "));
				   
				   
				    document.add(new Paragraph(" "));
				    Paragraph fin = new Paragraph(" ----- ");
				    fin.setAlignment(Element.ALIGN_CENTER);
				    document.add(fin);
				    
				    } catch (IOException | DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				    }
		      // step 5
	            document.close();
	        
			return RESULT;
		
	    }
	    /**
	     * Add a header table to the document
	     * @param document The document to which you want to add a header table
	     * @param day The day that needs to be shown in the header table
	     * @param page The page number that has to be shown in the header
	     * @return the height of the resulting header table
	     * @throws DocumentException
	     */
	    public float addDatosExpediente(Document document, Expediente expediente)
	            throws DocumentException {
	            PdfPTable header = new PdfPTable(4);
	            header.setWidthPercentage(100);
	            header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
	            Font font = new Font(FontFamily.UNDEFINED, 12, Font.BOLD, BaseColor.WHITE);
	            header.getDefaultCell().setBackgroundColor(new BaseColor(125,140,161));
	            Phrase p = new Phrase("TIPO EXPEDIENTE " , font);
	            header.addCell(p);
	            
	            p = new Phrase("NUM. EXPEDIENTE ", font);
	            header.addCell(p);
	           
	            p = new Phrase("FECHA SOLICITUD: ", font);
	            header.addCell(p);
	            p = new Phrase("NUM. REGISTRO: ", font);
	            header.addCell(p);
	            font = new Font(FontFamily.UNDEFINED, 12 , Font.NORMAL, BaseColor.BLACK);
	            p = new Phrase(expediente.getTipoExpediente() , font);
	            header.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));
	            header.addCell(p);
	            p = new Phrase(String.valueOf(expediente.getExpediente())+"-"+String.valueOf(expediente.getAnyo()) , font);
	            header.addCell(p);
	            DateFormat df2 = DateFormat.getDateInstance(DateFormat.SHORT);
	            String tFecha = df2.format(expediente.getFechaEntrada());
	            p = new Phrase(tFecha , font);
	            header.addCell(p);
	            p = new Phrase(String.valueOf(expediente.getNumEntrada()) , font);
	            header.addCell(p);
	            document.add(header);
	            
	            document.add(new Paragraph(" "));
	            
	            PdfPTable mainData = new PdfPTable(new float[] { 3, 5, 1, 2 });
	            mainData.setWidthPercentage(100);
	            mainData.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
	            Font font2 = new Font(FontFamily.UNDEFINED, 12, Font.BOLD, BaseColor.BLACK);
	            mainData.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
	            p = new Phrase("INTERESADO " , font);
	            mainData.addCell(p);
	            mainData.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));
	            p = new Phrase(expediente.getInteresado(), font2);
	            mainData.addCell(p);
	            mainData.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
	            p = new Phrase("DNI", font);
	            mainData.addCell(p);
	            mainData.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));
	            p = new Phrase(expediente.getNifInteresado(), font2);
	            mainData.addCell(p);
	            mainData.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
	            p = new Phrase("REPRESENTANTE " , font);
	            mainData.addCell(p);
	            mainData.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));
	            p = new Phrase(expediente.getRepresentante(), font2);
	            mainData.addCell(p);
	            mainData.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
	            p = new Phrase("DNI", font);
	            mainData.addCell(p);
	            mainData.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));
	            p = new Phrase(expediente.getNifRepresentante(), font2);
	            mainData.addCell(p);
	            document.add(mainData);
	            
	            document.add(new Paragraph(" "));
	            
	            PdfPTable auxData = new PdfPTable(new float[] { 2, 6});
	            auxData.setWidthPercentage(100);
	            auxData.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
	            Font font3 = new Font(FontFamily.UNDEFINED, 9, Font.NORMAL, BaseColor.BLACK);
	            Font font4 = new Font(FontFamily.UNDEFINED, 9, Font.BOLD, BaseColor.BLACK);
	            Font font5 = new Font(FontFamily.UNDEFINED, 9, Font.BOLD, BaseColor.WHITE);
	            p = new Phrase("DESCRIPCIÓN " , font3);
	            auxData.addCell(p);
	            auxData.getDefaultCell().setBackgroundColor(new BaseColor(125,140,161));
	            p = new Phrase(expediente.getActuacion(), font5);
	            auxData.addCell(p);
	            auxData.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
	            p = new Phrase("EMPLAZAMIENTO " , font3);
	            auxData.addCell(p);
	            p = new Phrase(expediente.getEmplazamiento(), font4);
	            auxData.addCell(p);
	            p = new Phrase("REF. CATASTRAL ", font3);
	            auxData.addCell(p);
	            p = new Phrase(expediente.getRefCatastral(), font4);
	            auxData.addCell(p);
	            document.add(auxData);
	            	            
	            return header.getTotalHeight()+auxData.getFooterHeight();
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
	    
	  
		public PdfPTable getTable(List<TramiteExpediente> tse)
	            throws DocumentException, IOException {
	            PdfPTable table = new PdfPTable(new float[] { 1, 5, 1 });
	            table.setWidthPercentage(100f);
	            table.getDefaultCell().setUseAscender(true);
	            table.getDefaultCell().setUseDescender(true);
	            Font font = new Font(FontFamily.UNDEFINED, 8, Font.BOLD, BaseColor.BLACK);
	            table.getDefaultCell().setBackgroundColor(new BaseColor(245,243,238));          
	            for (int i = 0; i < 2; i++) {
	                table.addCell(new Phrase("FECHA" , font));
	                table.addCell(new Phrase("TRAMITE" , font));
	                table.addCell(new Phrase("PÁGs" , font));
	                
	            }
	            
	            table.getDefaultCell().setBackgroundColor(null);
	            table.getDefaultCell().setBorder(Rectangle.BOTTOM);
	            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
	            table.setHeaderRows(2);
	            table.setFooterRows(1);
	            
	            font = new Font(FontFamily.UNDEFINED, 8, Font.NORMAL, BaseColor.BLACK);
	           
	            for (TramiteExpediente tramite : tse) {
	            	
	            	if(tramite.getIncluirEnIndice()==true){
	            	DateFormat df2 = DateFormat.getDateInstance(DateFormat.MEDIUM);
		            String tFecha = df2.format(tramite.getFechaTramite());
	            	table.addCell(new Phrase(tFecha,font));
	            	table.addCell(new Phrase(tramite.getDescripcionTramite(),font));
	                table.addCell(new Phrase(" ",font));
	                
	            	}
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
	         Expediente expediente = new Expediente();
	         Font font = new Font(FontFamily.UNDEFINED, 7, Font.BOLDITALIC, BaseColor.DARK_GRAY);
		     public HeaderFooter(Expediente expediente){
		    	 this.expediente=expediente;
		     }
	         /** Alternating phrase for the header. */
	         
	         /**
	          * Initialize one of the headers.
	          * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
	          *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
	          */
	         public void onOpenDocument(PdfWriter writer, Document document) {
	        	 Date ahora = new Date();
	        	 DateFormat df2 = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
	             header[0] = new Phrase("Expediente: "+expediente.getId()+". -- Documento generado en "+df2.format(ahora),this.font);
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
