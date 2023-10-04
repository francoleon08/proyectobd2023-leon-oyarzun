package parquimetros.modelo.beans;

import lombok.Getter;
import lombok.Setter;

//CLASE IMPLEMENTADA PROVISTA POR LA CATEDRA
//
@Getter @Setter
public class AutomovilBeanImpl implements AutomovilBean{
	
	private ConductorBean conductor;
	private String patente;
	private String marca;
	private String modelo;
	private String color;
}
