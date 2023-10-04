package parquimetros.modelo.beans;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Getter @Setter
public class TarjetaBeanImpl implements TarjetaBean {
	
	private static Logger logger = LoggerFactory.getLogger(TarjetaBeanImpl.class);
	private static final long serialVersionUID = 1L;

	private int id;
	private double saldo;
	private TipoTarjetaBean tipoTarjeta;
	private AutomovilBean automovil;

	@Override
	public String toString() {
		String tarjeta = String.valueOf(id);
		if (this.tipoTarjeta != null) {
			tarjeta += " [" + this.tipoTarjeta.getTipo() + "]";
		}
		if (this.automovil != null) {
			tarjeta += " " + this.automovil.getPatente();
			if (this.automovil.getConductor() != null) {
				tarjeta += " de " + this.automovil.getConductor().getApellido() + 
							", " + this.automovil.getConductor().getNombre();
			}
		}		
		return tarjeta;
	}
}
