package parquimetros.modelo.parquimetro;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parquimetros.modelo.ModeloImpl;
import parquimetros.modelo.beans.*;
import parquimetros.modelo.parquimetro.dto.EstacionamientoDTO;
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
			ResultSet rs = realizarConsulta("parquimetro", "parq", query);

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
			ResultSet rs = realizarConsulta("parquimetro", "parq", query);

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
			ResultSet rs = realizarConsulta("parquimetro", "parq", query);

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

		checkParquimetroTarjeta(parquimetro, tarjeta);

		String queryEstacionados = "SELECT * FROM parquimetros.estacionamientos E WHERE E.id_tarjeta = "+tarjeta.getId()+" AND E.fecha_sal IS NULL AND E.hora_sal IS NULL;";
		boolean estacionado = false;

		try {
			ResultSet rs = realizarConsulta("parquimetro", "parq", queryEstacionados);
			while (rs.next()) {
				estacionado = true;
				break;
			}
		} catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error en la conexión con la BD.");
		}

		if(estacionado) {
			return cerrarEstacionamiento(parquimetro, tarjeta);
		} else {
			return abrirEstacionamiento(parquimetro, tarjeta);
		}
	}

	/**
	 * Arreglo de dos Strings donde [0] es la fecha actual y [1] es la hora actual.
	 * @return String []
	 */
	private String [] getFechaHoraActual() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		// Definir formatos para la fecha y la hora
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

		// Formatear la fecha y la hora como cadenas separadas
		String fechaAhora = currentDateTime.format(dateFormatter);
		String horaAhora = currentDateTime.format(timeFormatter);

        return new String[]{fechaAhora, horaAhora};
	}

	private EstacionamientoDTO cerrarEstacionamiento(ParquimetroBean parquimetro, TarjetaBean tarjeta) {
		logger.info("Se realizara el cierre de un estacionamiento");
		String [] fechaHora = getFechaHoraActual();
		String tiempoTranscurrido;

		/*
			IMPLEMENTAR
		 */
		return null;
	}

	private EstacionamientoDTO abrirEstacionamiento(ParquimetroBean parquimetro, TarjetaBean tarjeta) throws SinSaldoSuficienteException{
		logger.info("Se intentara realizar la apertura de un estacionamiento");
		if(tarjeta.getSaldo() <= 0) {
			logger.info("La tarjeta "+tarjeta.getId()+" no tiene saldo suficiente ["+tarjeta.getSaldo()+"].");
			throw new SinSaldoSuficienteException();
		}

		/*
			IMPLEMENTAR
		 */
		return null;
	}

	/**
	 * Consulta en la base de datos si la tarejeta y el parquimetro existen. En caso de no existir alguna
	 * lanza su excepcion correspondiente.
	 *
	 * @param parquimetro
	 * @param tarjeta
	 * @throws ParquimetroNoExisteException
	 * @throws TarjetaNoExisteException
	 * @throws Exception
	 */
	private void checkParquimetroTarjeta(ParquimetroBean parquimetro, TarjetaBean tarjeta) throws ParquimetroNoExisteException, TarjetaNoExisteException, Exception {
		String queryParq = "SELECT * FROM parquimetros.parquimetros P WHERE P.id_parq = "+parquimetro.getId()+";";
		String queryTarjeta = "SELECT * FROM parquimetros.tarjetas T WHERE T.id_tarjeta = "+tarjeta.getId()+";";

		boolean estado = false;

		try {
			ResultSet rs1 = realizarConsulta("parquimetro", "parq", queryParq);
			ResultSet rs2 = realizarConsulta("parquimetro", "parq", queryTarjeta);

			while (rs1.next()){
				estado = true;
				break;
			}
			if(!estado) {
				throw new ParquimetroNoExisteException();
			}
			estado = false;
			while (rs2.next()){
				estado = true;
				break;
			}
			if(!estado) {
				throw new TarjetaNoExisteException();
			}
		} catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error en la conexión con la BD.");
		}
	}

	/**
	 * Realiza una consulta con un username y password a una B.D.
	 * @param username usuario de la base de datos
	 * @param password password del usuario
	 * @param query consulta a realizar
	 * @return ResultSet : retorna el resultado de la consulta.
	 */
	private ResultSet realizarConsulta(String username, String password, String query) {
		logger.info("Se intenta iniciar una consulta.");
		this.conectar(username, password);
		return this.consulta(query);
	}
}
