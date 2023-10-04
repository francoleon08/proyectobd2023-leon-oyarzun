package parquimetros.modelo.login.beans;

import lombok.Getter;
import lombok.Setter;

//CLASE IMPLEMENTADA PROVISTA POR LA CATEDRA
// Representa un usuario del servidor de bases de datos (MySQL) a través del cual se realizaran las conexiones 
@Getter @Setter
public class UsuarioBeanImpl implements UsuarioBean {
	
	private static final long serialVersionUID = 1L;	
	
	private String username;
	private String displayname;
	private String password;
	
	/* Sacar? Creo que no se usa
	public boolean passwordCoincide(char[] clave) {
		
		boolean esCorrecto = false; 
		
		char[] passCorrecto = this.getPassword().toCharArray(); 

		if (clave.length == passCorrecto.length) {
			esCorrecto = Arrays.equals (clave, passCorrecto);
		}			

		Arrays.fill(passCorrecto,'0');
		
		return esCorrecto;
	}
	*/
}

