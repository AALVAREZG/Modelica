package es.aalvarez.modelica.managedbeans;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;

@ManagedBean
public class LoginMB implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8879702388920024303L;
	private String password;
	
	public LoginMB(){
		
	}
	
	public void auth(){
		
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
        boolean loggedIn = false;
         
        if(this.password != null && this.password.equals("tt")) {
        	 System.out.println("Loged In");
            loggedIn = true;
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome" , "admin" );
            
        } else {
        	System.out.println("Login Error");
            loggedIn = false;
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
        }
         
        FacesContext.getCurrentInstance().addMessage(null, message);
        context.addCallbackParam("loggedIn", loggedIn);
   
}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
