package parquimetros.modelo.parquimetro;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
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
import parquimetros.utils.Mensajes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ModeloParquimetroImpl extends ModeloImpl implements ModeloParquimetro {

	private static Logger logger = LoggerFactory.getLogger(ModeloParquimetroImpl.class);
	
	@Override
	public ArrayList<TarjetaBean> recuperarTarjetas() throws Exception {
		logger.info(Mensajes.getMessage("ModeloParquimetroImpl.recuperarTarjetas.logger"));

		ArrayList<TarjetaBean> tarjetas = new ArrayList<TarjetaBean>();
		String query ="SELECT * FROM tipos_tarjeta TT NATURAL JOIN ( SELECT * FROM tarjetas T NATURAL JOIN ( SELECT * FROM automoviles A NATURAL JOIN conductores C ) AC ) TAC;";

		try (Statement st = this.conexion.createStatement();
			 ResultSet rs = st.executeQuery(query)) {

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
		String query ="SELECT * FROM parquimetros.ubicaciones;";

		try (Statement st = this.conexion.createStatement();
			 ResultSet rs = st.executeQuery(query)){

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
	
		return ubicaciones;
	}

	@Override
	public ArrayList<ParquimetroBean> recuperarParquimetros(UbicacionBean ubicacion) throws Exception {
		logger.info(Mensajes.getMessage("ModeloParquimetroImpl.recuperarParquimetros.logger"));

		ArrayList<ParquimetroBean> parquimetros = new ArrayList<ParquimetroBean>();
		String query ="SELECT P.id_parq, P.numero FROM parquimetros.parquimetros P WHERE P.calle = ? AND P.altura = ?;";

		try (PreparedStatement st = this.conexion.prepareStatement(query)) {
			st.setString(1, ubicacion.getCalle());
			st.setInt(2, ubicacion.getAltura());

			try (ResultSet rs = st.executeQuery()) {
				while(rs.next()) {
					ParquimetroBean parquimetro = new ParquimetroBeanImpl();
					parquimetro.setId(rs.getInt("id_parq"));
					parquimetro.setNumero(rs.getInt("numero"));
					parquimetro.setUbicacion(ubicacion);

					parquimetros.add(parquimetro);
				}
			}
		}
		catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error en la conexión con la BD.");
		}

		return parquimetros;
	}

	@Override
	public EstacionamientoDTO conectarParquimetro(ParquimetroBean parquimetro, TarjetaBean tarjeta)
			throws SinSaldoSuficienteException, ParquimetroNoExisteException, TarjetaNoExisteException, Exception {
		logger.info(Mensajes.getMessage("ModeloParquimetroImpl.conectarParquimetro.logger"),parquimetro.getId(),tarjeta.getId());

		String query ="call conectar(?,?)";

		try (PreparedStatement st = this.conexion.prepareStatement(query)) {
			st.setInt(1, tarjeta.getId());
			st.setInt(2, parquimetro.getId());

			try (ResultSet rs = st.executeQuery()) {
				if(rs.next()) {
					String result = rs.getString("operacion");
					if(Objects.equals(result, "error")) {
						if(Objects.equals(result, "tarjeta")) {
							throw new TarjetaNoExisteException();
						} else {
							throw new ParquimetroNoExisteException();
						}
					}
					if(Objects.equals(result, "apertura")) {
						if(Objects.equals(rs.getString("mensaje"), "Apertura exitosa")) {
							return aperturaEstacionamiento(rs);
						} else {
							throw new SinSaldoSuficienteException();
						}
					} else {
						return cierreEstacionamiento(rs);
					}
				}
			}
		}
		catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception(ex.getMessage());
		}

		return null;
	}

	private EstacionamientoDTO aperturaEstacionamiento(ResultSet rs) throws SQLException {
		SimpleDateFormat formato_fecha = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formato_hora = new SimpleDateFormat("HH:mm:ss");
		return new EntradaEstacionamientoDTOImpl(
				Integer.toString(rs.getInt("tiempo_disponible")),
				formato_fecha.format(rs.getDate("fecha_apertura")),
				formato_hora.format(rs.getTime("hora_apertura")));
	}

	private EstacionamientoDTO cierreEstacionamiento(ResultSet rs) throws SQLException {
		SimpleDateFormat formato_fecha = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formato_hora = new SimpleDateFormat("HH:mm:ss");
		return new SalidaEstacionamientoDTOImpl(
				Integer.toString(rs.getInt("tiempo_transcurrido")),
				Float.toString(rs.getFloat("saldo")),
				formato_fecha.format(rs.getDate("fecha_apertura")),
				formato_hora.format(rs.getTime("hora_apertura")),
				formato_fecha.format(rs.getDate("fecha_sal")),
				formato_hora.format(rs.getTime("hora_sal")));
	}
}
