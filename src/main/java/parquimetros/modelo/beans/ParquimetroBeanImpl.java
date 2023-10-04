package parquimetros.modelo.beans;

import lombok.Getter;
import lombok.Setter;

//CLASE IMPLEMENTADA PROVISTA POR LA CATEDRA
//Representa como un objeto una tupla de la tabla "parquimetros"
@Getter @Setter
public class ParquimetroBeanImpl implements ParquimetroBean {
	private static final long serialVersionUID = 1L;

	private int id;
	private int numero;
	private UbicacionBean ubicacion;

	@Override
	public String toString() {
		return Integer.toString(getNumero());
	}
}
