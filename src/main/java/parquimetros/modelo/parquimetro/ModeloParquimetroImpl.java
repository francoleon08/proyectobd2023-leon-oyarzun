package parquimetros.modelo.parquimetro;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parquimetros.modelo.ModeloImpl;
import parquimetros.modelo.beans.*;
import parquimetros.modelo.parquimetro.dto.EntradaEstacionamientoDTOImpl;
import parquimetros.modelo.parquimetro.dto.EstacionamientoDTO;
import parquimetros.modelo.parquimetro.dto.SalidaEstacionamientoDTOImpl;
import parquimetros.modelo.parquimetro.exception.ParquimetroNoExisteException;
import parquimetros.modelo.parquimetro.exception.SinSaldoSuficienteException;
import parquimetros.modelo.parquimetro.exception.TarjetaNoExisteException;
import parquimetros.utils.Config;
import parquimetros.utils.Mensajes;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ModeloParquimetroImpl extends ModeloImpl implements ModeloParquimetro {

	private static Logger logger = LoggerFactory.getLogger(ModeloParquimetroImpl.class);

	private static final String CONEXION_FILE = Config.getProperty("bd.conexion");
	
	@Override
	public ArrayList<TarjetaBean> recuperarTarjetas() throws Exception {
		logger.info(Mensajes.getMessage("ModeloParquimetroImpl.recuperarTarjetas.logger"));

		ArrayList<TarjetaBean> tarjetas = new ArrayList<TarjetaBean>();

		try {
			String query ="SELECT * FROM tipos_tarjeta TT NATURAL JOIN ( SELECT * FROM tarjetas T NATURAL JOIN ( SELECT * FROM automoviles A NATURAL JOIN conductores C ) AC ) TAC;";
			this.conectar("parquimetro", "parq");
			ResultSet rs = this.consulta(query);

			while(rs.next()) {
				ConductorBean conductor = new ConductorBeanImpl();
				conductor.setNroDocumento(rs.getInt("dni"));
				conductor.setRegistro(rs.getInt("registro"));
				conductor.setNombre(rs.getString("nombre"));
				conductor.setApellido(rs.getString("apellido"));
				conductor.setDireccion(rs.getString("direccion"));
				conductor.setTelefono(rs.getString("telefono"));

				AutomovilBean automovil = new AutomovilBeanImpl();
				automovil.setPatente(rs.getString("patente"));
				automovil.setMarca(rs.getString("marca"));
				automovil.setModelo(rs.getString("modelo"));
				automovil.setColor(rs.getString("color"));
				automovil.setConductor(conductor);

				TipoTarjetaBean tipo = new TipoTarjetaBeanImpl();
				tipo.setTipo(rs.getString("tipo"));
				tipo.setDescuento(rs.getDouble("descuento"));

				TarjetaBean insert = new TarjetaBeanImpl();
				insert.setId(rs.getInt("id_tarjeta"));
				insert.setSaldo(rs.getDouble("saldo"));
				insert.setTipoTarjeta(tipo);
				insert.setAutomovil(automovil);

				tarjetas.add(insert);
			}
		}
		catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error en la conexión con la BD.");
		}

		this.desconectar();
	
		return tarjetas;
	}
	
	/*
	 * Atención: Este codigo de recuperarUbicaciones (como el de recuperarParquimetros) es igual en el modeloParquimetro 
	 *           y en modeloInspector. Se podría haber unificado en un DAO compartido. Pero se optó por dejarlo duplicado
	 *           porque tienen diferentes permisos ambos usuarios y quizas uno estaría tentado a seguir agregando metodos
	 *           que van a resultar disponibles para ambos cuando los permisos de la BD no lo permiten.
	 */	
	@Override
	public ArrayList<UbicacionBean> recuperarUbicaciones() throws Exception {
		logger.info(Mensajes.getMessage("ModeloParquimetroImpl.recuperarUbicaciones.logger"));

		ArrayList<UbicacionBean> ubicaciones = new ArrayList<UbicacionBean>();

		try {
			String query ="SELECT * FROM parquimetros.ubicaciones;";
			this.conectar("parquimetro", "parq");
			ResultSet rs = this.consulta(query);

			while(rs.next()) {
				UbicacionBean ubicacion = new UbicacionBeanImpl();
				ubicacion.setCalle(rs.getString("calle"));
				ubicacion.setAltura(rs.getInt("altura"));
				ubicacion.setTarifa(rs.getDouble("tarifa"));

				ubicaciones.add(ubicacion);
			}
		}
		catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error en la conexión con la BD.");
		}

		this.desconectar();
	
		return ubicaciones;
	}

	@Override
	public ArrayList<ParquimetroBean> recuperarParquimetros(UbicacionBean ubicacion) throws Exception {
		logger.info(Mensajes.getMessage("ModeloParquimetroImpl.recuperarParquimetros.logger"));

		ArrayList<ParquimetroBean> parquimetros = new ArrayList<ParquimetroBean>();

		try {
			String query ="SELECT * FROM parquimetros.parquimetros P WHERE P.calle = '"+ubicacion.getCalle()+"' AND P.altura = "+ubicacion.getAltura()+";";
			this.conectar("parquimetro", "parq");
			ResultSet rs = this.consulta(query);

			while(rs.next()) {
				ParquimetroBean parquimetro = new ParquimetroBeanImpl();
				parquimetro.setId(rs.getInt("id_parq"));
				parquimetro.setNumero(rs.getInt("numero"));
				parquimetro.setUbicacion(ubicacion);

				parquimetros.add(parquimetro);
			}
		}
		catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error en la conexión con la BD.");
		}

		this.desconectar();

		return parquimetros;
	}

	@Override
	public EstacionamientoDTO conectarParquimetro(ParquimetroBean parquimetro, TarjetaBean tarjeta)
			throws SinSaldoSuficienteException, ParquimetroNoExisteException, TarjetaNoExisteException, Exception {

		logger.info(Mensajes.getMessage("ModeloParquimetroImpl.conectarParquimetro.logger"),parquimetro.getId(),tarjeta.getId());
		
		/**
		 * TODO Invoca al stored procedure conectar(...) que se encarga de realizar en una transacción la apertura o cierre 
		 *      de estacionamiento segun corresponda.
		 *      
		 *      Segun la infromacion devuelta por el stored procedure se retorna un objeto EstacionamientoDTO o
		 *      dependiendo del error se produce la excepción correspondiente:
		 *       SinSaldoSuficienteException, ParquimetroNoExisteException, TarjetaNoExisteException     
		 *  
		 */
		
		//Datos estáticos de prueba. Quitar y reemplazar por código que recupera los datos reales.
		if ((tarjeta.getSaldo() < 0) && (tarjeta.getTipoTarjeta().getDescuento() < 1)) {  // tarjeta k1
			throw new SinSaldoSuficienteException();
		}
		EstacionamientoDTO estacionamiento;

		LocalDateTime currentDateTime = LocalDateTime.now();
        // Definir formatos para la fecha y la hora
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Formatear la fecha y la hora como cadenas separadas
        String fechaAhora = currentDateTime.format(dateFormatter);
        String horaAhora = currentDateTime.format(timeFormatter);
		
		if (tarjeta.getId() == 2) { 		//EntradaEstacionamientoDTO(String tiempoDisponible, String fechaEntrada, String horaEntrada)			
			estacionamiento = new EntradaEstacionamientoDTOImpl("01:40:00",
																fechaAhora,
																horaAhora);
		} else if (tarjeta.getId() == 3) {  		//SalidaEstacionamientoDTO(String tiempoTranscurrido, String saldoTarjeta, String fechaEntrada,	String horaEntrada, String fechaSalida, String horaSalida)
			
			LocalDateTime antes = currentDateTime.minusMinutes(45); // hora actual menos 45 minutos
			
			estacionamiento = new SalidaEstacionamientoDTOImpl("00:45:00", // tiempoTranscurrido
																"10.20", // saldoTarjeta
																fechaAhora, // fechaEntrada
																antes.format(timeFormatter), // horaEntrada
																fechaAhora, // fechaSalida
																horaAhora); // horaSalida
		} else if (tarjeta.getId() == 4) { 

			LocalDateTime antes = currentDateTime.minusMinutes(90); // hora actual menos 45 minutos
			
			estacionamiento = new SalidaEstacionamientoDTOImpl("01:30:00", // tiempoTranscurrido
																"-85", // saldoTarjeta
																fechaAhora, // fechaEntrada
																antes.format(timeFormatter), // horaEntrada
																fechaAhora, // fechaSalida
																horaAhora); // horaSalida
			
		} else {
			throw new Exception();
		}
	
		return estacionamiento;
	}

}
