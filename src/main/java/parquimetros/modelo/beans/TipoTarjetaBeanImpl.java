package parquimetros.modelo.beans;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter @Setter
public class TipoTarjetaBeanImpl implements TipoTarjetaBean {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(TipoTarjetaBeanImpl.class);

	private String tipo;
	private double descuento;
}
