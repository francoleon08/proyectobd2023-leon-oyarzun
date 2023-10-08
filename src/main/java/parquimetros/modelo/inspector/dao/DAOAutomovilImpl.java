package parquimetros.modelo.inspector.dao;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parquimetros.modelo.inspector.exception.AutomovilNoEncontradoException;

public class DAOAutomovilImpl implements DAOAutomovil {

	private static Logger logger = LoggerFactory.getLogger(DAOAutomovilImpl.class);
	
	private Connection conexion;
	
	public DAOAutomovilImpl(Connection conexion) {
		this.conexion = conexion;
	}


	@Override
	public void verificarPatente(String patente) throws AutomovilNoEncontradoException, Exception {
		logger.info("Se busca en la base de datos si la patente [?] se encuentra registrada.", patente);

		String query = "SELECT EXISTS (SELECT A.patente FROM parquimetros.automoviles A WHERE A.patente = ?)";

		try (PreparedStatement st = this.conexion.prepareStatement(query)) {
			st.setString(1, patente);
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next() && !rs.getBoolean(1)) {
					throw new AutomovilNoEncontradoException("La patente no se encuentra registrada.");
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
