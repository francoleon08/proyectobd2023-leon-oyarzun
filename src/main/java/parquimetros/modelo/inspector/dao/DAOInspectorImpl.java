package parquimetros.modelo.inspector.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		logger.info("Se intenta realizar la autenticacion en la base de datos al usuario con legajo : "+legajo);

		try {
			InspectorBean inspector = new InspectorBeanImpl();
			String query = "SELECT * FROM parquimetros.inspectores I WHERE I.legajo = "+legajo+" AND I.password = md5('"+password+"');";

			Statement st = this.conexion.createStatement();
			ResultSet rs = st.executeQuery(query);

			while(rs.next()) {
				inspector.setLegajo(rs.getInt("legajo"));
				inspector.setDNI(rs.getInt("dni"));
				inspector.setNombre(rs.getString("nombre"));
				inspector.setApellido(rs.getString("apellido"));
				inspector.setPassword(rs.getString("password"));
			}

			//Mejorar este control!!!
			if(inspector.getNombre() == null) {
				throw new InspectorNoAutenticadoException("Inspector no autorizado.");
			}
			return inspector;
		} catch (SQLException ex)
		{
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
	}
}
