package parquimetros.modelo.login;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parquimetros.modelo.login.beans.UsuarioBean;
import parquimetros.modelo.login.beans.UsuarioBeanImpl;
import parquimetros.utils.Conexion;
import parquimetros.utils.Config;

//CLASE IMPLEMENTADA PROVISTA POR LA CATEDRA
public class ModeloLoginImpl implements ModeloLogin {
	
	private static Logger logger = LoggerFactory.getLogger(ModeloLoginImpl.class);
	
	private static final String USUARIOS_FILE = Config.getProperty("usuarios");
	private static final String CONEXION_FILE = Config.getProperty("bd.conexion");	
	
	private HashMap<String,UsuarioBean> usuarios = new HashMap<String,UsuarioBean>();
	
	public ModeloLoginImpl() {
		//Conexion a la base de datos
		Conexion.inicializar(CONEXION_FILE);

		/*
		 * Cargamos del archivo de propiedades los usuarios de MySQL que vamos utilizar para conectarnos a la B.D.
		 */
		Properties prop = new Properties();
		try
		{
			FileInputStream file=new FileInputStream(USUARIOS_FILE); 
			prop.load(file);
			
			Enumeration<?> enumeration = prop.propertyNames();
			
			while (enumeration.hasMoreElements()) {
				
				String key=enumeration.nextElement().toString();
				String[] label = key.split("[.]");  // el punto es un caracter especial que significa cualquier caracter como en SQL entonces hay que escapar o utilizar clase caracter con []
				
				String valor = prop.getProperty(key);
				
				UsuarioBean user;				
				if (usuarios.containsKey(label[0])) {
					user = usuarios.get(label[0]);					
				} else {
					user = new UsuarioBeanImpl();
				}
				
				if (label[1].equals("username")) user.setUsername(valor);
				if (label[1].equals("password")) user.setPassword(valor);
				if (label[1].equals("displayname")) user.setDisplayname(valor);
				
				usuarios.put(label[0], user);
			}					
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
 	public List<String> obtenerNombresUsuarios() {
		LinkedList<String> nombres = new LinkedList<String>();
		
		for(Map.Entry<String,UsuarioBean> m : usuarios.entrySet()){
			UsuarioBean user = m.getValue();
			nombres.add(user.getDisplayname());
		}  		
		
		return nombres;
	}
	
	@Override
	public UsuarioBean obtenerUsuario(String displayname)
	{
		logger.info("Ingresa a recuperar el usuarioBD y passwordBD para acceder a la BD del usuario {}", displayname);

		for(Map.Entry<String,UsuarioBean> m : usuarios.entrySet()){
			UsuarioBean user = m.getValue();
			if (user.getDisplayname().equals(displayname)) {
				return user;
			}
		}  		
		return null;
	}

	@Override
	public void iniciarConexion() throws Exception {
		Conexion.inicializar(CONEXION_FILE);		
	}

}
