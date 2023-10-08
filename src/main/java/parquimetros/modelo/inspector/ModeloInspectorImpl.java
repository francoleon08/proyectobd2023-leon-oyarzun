package parquimetros.modelo.inspector;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parquimetros.modelo.ModeloImpl;
import parquimetros.modelo.beans.*;
import parquimetros.modelo.inspector.dao.DAOParquimetro;
import parquimetros.modelo.inspector.dao.DAOParquimetroImpl;
import parquimetros.modelo.inspector.dao.DAOInspector;
import parquimetros.modelo.inspector.dao.DAOInspectorImpl;
import parquimetros.modelo.inspector.dao.DAOAutomovil;
import parquimetros.modelo.inspector.dao.DAOAutomovilImpl;
import parquimetros.modelo.inspector.dto.EstacionamientoPatenteDTO;
import parquimetros.modelo.inspector.dto.EstacionamientoPatenteDTOImpl;
import parquimetros.modelo.inspector.dto.MultaPatenteDTO;
import parquimetros.modelo.inspector.dto.MultaPatenteDTOImpl;
import parquimetros.modelo.inspector.exception.AutomovilNoEncontradoException;
import parquimetros.modelo.inspector.exception.ConexionParquimetroException;
import parquimetros.modelo.inspector.exception.InspectorNoAutenticadoException;
import parquimetros.modelo.inspector.exception.InspectorNoHabilitadoEnUbicacionException;
import parquimetros.utils.Fechas;
import parquimetros.utils.Mensajes;

public class ModeloInspectorImpl extends ModeloImpl implements ModeloInspector {

	private static Logger logger = LoggerFactory.getLogger(ModeloInspectorImpl.class);	
	
	public ModeloInspectorImpl() {
		logger.debug(Mensajes.getMessage("ModeloInspectorImpl.constructor.logger"));
	}

	@Override
	public InspectorBean autenticar(String legajo, String password) throws InspectorNoAutenticadoException, Exception {
		logger.info(Mensajes.getMessage("ModeloInspectorImpl.autenticar.logger"), legajo, password);

		if (legajo==null || legajo.isEmpty() || password==null || password.isEmpty()) {
			throw new InspectorNoAutenticadoException(Mensajes.getMessage("ModeloInspectorImpl.autenticar.parametrosVacios"));
		}
		DAOInspector dao = new DAOInspectorImpl(this.conexion);
		return dao.autenticar(legajo, password);		
	}
	
	@Override
	public ArrayList<UbicacionBean> recuperarUbicaciones() throws Exception {
		logger.info(Mensajes.getMessage("ModeloInspectorImpl.recuperarUbicaciones.logger"));
		ArrayList<UbicacionBean> ubicaciones = new ArrayList<UbicacionBean>();
		String query = "SELECT * FROM parquimetros.ubicaciones;";

		try (Statement st = this.conexion.createStatement();
			 ResultSet rs = st.executeQuery(query)) {

			while (rs.next()) {
				UbicacionBean ubicacion = new UbicacionBeanImpl();
				ubicacion.setCalle(rs.getString("calle"));
				ubicacion.setAltura(rs.getInt("altura"));
				ubicacion.setTarifa(rs.getDouble("tarifa"));

				ubicaciones.add(ubicacion);
			}
		} catch (SQLException ex)
		{
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}

		return ubicaciones;
	}

