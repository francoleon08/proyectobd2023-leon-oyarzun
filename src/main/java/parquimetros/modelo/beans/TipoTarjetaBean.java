package parquimetros.modelo.beans;

import java.io.Serializable;

public interface TipoTarjetaBean extends Serializable {

	public String getTipo();

	public void setTipo(String tipo);
	
	public double getDescuento();
	
	public void setDescuento(double descuento);
	
}
