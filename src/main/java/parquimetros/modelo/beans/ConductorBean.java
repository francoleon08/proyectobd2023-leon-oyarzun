package parquimetros.modelo.beans;

import java.io.Serializable;

public interface ConductorBean extends Serializable {

	public int getNroDocumento();

	public void setNroDocumento(int nroDocumento);
	
	public int getRegistro();

	public void setRegistro(int registro);

	public String getApellido();
	
	public void setApellido(String apellido);
	
	public String getNombre();
	
	public void setNombre(String nombre);
	
	public String getDireccion();
	
	public void setDireccion(String direccion);
	
	public String getTelefono();
	
	public void setTelefono(String telefono);
	
	
}
