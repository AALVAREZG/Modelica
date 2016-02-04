package es.aalvarez.modelica.poi;



import org.apache.poi.xwpf.usermodel.*;

import es.aalvarez.modelica.model.ArticuloInformeJuridico;
import es.aalvarez.modelica.model.Expediente;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;



/**
 * A simple WOrdprocessingML document created by POI XWPF API
 *
 * @author Yegor Kozlov
 */

public class GeneraInformeJuridicoObraMayor {
	
	
	public String replaceTextFound(String path, Expediente licenciaOM, List<ArticuloInformeJuridico> articulosInsertados) throws IOException {
		String numExpediente = String.format("%04d",licenciaOM.getAnyo())+String.format("%03d",licenciaOM.getExpediente());
		System.out.println(numExpediente);
		String realContextPath = path; 
		String inputfilepath = realContextPath+"/models/OBRAMAYOR/"+ "INFJURIDICO.docx";
		String relativeOutputfilepath = "/docs/obraMayor/BORRADOR_InfJco_"+ licenciaOM.getAnyo()+"-"+numExpediente+".docx";
		String outputfilepath = realContextPath+relativeOutputfilepath;
		System.out.println(outputfilepath);
		InputStream fs = new FileInputStream(inputfilepath);
		XWPFDocument doc = new XWPFDocument(fs); 
		Integer posicion=0;
		
		for (XWPFParagraph p : doc.getParagraphs()) {
		    List<XWPFRun> runs = p.getRuns();
		    if (runs != null) {
		        for (XWPFRun r : runs) {
		        	String text = r.getText(0);
		        	System.out.println("Texto del run ---> " +text);
		        	if (text != null){
		        		 
		        		if(text.contains("$NUMEXPTE")) {
			                text = text.replace("$NUMEXPTE", licenciaOM.getExpediente().toString());
			                r.setText(text, 0);
		        		}if(text.contains("$EXPTEANYO")) {
			                text = text.replace("$EXPTEANYO", licenciaOM.getAnyo().toString());
			                r.setText(text, 0);
		        		}
		        		        		
		        		if(text.contains("$ACTUACION")) {
			                text = text.replace("$ACTUACION", licenciaOM.getActuacion());
			                r.setText(text, 0);
		        		}
		        		if(text.contains("$NORMATIVA")) {
			                
			                posicion = r.getTextPosition();
		        		}
		        }
		    }
		}
		   
		} 
		
		if (articulosInsertados!=null && articulosInsertados.size()>0){
				 //create Paragraph
			/*System.out.println("numero de artículos" + articulosInsertados.size());
			 for (ArticuloInformeJuridico r : articulosInsertados) {
				 System.out.println(r.getContenidoArticulo());
			 }*/
			System.out.println(numExpediente);
				   XWPFParagraph paragraph = doc.createParagraph();
				   XWPFRun run=paragraph.createRun();
				   System.out.println("Insertando Artículo" + articulosInsertados.get(0).getLex() + " " + articulosInsertados.get(0).getArticulo() );
				   run.setText(articulosInsertados.get(0).getContenidoArticulo(), posicion);
			
		}
		
		FileOutputStream out = new FileOutputStream(outputfilepath);
	    doc.write(out);
	    out.close(); 
	
	    return relativeOutputfilepath;
	}
}




