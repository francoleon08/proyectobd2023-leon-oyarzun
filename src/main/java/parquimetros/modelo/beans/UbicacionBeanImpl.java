package parquimetros.modelo.beans;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//CLASE IMPLEMENTADA PROVISTA POR LA CATEDRA
//Representa como un objeto una tupla de la tabla "ubicaciones"
@Getter @Setter
public class UbicacionBeanImpl implements UbicacionBean {
	
	private static Logger logger = LoggerFactory.getLogger(UbicacionBeanImpl.class);
	private static final long serialVersionUID = 1L;

	private String calle;
	private int altura;
	private double tarifa;

	@Override
	public String toString() {
		return this.calle + " Nº " +  String.valueOf(this.altura);		
	}
	
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UbicacionBeanImpl) {
        	UbicacionBeanImpl ub = (UbicacionBeanImpl) obj;
            if (ub.getCalle().equals(this.getCalle()) && 
            	(ub.getAltura() == this.getAltura()) &&
            	(ub.getTarifa() == this.getTarifa()) 
            ){
                return true;
            }
        }
        return false;       
    }	
}
