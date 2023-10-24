package parquimetros.modelo.inspector;

import java.sql.*;
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

    @Override
    public EstacionamientoPatenteDTO recuperarEstacionamiento(String patente, UbicacionBean ubicacion) throws Exception {

        logger.info(Mensajes.getMessage("ModeloInspectorImpl.recuperarEstacionamiento.logger"),patente,ubicacion.getCalle(),ubicacion.getAltura());

        String query = "SELECT * FROM parquimetros.estacionados E WHERE E.patente = ? AND E.calle = ? AND E.altura = ?;";
        String estado;
        String [] fechaHora = Fechas.getDiaTurnoFechaHora();

        try (PreparedStatement st = this.conexion.prepareStatement(query)) {
            st.setString(1, patente);
            st.setString(2, ubicacion.getCalle());
            st.setInt(3, ubicacion.getAltura());

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    estado = EstacionamientoPatenteDTO.ESTADO_REGISTRADO;
                } else {
                    estado = EstacionamientoPatenteDTO.ESTADO_NO_REGISTRADO;
                }
            }
        } catch (SQLException ex)
        {
            logger.error("SQLException: " + ex.getMessage());
            logger.error("SQLState: " + ex.getSQLState());
            logger.error("VendorError: " + ex.getErrorCode());
            throw new Exception("Error inesperado al consultar la B.D.");
        }
        return new EstacionamientoPatenteDTOImpl(patente, ubicacion.getCalle(), Integer.toString(ubicacion.getAltura()), fechaHora[2], fechaHora[3], estado);
    }

    @Override
    public ArrayList<MultaPatenteDTO> generarMultas(ArrayList<String> listaPatentes,
                                                    UbicacionBean ubicacion,
                                                    InspectorBean inspectorLogueado)
            throws InspectorNoHabilitadoEnUbicacionException, Exception {

        logger.info(Mensajes.getMessage("ModeloInspectorImpl.generarMultas.logger"),listaPatentes.size());

        try {
            //Chequeo de inspector habilitado
            checkInspectorHabilitado(ubicacion, inspectorLogueado);
        } catch (ConexionParquimetroException e) {
            throw new InspectorNoHabilitadoEnUbicacionException();
        }

        String query = "SELECT E.patente FROM parquimetros.estacionados E WHERE E.calle = ? AND E.altura = ?;";
        String queryID = "SELECT A.id_asociado_con FROM parquimetros.asociado_con A WHERE A.legajo = ? AND A.calle = ? AND A.altura = ?;";
        ArrayList<MultaPatenteDTO> multas = new ArrayList<MultaPatenteDTO>();
        String[] fechaHora = Fechas.getDiaTurnoFechaHora();
        int id_asociado = 0;

        try (PreparedStatement psPatentes = this.conexion.prepareStatement(query);
             PreparedStatement psID = this.conexion.prepareStatement(queryID)) {
            psPatentes.setString(1, ubicacion.getCalle());
            psPatentes.setInt(2, ubicacion.getAltura());
            psID.setInt(1, inspectorLogueado.getLegajo());
            psID.setString(2, ubicacion.getCalle());
            psID.setInt(3, ubicacion.getAltura());

            try (ResultSet rsPatentes = psPatentes.executeQuery();
                 ResultSet rsID = psID.executeQuery()) {
                //Elimino las patentes con un estacionamiento abierto
                while (rsPatentes.next()) {
                    listaPatentes.remove(rsPatentes.getString("patente"));
                }

                //Obtengo el id_asociado_con del inspector
                if(rsID.next()) {
                    id_asociado = rsID.getInt("id_asociado_con");
                }

                //Se crean las multas correspondientes
                int nroMulta = 1;
                for (String p: listaPatentes) {
                    MultaPatenteDTO multa = new MultaPatenteDTOImpl(
                            Integer.toString(nroMulta++),
                            p,
                            ubicacion.getCalle(),
                            Integer.toString(ubicacion.getAltura()),
                            fechaHora[2],
                            fechaHora[3],
                            Integer.toString(inspectorLogueado.getLegajo())
                    );

                    multas.add(multa);
                }
            }

            String queryMultas = "INSERT INTO parquimetros.multa (patente, id_asociado_con, fecha, hora) VALUES (?,?,?,?) ";

            //Utilizando lotes, se insertan las multas en la base de datos
            try (PreparedStatement psMultas = this.conexion.prepareStatement(queryMultas)) {
                for (MultaPatenteDTO m : multas) {
                    psMultas.setString(1, m.getPatente());
                    psMultas.setInt(2,id_asociado);
                    psMultas.setString(3, fechaHora[2]);
                    psMultas.setString(4, fechaHora[3]);

                    psMultas.addBatch();
                }

                psMultas.executeBatch();
            }
        } catch (SQLException ex) {
            logger.error("SQLException: " + ex.getMessage());
            logger.error("SQLState: " + ex.getSQLState());
            logger.error("VendorError: " + ex.getErrorCode());
            throw new Exception("Error inesperado al consultar la B.D.");
        }
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
        /*
        En el caso de que el inspector este habilitado retorna una tabla con una unica tupla
        que contiene un 1, caso contrario devuelve una tabla vacia.
         */
        String queryCheck = "SELECT EXISTS (SELECT 1 FROM parquimetros.asociado_con A WHERE A.legajo = ? AND A.calle = ? AND A.altura = ? AND A.dia = ? AND A.turno = ?)";

        try (PreparedStatement stCheck = this.conexion.prepareStatement(queryCheck)) {
            stCheck.setInt(1, inspectorLogueado.getLegajo());
            stCheck.setString(2, ubicacion.getCalle());
            stCheck.setInt(3, ubicacion.getAltura());
            stCheck.setString(4, fechaHora[0]);
            stCheck.setString(5, fechaHora[1]);


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