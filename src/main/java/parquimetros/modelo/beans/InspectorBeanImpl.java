package parquimetros.modelo.beans;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//CLASE IMPLEMENTADA PROVISTA POR LA CATEDRA
//Representa como un objeto una tupla de la tabla "inspectores"
@Getter @Setter
public class InspectorBeanImpl implements InspectorBean {

	private static Logger logger = LoggerFactory.getLogger(InspectorBeanImpl.class);
	private static final long serialVersionUID = 1L;

	private int legajo;
	private int dni;
	private String apellido;
	private String nombre;
	private String password;

	@Override
	public int getDNI() {
		return this.dni;
	}

	@Override
	public void setDNI(int dni) {
		this.dni = dni;
	}
}
