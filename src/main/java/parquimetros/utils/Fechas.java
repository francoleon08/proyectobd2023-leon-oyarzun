package parquimetros.utils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//CLASE IMPLEMENTADA PROVISTA POR LA CATEDRA
//Clase con métodos estáticos para convertir diferentes formatos de fechas
public class Fechas
{
   private static Logger logger = LoggerFactory.getLogger(Fechas.class);

   public static Date hoy() {
		LocalDateTime myDateTime = LocalDateTime.now();
		return Date.valueOf(myDateTime.toLocalDate());
   }	   
   
   public static java.util.Date convertirStringADate(String p_fecha) throws Exception
   {
      java.util.Date retorno = null;
      if (p_fecha != null)
      {
         try
         {
            retorno = (new SimpleDateFormat("yyyy-MM-dd")).parse(p_fecha);
         }
         catch (ParseException ex)
         {
        	logger.error("Se produjo un error en la conversión del string {} a fecha mensaje {}",p_fecha,ex.getMessage());
        	throw new Exception("Se produjo un error en la conversión del string a fecha.");
        	
         }
      }
      return retorno;
   }

   public static java.util.Date convertirStringADate(String p_fecha, String p_hora) throws Exception
   {
      java.util.Date retorno = null;
      if ((p_fecha != null) && (p_hora != null))
      {
         try
         {
            retorno = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(p_fecha + " " + p_hora);
         }
         catch (ParseException ex)
         {
         	logger.error("Se produjo un error en la conversión del string {} a fecha mensaje {}",p_fecha,ex.getMessage());
         	throw new Exception("Se produjo un error en la conversión del string a fecha.");
         }
      }
      return retorno;
   }
   
   
   public static String convertirDateAString(java.util.Date p_fecha)
   {
      String retorno = null;
      if (p_fecha != null)
      {
         retorno = (new SimpleDateFormat("dd/MM/yyyy")).format(p_fecha);
      }
      return retorno;
   }

   public static String convertirDateAHoraString(java.util.Date p_fecha)
   {
      String retorno = null;
      if (p_fecha != null)
      {
         retorno = (new SimpleDateFormat("HH:mm:ss")).format(p_fecha);
      }
      return retorno;
   }
   
   public static String convertirDateAStringDB(java.util.Date p_fecha)
   {
      String retorno = null;
      if (p_fecha != null)
      {
         retorno = (new SimpleDateFormat("yyyy-MM-dd")).format(p_fecha);
      }
      return retorno;
   }

   public static java.sql.Date convertirDateADateSQL(java.util.Date p_fecha)
   {
      java.sql.Date retorno = null;
      if (p_fecha != null)
      {
         retorno = java.sql.Date.valueOf((new SimpleDateFormat("yyyy-MM-dd")).format(p_fecha));
      }
      return retorno;
   }


   public static java.sql.Date convertirStringADateSQL(String p_fecha) throws Exception 
   {
      java.sql.Date retorno = null;
      if (p_fecha != null)
      {
         retorno = Fechas.convertirDateADateSQL(Fechas.convertirStringADate(p_fecha));
      }
      return retorno;
   }

   public static boolean validar(String p_fecha) throws Exception
   {
      if (p_fecha != null)
      {
         try
         {
        	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        	sdf.setLenient(false);
        	sdf.parse(p_fecha);
            return true;
         }
         catch (ParseException ex) {
         	logger.error("Se produjo un error en la conversión del string {} a fecha mensaje {}",p_fecha,ex.getMessage()); 
         	throw new Exception("Se produjo un error en la conversión del string a fecha.");
         }
      }
      return false;
   }
   
   public static String convertirStringSQL(String p_fecha)
   {
	   if (p_fecha==null)
	   {
		   return null;
	   }
	   else 
	   {
		   return ("" + p_fecha.charAt(8) + p_fecha.charAt(9) +"/"+
  					p_fecha.charAt(5) + p_fecha.charAt(6) +"/"+
  					p_fecha.charAt(0) + p_fecha.charAt(1) + p_fecha.charAt(2) + p_fecha.charAt(3));
	   }
   }

   /**
    * Arreglo Strings donde [0] corresponde al dia de la semana, [1] al turno,
    * [2] a la fecha actual y [3] a la hora actual.
    * @return String []
    */
   public static String [] getDiaTurnoFechaHora() {
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
   
}
