package parquimetros.modelo.beans;

import java.io.Serializable;

public interface AutomovilBean extends Serializable {

	ConductorBean getConductor();

	void setConductor(ConductorBean conductor);

	String getPatente();
	
	void setPatente(String patente);
	
	String getMarca();
	
	void setMarca(String marca);

	String getModelo();
	
	void setModelo(String modelo);

	String getColor();
	
	void setColor(String color);

}
