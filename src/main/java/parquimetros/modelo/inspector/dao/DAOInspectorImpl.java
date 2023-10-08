package parquimetros.modelo.inspector.dao;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parquimetros.modelo.beans.InspectorBean;
import parquimetros.modelo.beans.InspectorBeanImpl;
import parquimetros.modelo.inspector.exception.InspectorNoAutenticadoException;

public class DAOInspectorImpl implements DAOInspector {

	private static Logger logger = LoggerFactory.getLogger(DAOInspectorImpl.class);
	
	private Connection conexion;
	
	public DAOInspectorImpl(Connection c) {
		this.conexion = c;
	}

	@Override
	public InspectorBean autenticar(String legajo, String password) throws InspectorNoAutenticadoException, Exception {
		logger.info("Se intenta realizar la autenticación en la base de datos al usuario con legajo : " + legajo);
		InspectorBean inspector = null;
		String query = "SELECT * FROM parquimetros.inspectores I WHERE I.legajo = ? AND I.password = md5(?)";

		try (PreparedStatement st = this.conexion.prepareStatement(query)) {
			st.setString(1, legajo);
			st.setString(2, password);

			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					inspector = new InspectorBeanImpl();
					inspector.setLegajo(rs.getInt("legajo"));
					inspector.setDNI(rs.getInt("dni"));
					inspector.setNombre(rs.getString("nombre"));
					inspector.setApellido(rs.getString("apellido"));
					inspector.setPassword(rs.getString("password"));
				}
			}

			if (inspector == null) {
				throw new InspectorNoAutenticadoException("Inspector no autorizado.");
			}
			return inspector;
		} catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
	}

}
