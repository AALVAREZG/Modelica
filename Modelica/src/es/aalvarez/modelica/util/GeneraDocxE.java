package es.aalvarez.modelica.util;



import org.apache.poi.xwpf.usermodel.*;

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

public class GeneraDocxE {
	
	public void replaceTextProvidenciaOM(String path, Expediente licenciaOM) throws IOException {
		
		String realContextPath = path; 
		String inputfilepath = realContextPath+"/models"+ "/MODELO-PROVIDENCIA-OBRAMAYOR.docx";
		String outputfilepath = realContextPath+"/docs/OBRAMAYOR/Providencia"+ licenciaOM.getRefCatastral() +".docx";
		System.out.println(realContextPath);
		InputStream fs = new FileInputStream(inputfilepath);
		XWPFDocument doc = new XWPFDocument(fs); 
		
		for (XWPFParagraph p : doc.getParagraphs()) {
		    List<XWPFRun> runs = p.getRuns();
		    if (runs != null) {
		        for (XWPFRun r : runs) {
		        	String text = r.getText(0);
		        	System.out.println("Texto del run ---> " +text);
		        	if (text != null){
		        		if (text.contains("$FECHA")) {
			        	    text = text.replace("$FECHA", String.valueOf(licenciaOM.getFechaEntrada()));
			                r.setText(text, 0);
			        	}if (text.contains("$NUMENTRADA")) {
			        	    text = text.replace("$NUMENTRADA", licenciaOM.getNumEntrada().toString());
			                r.setText(text, 0);
			        	}if (text.contains("$NUMEXPTE")) {
			        	    text = text.replace("$NUMEXPTE", String.valueOf(licenciaOM.getExpediente()));
			                r.setText(text, 0);
			        	}if (text.contains("$ANYOEXPTE")) {
			        	    text = text.replace("$ANYOEXPTE", String.valueOf(licenciaOM.getAnyo()));
			                r.setText(text, 0);
			        	}if (text.contains("$NOMBRE")) {
		        	    text = text.replace("$NOMBRE", licenciaOM.getInteresado());
		                r.setText(text, 0);
		        		}if(text.contains("$NIF")) {
			                text = text.replace("$NIF", licenciaOM.getNifInteresado());
			                r.setText(text, 0);
		        		}if(text.contains("$ACTUACION")) {
			                text = text.replace("$ACTUACION", licenciaOM.getActuacion());
			                r.setText(text, 0);
		        		}if(text.contains("$EMPLAZAMIENTO")) {
			                text = text.replace("$EMPLAZAMIENTO", licenciaOM.getEmplazamiento());
			                r.setText(text, 0);
		        		}if(text.contains("$PROYECTO")) {
			                text = text.replace("$PROYECTO", licenciaOM.getDocTecnico());
			                r.setText(text, 0);
		        		}if(text.contains("$TECNICO")) {
			                text = text.replace("$TECNICO", licenciaOM.getRedactor());
			                r.setText(text, 0);
		        		}if(text.contains("$COLEGIADO")) {
			                text = text.replace("$COLEGIADO", licenciaOM.getNumColegiado());
			                r.setText(text, 0);
		        		}if(text.contains("$FECHA-PROVIDENCIA")) {
			                text = text.replace("$FECHAPROVIDENCIA", String.valueOf(licenciaOM.getProvidenciaFecha()));
			                r.setText(text, 0);
		        		}if(text.contains("$CARGO")) {
			                text = text.replace("$CARGO", licenciaOM.getProvidenciaFirmanteCargo());
			                r.setText(text, 0);
		        		}if(text.contains("$NOMBRE-CARGO")) {
			                text = text.replace("$NOMBRE-CARGO", licenciaOM.getProvidenciaFirmante());
			                r.setText(text, 0);
		        		}
		        		
		        }
		    }
		}
		
		FileOutputStream out = new FileOutputStream(outputfilepath);
	    doc.write(out);
	    out.close();
	}
  }
	
	public void replaceTextFound(String path, Expediente licenciaOM) throws IOException {
		
		String realContextPath = path; 
		String inputfilepath = realContextPath+"/models"+ "/PROVIDENCIA.docx";
		String outputfilepath = realContextPath+"/models"+  "/OUT_ProvidenciaPOI.docx";
		System.out.println(realContextPath);
		InputStream fs = new FileInputStream(inputfilepath);
		XWPFDocument doc = new XWPFDocument(fs); 
		
		for (XWPFParagraph p : doc.getParagraphs()) {
		    List<XWPFRun> runs = p.getRuns();
		    if (runs != null) {
		        for (XWPFRun r : runs) {
		        	String text = r.getText(0);
		        System.out.println("Texto del run ---> " +text);
		        	if (text != null){
		        		if (text.contains("$NOMBRE")) {
		        	    text = text.replace("$NOMBRE", licenciaOM.getInteresado());
		                r.setText(text, 0);
		        		}if(text.contains("$2NOMBRE}")) {
		                text = text.replace("$2NOMBRE}", licenciaOM.getInteresado());
		                r.setText(text, 0);
		        		}if(text.contains("$NIF")) {
			                text = text.replace("$NIF", "00000000T-");
			                r.setText(text, 0);
		        		}if(text.contains("$FECHA-ENTRADA")) {
			                text = text.replace("$FECHA-ENTRADA", "25-febrero-2015");
			                r.setText(text, 0);
		        		}if(text.contains("$NUM-ENTRADA")) {
			                text = text.replace("$NUM-ENTRADA", "2582");
			                r.setText(text, 0);
		        		}if(text.contains("$FECHA-SALIDA")) {
			                text = text.replace("$FECHA-SALIDA", "25-DICEMBRE-2015");
			                r.setText(text, 0);
		        		}if(text.contains("$NUM-SALIDA")) {
			                text = text.replace("$NUM-SALIDA", "9999");
			                r.setText(text, 0);
		        		}
		        		if(text.contains("$EXPEDIENTE")) {
			                text = text.replace("$EXPEDIENTE", licenciaOM.getExpediente().toString()+"-"+licenciaOM.getAnyo().toString());
			                r.setText(text, 0);
		        		}
		        }
		    }
		}
		
		FileOutputStream out = new FileOutputStream(outputfilepath);
	    doc.write(out);
	    out.close();
	}
  }
}



