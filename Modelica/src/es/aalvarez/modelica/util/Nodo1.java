package es.aalvarez.modelica.util;

import java.io.Serializable;

public class Nodo1 implements Serializable, Comparable<Nodo1> {

	
		private static final long serialVersionUID = 5277473306621486073L;
		private String name;
	    private Integer size;
		private String type;
		private String selectString; //texto utilizado para realizar la consulta;
		 
	    public Nodo1(String name, Integer size, String type, String selectString) {
	        this.name = name;
	        this.size = size;
	        this.type = type;
	        this.selectString = selectString;
	        
	    }
	 
	    public String getName() {
	        return name;
	    }
	 
	    public void setName(String name) {
	        this.name = name;
	    }
	    public Integer getSize() {
			return size;
		}

		public void setSize(Integer size) {
			this.size = size;
		}
	    
	 
	    public String getType() {
	        return type;
	    }
	 
	    public void setType(String type) {
	        this.type = type;
	    }
	
	public String getSelectString() {
			return selectString;
		}

		public void setSelectString(String selectString) {
			this.selectString = selectString;
		}

	@Override
	public int compareTo(Nodo1 o) {
		// TODO Auto-generated method stub
		return this.getSize().compareTo(o.size);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((selectString == null) ? 0 : selectString.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nodo1 other = (Nodo1) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (selectString == null) {
			if (other.selectString != null)
				return false;
		} else if (!selectString.equals(other.selectString))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
