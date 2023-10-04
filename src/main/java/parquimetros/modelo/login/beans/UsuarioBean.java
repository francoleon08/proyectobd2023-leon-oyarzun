package parquimetros.modelo.login.beans;

import java.io.Serializable;

public interface UsuarioBean extends Serializable {
	
	public String getUsername();
	public String getPassword();
	public String getDisplayname();
	public void setUsername(String username);
	public void setPassword(String password);
	public void setDisplayname(String displayname);
	
	/**
	 * Determina si la clave pasada como parametro es igual al password del usuario
	 * 
	 * @param clave
	 * @return
	 */
	/* Sacar? Creo que no se usa
	public boolean passwordCoincide(char[] clave);	
	*/
}
