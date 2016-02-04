package es.aalvarez.modelica.util;

public class LabelClass {
	
	    private String lbl;
	    private String value;

	    public LabelClass(String lbl, String value) {
	        super();
	        this.lbl = lbl;
	        this.value = value;
	    }

	    public String getLbl() {
	        return lbl;
	    }

	    public void setLbl(String lbl) {
	        this.lbl = lbl;
	    }

	    public String getValue() {
	        return value;
	    }

	    public void setValue(String value) {
	        this.value = value;
	    }
}
