package parquimetros.modelo.inspector.dao;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parquimetros.modelo.beans.*;

public class DAOParquimetroImpl implements DAOParquimetro {

	private static Logger logger = LoggerFactory.getLogger(DAOParquimetroImpl.class);
	
	private Connection conexion;
	
	public DAOParquimetroImpl(Connection c) {
		this.conexion = c;
	}

	@Override
	public UbicacionBean recuperarUbicacion(ParquimetroBean parquimetro) throws Exception {
		logger.info(String.format("Se busca en la base de datos la ubicacion del parquiemtro [%d].", parquimetro.getId()));

		UbicacionBean ubicacion = new UbicacionBeanImpl();
		String query = "SELECT * FROM parquimetros.parquiemtros P WHERE P.id_parq = ?;";

		try(PreparedStatement st = this.conexion.prepareStatement(query)) {
			st.setInt(1, parquimetro.getId());

			try (ResultSet rs = st.executeQuery(query)){
				if (rs.next()) {
					ubicacion.setCalle(rs.getString("calle"));
					ubicacion.setAltura(rs.getInt("altura"));
					ubicacion.setTarifa(rs.getDouble("tarifa"));

					parquimetro.setUbicacion(ubicacion);
				}
			}
		} catch (SQLException ex)
		{
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}

		return ubicacion;
	}
}
