package parquimetros.modelo.beans;

import java.io.Serializable;

public interface TarjetaBean extends Serializable {

	public int getId();

	public void setId(int id);
	
	public double getSaldo();
	
	public void setSaldo(double saldo);
	
	public TipoTarjetaBean getTipoTarjeta();
	
	public void setTipoTarjeta(TipoTarjetaBean tipo);
	
	public AutomovilBean getAutomovil();
	
	public void setAutomovil(AutomovilBean automovil);
	
}
