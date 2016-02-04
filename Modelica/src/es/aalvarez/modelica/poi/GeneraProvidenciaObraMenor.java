package es.aalvarez.modelica.poi;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;

import es.aalvarez.modelica.managedbeans.DashboardMB;
import es.aalvarez.modelica.model.Expediente;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.List;



/**
 * A simple WOrdprocessingML document created by POI XWPF API
 *
 * @author Yegor Kozlov
 */

public class GeneraProvidenciaObraMenor {
	final  Logger logger = LogManager.getLogger(GeneraProvidenciaObraMenor.class);
	
	public String replaceTextFound(String path, Expediente licenciaOM) throws IOException {
		String numExpediente = String.format("%04d",licenciaOM.getAnyo())+String.format("%03d",licenciaOM.getExpediente());
		System.out.println(numExpediente);
		String realContextPath = path; 
		String inputfilepath = realContextPath+"/models/OBRAMENOR/"+ "/PROVIDENCIA-oMenor.docx";
		String relativeOutputfilepath = "/docs/OBRAMENOR/"+numExpediente+"_Providencia.docx";
		String outputfilepath = realContextPath+relativeOutputfilepath;
		
		System.out.println(outputfilepath);
		InputStream fs = new FileInputStream(inputfilepath);
		XWPFDocument doc = new XWPFDocument(fs); 
		DateFormat df2 = DateFormat.getDateInstance(DateFormat.SHORT);
        String tFecha1 = df2.format(licenciaOM.getFechaEntrada());
        String tFecha2 = df2.format(licenciaOM.getProvidenciaFecha());
        String interesado, nifInteresado, representante, nifRepresentante, textoInteresado, textoActuacion, tecnico;
        textoInteresado="texto_tmp";
        textoActuacion="actuación";
        tecnico="técnico";
        if (licenciaOM.getRepresentante().isEmpty() || (licenciaOM.getRepresentante().compareTo(" ")==0)){
        	interesado = licenciaOM.getInteresado();
        	nifInteresado = licenciaOM.getNifInteresado();
        	textoInteresado = interesado + " con NIF " + nifInteresado + " actuando en su propio nombre y representación ";
        }else{
        	interesado = licenciaOM.getInteresado();
        	nifInteresado = licenciaOM.getNifInteresado();
        	representante = licenciaOM.getRepresentante();
        	nifRepresentante=licenciaOM.getNifRepresentante();
        	textoInteresado = representante + " con NIF " +  nifRepresentante + "actuando en nombre y representación de "+interesado +" con CIF "+ nifInteresado;
        }
        textoActuacion= licenciaOM.getActuacion()+", con emplazamiento en "+licenciaOM.getEmplazamiento()+" de esta localidad (Ref. Catastral: " + licenciaOM.getRefCatastral()
        		+ "); ";
        
        switch (licenciaOM.getProvidenciaTecnicoAsignado()) {
            	case "ARQUITECTO TECNICO (M. ÁNGELES)":
            		tecnico="Mª Ángeles Parrado Parrado";
            		break;
            	case "ARQUITECTO TECNICO (CÉSAR)":
            		tecnico="César Borrego Bermúdez";
            		break;
            	case "ARQUITECTO (JORGE)":
            		tecnico="Jorge Alberto Salas Lúcia";
            		break;
            	case "INGENIERO TÉCNICO (JOSÉ MANUEL)":
            		tecnico="José Manuel Sojo Torres";
            		break;
            		
      	  
      	}
        logger.debug(textoInteresado);
		for (XWPFParagraph p : doc.getParagraphs()) {
		    List<XWPFRun> runs = p.getRuns();
		    if (runs != null) {
		        for (XWPFRun r : runs) {
		        	String text = r.getText(0);
		        	System.out.println("Texto del run ---> " +text);
		        	if (text != null){
		        		if (text.contains("$FECHAENTRADA")) {
			        	    text = text.replace("$FECHAENTRADA", tFecha1);
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
			        	}if (text.contains("$INTERESADO")) {
			        		text = text.replace("$INTERESADO", textoInteresado);
			        		r.setText(text, 0);
		        		}if(text.contains("$ACTUACION")) {
			                text = text.replace("$ACTUACION", textoActuacion);
			                r.setText(text, 0);
		        		
		        		}if(text.contains("$FECHPROVIDENCIA")) {
			                text = text.replace("$FECHPROVIDENCIA", tFecha2);
			                r.setText(text, 0);
		        		}if(text.contains("$CARGO")) {
			                text = text.replace("$CARGO", licenciaOM.getProvidenciaFirmanteCargo());
			                r.setText(text, 0);
		        		}if(text.contains("$DELEGACION")) {
			                text = text.replace("$DELEGACION",licenciaOM.getProvidenciaFirmanteDelegacion());
			                r.setText(text, 0);
		        		}if(text.contains("$NOMBRCARGO")) {
			                text = text.replace("$NOMBRCARGO", licenciaOM.getProvidenciaFirmante());
			                r.setText(text, 0);
		        		}
		        }
		    }
		}
		
		
	}
		
		
		for (XWPFTable tbl : doc.getTables()) {
			   for (XWPFTableRow row : tbl.getRows()) {
			      for (XWPFTableCell cell : row.getTableCells()) {
			         for (XWPFParagraph p : cell.getParagraphs()) {
			            for (XWPFRun r : p.getRuns()) {
				              String text = r.getText(0);
				              if (text != null){
				              if(text.contains("$TECNICO")) {
					                text = text.replace("$TECNICO", tecnico);
					                r.setText(text, 0);
				        	  }if(text.contains("$CODIGOPROV")) {
					                text = text.replace("$CODIGOPROV", licenciaOM.getProvidenciaCodId());
					                r.setText(text, 0);
				              }
			            }
			            }
			         }
			      }
			   }
			}
		/*
			for (XWPFParagraph p : doc.getParagraphs()) {
		     String text = p.getParagraphText(); //here is where you receive text from textbox
		     logger.debug("TExto: "+text);
		    
		}
			*
			*/
		
		FileOutputStream out = new FileOutputStream(outputfilepath);
	    doc.write(out);
	    logger.debug("Generando documento...."+outputfilepath);
	    out.close();
		return relativeOutputfilepath;
  }
}



