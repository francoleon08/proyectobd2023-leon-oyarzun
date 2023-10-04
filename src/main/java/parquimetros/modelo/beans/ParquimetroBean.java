package parquimetros.modelo.beans;

import java.io.Serializable;

public interface ParquimetroBean extends Serializable {

	int getId();

	void setId(int id);

	int getNumero();

	void setNumero(int numero);
	
	UbicacionBean getUbicacion();

	void setUbicacion(UbicacionBean ubicacion);


}