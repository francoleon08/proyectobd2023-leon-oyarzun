package parquimetros.modelo.inspector;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
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

		try {
			String query = "SELECT * FROM parquimetros.ubicaciones;";
			Statement st = this.conexion.createStatement();
			ResultSet rs = st.executeQuery(query);

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

		try {
			String query = "SELECT * FROM parquimetros.parquimetros P WHERE P.calle = '"+ubicacion.getCalle()+"' AND P.altura = "+ubicacion.getAltura()+";";
			Statement st = this.conexion.createStatement();
			ResultSet rs = st.executeQuery(query);

			while (rs.next()) {
				ParquimetroBean parquimetro = new ParquimetroBeanImpl();
				parquimetro.setId(rs.getInt("id_parq"));
				parquimetro.setNumero(rs.getInt("numero"));
				parquimetro.setUbicacion(ubicacion);

				parquimetros.add(parquimetro);
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

		String [] fechaHora = getDiaTurnoFechaHora();
		String query = String.format("SELECT EXISTS (SELECT * FROM parquimetros.asociado_con A WHERE A.legajo = %d AND A.calle = '%s' AND A.altura = %d AND A.dia = '%s' AND A.turno = '%s');", inspectorLogueado.getLegajo(), parquimetro.getUbicacion().getCalle(), parquimetro.getUbicacion().getAltura(), fechaHora[0], fechaHora[1]);

		try {
			Statement st = this.conexion.createStatement();
			ResultSet rs = st.executeQuery(query);

			while (rs.next()) {
				if(!rs.getBoolean(1)){
					throw new ConexionParquimetroException("Error: inspector fuera de turno/horario/ubicacion.");
				}
				else {
					query = String.format("INSERT INTO parquimetros.accede VALUES (%d, '%s', '%s', %d);", parquimetro.getId(), fechaHora[2], fechaHora[3], inspectorLogueado.getLegajo());
					this.conexion.createStatement().execute(query);
				}
			}
		} catch (SQLException ex)
		{
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
	}

	/**
	 * Arreglo Strings donde [0] corresponde al dia de la semana, [1] al turno,
	 * [2] a la fecha actual y [3] a la hora actual.
	 * @return String []
	 */
	private String [] getDiaTurnoFechaHora() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		int hora = Integer.parseInt(currentDateTime.format(DateTimeFormatter.ofPattern("HH")));
		Calendar calendario = Calendar.getInstance();
		int dia = calendario.get(Calendar.DAY_OF_WEEK);
		String fecha = "";
		String horaRet = "";

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

		String fechaAhora = currentDateTime.format(dateFormatter);
		String horaAhora = currentDateTime.format(timeFormatter);

		if(hora >= 8 && hora <14){
			horaRet = "m";
		} else {
			if(hora >= 14 && hora <20){
				horaRet = "t";
			}
		}

		switch (dia) {
			case 1: {
				fecha = "do";
				break;
			}
			case 2: {
				fecha = "lu";
				break;
			}
			case 3: {
				fecha = "ma";
				break;
			}
			case 4: {
				fecha = "mi";
				break;
			}
			case 5: {
				fecha = "ju";
				break;
			}
			case 6: {
				fecha = "vi";
				break;
			}
			case 7: {
				fecha = "sa";
				break;
			}
		}
		return new String[]{fecha, horaRet, fechaAhora, horaAhora};
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
		dao.verificarPatente(patente); 
	}	
	
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
		//
		// Datos estáticos de prueba. Quitar y reemplazar por código que recupera los datos reale de la BD.
		//
		// Diseño de datos de prueba: Las patentes que terminan en 1 al 8 fueron verificados como existentes en la tabla automovil,
		//                            las terminadas en 9 y 0 produjeron una excepción de AutomovilNoEncontradoException y Exception.
		//                            entonces solo consideramos los casos terminados de 1 a 8
 		// 
		// Utilizaremos el criterio que si es par el último digito de patente entonces está registrado correctamente el estacionamiento.
		//
		String fechaEntrada, horaEntrada, estado;
		
		if (Integer.parseInt(patente.substring(patente.length()-1)) % 2 == 0) {
			estado = EstacionamientoPatenteDTO.ESTADO_REGISTRADO;

			LocalDateTime currentDateTime = LocalDateTime.now();
	        // Definir formatos para la fecha y la hora
	        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	        // Formatear la fecha y la hora como cadenas separadas
	        fechaEntrada = currentDateTime.format(dateFormatter);
	        horaEntrada = currentDateTime.format(timeFormatter);
			
		} else {
			estado = EstacionamientoPatenteDTO.ESTADO_NO_REGISTRADO;
	        fechaEntrada = "";
	        horaEntrada = "";
		}

		return new EstacionamientoPatenteDTOImpl(patente, ubicacion.getCalle(), String.valueOf(ubicacion.getAltura()), fechaEntrada, horaEntrada, estado);
		// Fin de datos de prueba
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
}
