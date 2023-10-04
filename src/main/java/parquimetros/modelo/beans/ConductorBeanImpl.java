package parquimetros.modelo.beans;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//CLASE IMPLEMENTADA PROVISTA POR LA CATEDRA
//Representa como un objeto una tupla de la tabla "pasajeros"
@Getter @Setter
public class ConductorBeanImpl implements ConductorBean  {

	private static Logger logger = LoggerFactory.getLogger(ConductorBeanImpl.class);
	private static final long serialVersionUID = 1L;

	private int nroDocumento;
	private int registro;
	private String apellido;
	private String nombre;
	private String direccion;	
	private String telefono;
	
	@Override
	public String toString() {
		return "PasajeroBeanImpl [nroDocumento=" + nroDocumento + ", apellido=" + apellido + ", "
				+ "nombre=" + nombre + ", direccion=" + direccion + ", telefono=" + telefono
				+ ", registro=" + registro + "]";
	}
}