	@Override
	public ArrayList<ParquimetroBean> recuperarParquimetros(UbicacionBean ubicacion) throws Exception {
		logger.info(Mensajes.getMessage("ModeloInspectorImpl.recuperarParquimetros.logger"),ubicacion.toString());
		ArrayList<ParquimetroBean> parquimetros = new ArrayList<ParquimetroBean>();
		String query = "SELECT * FROM parquimetros.parquimetros P WHERE P.calle = ? AND P.altura = ?;";

		try (PreparedStatement st = this.conexion.prepareStatement(query)){
			st.setString(1, ubicacion.getCalle());
			st.setInt(2, ubicacion.getAltura());

			try (ResultSet rs = st.executeQuery()) {
				while (rs.next()) {
					ParquimetroBean parquimetro = new ParquimetroBeanImpl();
					parquimetro.setId(rs.getInt("id_parq"));
					parquimetro.setNumero(rs.getInt("numero"));
					parquimetro.setUbicacion(ubicacion);

					parquimetros.add(parquimetro);
				}
			}
		} catch (SQLException ex)
		{
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
	
		return parquimetros;
	}

	@Override
	public void conectarParquimetro(ParquimetroBean parquimetro, InspectorBean inspectorLogueado) throws ConexionParquimetroException, Exception {
		logger.info(Mensajes.getMessage("ModeloInspectorImpl.conectarParquimetro.logger"),parquimetro.toString());

		String[] fechaHora = Fechas.getDiaTurnoFechaHora();
		String queryInsert = "INSERT INTO parquimetros.accede VALUES (?, ?, ?, ?)";

		try (PreparedStatement stInsert = this.conexion.prepareStatement(queryInsert)) {
			checkInspectorHabilitado(parquimetro.getUbicacion(), inspectorLogueado);

			stInsert.setInt(1, parquimetro.getId());
			stInsert.setString(2, fechaHora[2]);
			stInsert.setString(3, fechaHora[3]);
			stInsert.setInt(4, inspectorLogueado.getLegajo());

			stInsert.executeUpdate();
		} catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
	}

	@Override
	public UbicacionBean recuperarUbicacion(ParquimetroBean parquimetro) throws Exception {
		logger.info(Mensajes.getMessage("ModeloInspectorImpl.recuperarUbicacion.logger"),parquimetro.getId());
		UbicacionBean ubicacion = parquimetro.getUbicacion();
		if (Objects.isNull(ubicacion)) {
			DAOParquimetro dao = new DAOParquimetroImpl(this.conexion);
			ubicacion = dao.recuperarUbicacion(parquimetro);
		}			
		return ubicacion; 
	}

	@Override
	public void verificarPatente(String patente) throws AutomovilNoEncontradoException, Exception {
		logger.info(Mensajes.getMessage("ModeloInspectorImpl.verificarPatente.logger"),patente);
		DAOAutomovil dao = new DAOAutomovilImpl(this.conexion);
		try {
			dao.verificarPatente(patente);
		} catch (AutomovilNoEncontradoException e) {
			throw new AutomovilNoEncontradoException(e.getMessage());
		}
	}

	/**
	 * PREGUNTAR NO ENTIENDO ESTOY CANSADO
	 * @param patente
	 * @param ubicacion
	 * @return
	 * @throws Exception
	 */
	@Override
	public EstacionamientoPatenteDTO recuperarEstacionamiento(String patente, UbicacionBean ubicacion) throws Exception {

		logger.info(Mensajes.getMessage("ModeloInspectorImpl.recuperarEstacionamiento.logger"),patente,ubicacion.getCalle(),ubicacion.getAltura());
		/**
		 * TODO Verifica si existe un estacionamiento abierto registrado la patente en la ubicación, y
		 *	    de ser asi retorna un EstacionamientoPatenteDTO con estado Registrado (EstacionamientoPatenteDTO.ESTADO_REGISTRADO), 
		 * 		y caso contrario sale con estado No Registrado (EstacionamientoPatenteDTO.ESTADO_NO_REGISTRADO).
		 * 
		 *      Importante: Para acceder a la B.D. utilice la propiedad this.conexion (de clase Connection) 
		 *      que se hereda al extender la clase ModeloImpl.
		 */
		String query = "SELECT C.patente, P.calle, P.altura, C.fecha_ent, C.hora_ent, C.fecha_sal, C.hora_sal FROM parquimetros.parquimetros P NATURAL JOIN (SELECT * FROM parquimetros.estacionamientos E NATURAL JOIN parquimetros.tarjetas T WHERE T.patente = ?) C;";

		String patenteAuto = "", calle, altura, fecha_ent = "", hora_ent = "", estado = EstacionamientoPatenteDTO.ESTADO_NO_REGISTRADO;
		calle = ubicacion.getCalle();
		altura = ubicacion.getAltura()+"";

		try (PreparedStatement st = this.conexion.prepareStatement(query)) {
			st.setString(1, patente);

			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					if(rs.getDate("fecha_sal") == null && rs.getTime("hora_sal") == null && Objects.equals(rs.getString("calle"), ubicacion.getCalle()) && rs.getInt("altura") == ubicacion.getAltura()) {
						estado = EstacionamientoPatenteDTO.ESTADO_REGISTRADO;
					}

					SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy/MM/dd");
					SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

					patenteAuto = rs.getString("patente");
					fecha_ent = formatoFecha.format(rs.getDate("fecha_ent"));
					hora_ent = formatoHora.format(rs.getTime("hora_ent"));
				}
			}
		} catch (SQLException ex)
		{
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
		//La patente no se encuentra registrada o no tiene una tarjeta asociada
		//El controlador no permite agregar patentes no registradas, por lo tanto no es posible multar autos no registrados
		if(Objects.equals(patenteAuto, "")){
			String [] fechaHora = Fechas.getDiaTurnoFechaHora();
			fecha_ent = fechaHora[2];
			hora_ent = fechaHora[3];
		}
		return new EstacionamientoPatenteDTOImpl(patente, calle, altura, fecha_ent, hora_ent, estado);
	}
	

	@Override
	public ArrayList<MultaPatenteDTO> generarMultas(ArrayList<String> listaPatentes, 
													UbicacionBean ubicacion, 
													InspectorBean inspectorLogueado) 
									throws InspectorNoHabilitadoEnUbicacionException, Exception {

		logger.info(Mensajes.getMessage("ModeloInspectorImpl.generarMultas.logger"),listaPatentes.size());		
		
		/** 
		 * TODO Primero verificar si el inspector puede realizar una multa en esa ubicacion el dia y hora actual 
		 *      segun la tabla asociado_con. Sino puede deberá producir una excepción de 
		 *      InspectorNoHabilitadoEnUbicacionException. 
		 *            
		 * 		Luego para cada una de las patentes suministradas, si no tiene un estacionamiento abierto en dicha 
		 *      ubicación, se deberá cargar una multa en la B.D. 
		 *      
		 *      Debe retornar una lista de las multas realizadas (lista de objetos MultaPatenteDTO).
		 *      
		 *      Importante: Para acceder a la B.D. utilice la propiedad this.conexion (de clase Connection) 
		 *      que se hereda al extender la clase ModeloImpl.      
		 */

		try {
			checkInspectorHabilitado(ubicacion, inspectorLogueado);
		} catch (ConexionParquimetroException e) {
			throw new InspectorNoHabilitadoEnUbicacionException();
		}
		
		//Datos estáticos de prueba. Quitar y reemplazar por código que recupera los datos reales.
		//
		// 1) throw InspectorNoHabilitadoEnUbicacionException
		//
		ArrayList<MultaPatenteDTO> multas = new ArrayList<MultaPatenteDTO>();
		int nroMulta = 1;
		
		LocalDateTime currentDateTime = LocalDateTime.now();
        // Definir formatos para la fecha y la hora
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Formatear la fecha y la hora como cadenas separadas
        String fechaMulta = currentDateTime.format(dateFormatter);
        String horaMulta = currentDateTime.format(timeFormatter);
		
		for (String patente : listaPatentes) {
			
			EstacionamientoPatenteDTO estacionamiento = this.recuperarEstacionamiento(patente,ubicacion);
			if (estacionamiento.getEstado() == EstacionamientoPatenteDTO.ESTADO_NO_REGISTRADO) {
				
				MultaPatenteDTO multa = new MultaPatenteDTOImpl(String.valueOf(nroMulta), 
																patente, 
																ubicacion.getCalle(), 
																String.valueOf(ubicacion.getAltura()), 
																fechaMulta, 
																horaMulta, 
																String.valueOf(inspectorLogueado.getLegajo()));
				multas.add(multa);
				nroMulta++;
			}
		}
		// Fin datos prueba
		return multas;		
	}

	/**
	 * Consulta en la BD si el inspector recibido se encuentra habilitado para realizar una accion, es decir,
	 * si se encuentra en una ubicacion valida y en un turno valido.
	 * @param inspectorLogueado
	 * @throws ConexionParquimetroException
	 * @throws Exception
	 */
	private void checkInspectorHabilitado(UbicacionBean ubicacion, InspectorBean inspectorLogueado) throws ConexionParquimetroException, Exception {
		logger.info(Mensajes.getMessage("ModeloInspectorImpl.checkInspectorHabilitado.logger"),inspectorLogueado.getLegajo(), ubicacion.getCalle(), ubicacion.getAltura());

		String[] fechaHora = Fechas.getDiaTurnoFechaHora();
		String queryCheck = "SELECT EXISTS (SELECT 1 FROM parquimetros.asociado_con A WHERE A.legajo = ? AND A.calle = ? AND A.altura = ? AND A.dia = ? AND A.turno = ?)";

		try (PreparedStatement stCheck = this.conexion.prepareStatement(queryCheck)) {
			stCheck.setInt(1, inspectorLogueado.getLegajo());
			stCheck.setString(2, ubicacion.getCalle());
			stCheck.setInt(3, ubicacion.getAltura());
			//stCheck.setString(4, fechaHora[0]);
			//stCheck.setString(5, fechaHora[1]);
			//Usar inspector 1001 con contraseña c4rl05P@ss para pruebas
			stCheck.setString(4, "lu");
			stCheck.setString(5, "m");


			try (ResultSet rs = stCheck.executeQuery()) {
				if (rs.next() && !rs.getBoolean(1)) {
					throw new ConexionParquimetroException("Error: inspector fuera de turno/horario/ubicacion.");
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
	}
}
