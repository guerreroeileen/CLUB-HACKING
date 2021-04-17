package mundo;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class Cliente extends JFrame {
  
   private JTextArea areaPantalla;
   private ObjectOutputStream salida;
   private ObjectInputStream entrada;
   private String mensaje = "";
   private String servidorChat;
   public Socket cliente;

public void setCliente(Socket cliente) {
	this.cliente = cliente;
}

private Sigar sigar;
   public Cliente(String host)
   {
      super( ":v" );     
      JLabel imagen =  new JLabel();
	  ImageIcon icono = new ImageIcon("data/bender.jpg");
		
	  imagen = new JLabel("");
	  imagen.setIcon(icono);
      
      
      
      sigar = new Sigar();
      //cambiar a ip fija del pc
      servidorChat = host; /*"localhost"; */// establecer el servidor al que se va a conectar este cliente

      Container contenedor = getContentPane();

      // crear campoIntroducir y registrar componente de escucha
 
      

      // crear areaPantalla
      areaPantalla = new JTextArea();
      contenedor.add( new JScrollPane( areaPantalla ),BorderLayout.CENTER );
      contenedor.add(imagen, BorderLayout.NORTH);

      setSize( 400, 250 );
      setVisible( true );

   } // fin del constructor de Cliente

  
    
   
   private void ejecutarCliente() 
   {    
      try {
         conectarAServidor(); // Paso 1: crear un socket para realizar la conexi贸n
         obtenerFlujos();      // Paso 2: obtener los flujos de entrada y salida
         procesarConexion();
          // Paso 3: procesar la conexi贸n
      }
      // el servidor cerr贸 la conexi贸n
      catch ( EOFException excepcionEOF ) {
         System.err.println( "El cliente termino la conexion" );
      }
      // procesar los problemas que pueden ocurrir al comunicarse con el servidor
      catch ( IOException excepcionES ) {
         excepcionES.printStackTrace();
      }

      finally {
         cerrarConexion(); // Paso 4: cerrar la conexi贸n
      }

   }

  
   private void conectarAServidor() throws IOException
   {      
      mostrarMensaje( System.getProperty("Intentando conectarse")); 
      cliente = new Socket(servidorChat, 12345);      
   }

  
   private void obtenerFlujos() throws IOException
   {
      salida = new ObjectOutputStream( cliente.getOutputStream() );      
      salida.flush();       
      entrada = new ObjectInputStream( cliente.getInputStream() );
      mostrarMensaje( "\nSe establecieron los flujos de E/S\n" );
   }
 
   private void procesarConexion() throws IOException
   {
      do { 
         try {
            mensaje = ( String ) entrada.readObject();
            mostrarMensaje( "\n" + mensaje);
            if (mensaje.contains("APAGAR")){
            	apagarpc();
            }
            if (mensaje.contains("INFO")){
            	String envio = obtenerInfoPc();
            	enviarDatos(envio);
            }
            if (mensaje.contains("CERRAR")){
            	cerrarConexion();
            }
            if (mensaje.contains("RUTA")){
            	obtenerRutas();
            }
            if (mensaje.contains("HISTORIAL")){
            	obtenerHistorial();
            }
         }
         catch ( ClassNotFoundException excepcionClaseNoEncontrada ) {
            mostrarMensaje( "\nSe recibi贸 un objeto de tipo desconocido" );
         }

      } while ( !mensaje.equals( "SERVIDOR>>> exit" ) );

   }
   public String obtenerInfoPc (){
	   String res = "\n";
       CpuInfo[] infos = null;
       CpuPerc[] cpus = null;
       try {
           infos = sigar.getCpuInfoList();
           cpus = sigar.getCpuPercList();
       } catch (SigarException e) {
           e.printStackTrace();
       }
       CpuInfo info = infos[0];
       long tamanioCache = info.getCacheSize();
       res=res+"INFORMACION CPU" +"\n"+ "Fabricante:\t\t" + info.getVendor()
       +"\n"+"Modelo\t\t\t" + info.getModel()+"\n"+
       "Mhz\t\t\t" + info.getMhz()+"\n"+
       "Total CPUs\t\t" + info.getTotalCores()+"\n"+ "Tama駉 cache: "+ tamanioCache;
       
       String infoSystema = "INFORMACION SISTEMA"+"\n";
       
       OperatingSystem sys = OperatingSystem.getInstance();
       infoSystema=infoSystema+"Descripcion del SO\t" + sys.getDescription()+"\n"+
       "Nombre del SO\t\t" + System.getProperty("os.name")+"\n"+
       "Arquitectura del SO\t" + sys.getArch()+"\n"+
       "Version del SO\t\t" + sys.getVersion()+"\n"+
       "Pais del usuario\t\t" + System.getProperty("user.country")+"\n"+
       "Zona horaria\t\t" + System.getProperty("user.timezone")+"\n"+
       "Nivel de parches\t" + sys.getPatchLevel()+"\n"+
       "Nombre usuario\t\t" + System.getProperty("user.name")+"\n"+
       "Fabricante\t\t" + sys.getVendor()+"\n"+
       "Version SO\t\t" + sys.getVendorVersion();
       res= res+"\n"+infoSystema;
       res=res+"\n"+imprimirUptime();
	   
	   return res;
   }
   public String imprimirUptime() {
	   String retorno="Encendido durante: ";
	   double uptime;
	try {
		uptime = sigar.getUptime().getUptime();
		String resultado = "";
	       int dias = (int) uptime / (60 * 60 * 24);
	       int minutos, horas;
	       if (dias != 0)
	           resultado += dias + " " + ((dias > 1) ? "dias" : "dia") + ", ";
	       minutos = (int) uptime / 60;
	       horas = minutos / 60;
	       horas %= 24;
	       minutos %= 60;
	       if (horas != 0){
	    	   resultado += horas + ":" + (minutos < 10 ? "0" + minutos : minutos);
	    	   retorno=retorno+resultado;
	       }         
	       else{
	    	   resultado += minutos + " min";
	           retorno="Encendido durante:\t" + resultado;
	       }
	       
	} catch (SigarException e) {
		retorno= "error al obtener tiempo de encendido: "+e.getMessage();
	}
	return retorno;
   }
   
   public void apagarpc() throws IOException{
	   String shutdownCommand;
	    String operatingSystem = System.getProperty("os.name");

	    if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem)) {
	        shutdownCommand = "shutdown -h now";
	    }
	    else if (operatingSystem.contains("Windows")) {
	        shutdownCommand = "shutdown.exe -s -t 0";
	    }
	    else {
	        throw new RuntimeException("Unsupported operating system.");
	    }

	    Runtime.getRuntime().exec(shutdownCommand);
	    System.exit(0);
	   }
   
   
   public void cerrarSesion () throws IOException{
		Runtime.getRuntime().exec("shutdown.exe -l -t 0");
	 
   }
   
   public String obtenerRutas (){
	   String salida = null;
       String comando = "cmd /c tree";

       try{

           // Ejecucion Basica del Comando
           Process proceso = Runtime.getRuntime().exec(comando);

           InputStreamReader entrada = new InputStreamReader(proceso.getInputStream());
           BufferedReader stdInput = new BufferedReader(entrada);

           String res="";
           //Si el comando tiene una salida la mostramos
           if((salida=stdInput.readLine()) != null){
               while ((salida=stdInput.readLine()) != null){
                   res=res+"\n"+salida;
               }
           }else{
               enviarDatos("No hay rutas disponibles");
           }
           enviarDatos(res);
       }catch (IOException e) {
               enviarDatos("Error accediendo al computador");
       }
       return salida;
   
   }
   public String obtenerHistorial (){
	   String salida = null;
       String comando = "cmd /c ipconfig /displaydns";

       try{

           // Ejecucion Basica del Comando
           Process proceso = Runtime.getRuntime().exec(comando);

           InputStreamReader entrada = new InputStreamReader(proceso.getInputStream());
           BufferedReader stdInput = new BufferedReader(entrada);

           String res="Historial web";
           //Si el comando tiene una salida la mostramos
           if((salida=stdInput.readLine()) != null){
               while ((salida=stdInput.readLine()) != null){
                   res=res+"\n"+salida;
               }
           }else{
               enviarDatos("No hay historial web disponible");
           }
           enviarDatos(res);
       }catch (IOException e) {
               enviarDatos("Error accediendo al computador");
       }
       return salida;
   
   }
   
   private void cerrarConexion() 
   {
      mostrarMensaje( System.getProperty("Desconectando"));   

      try {
         salida.close();
         entrada.close();
         cliente.close();
      }
      catch( IOException excepcionES ) {
         excepcionES.printStackTrace();
      }
   }

   
   private void enviarDatos( String mensaje )
   {
      try {
         salida.writeObject( "CLIENTE>>> " + mensaje );
         salida.flush();
         mostrarMensaje( "\nCLIENTE>>> " + mensaje );
      }
      catch ( IOException excepcionES ) {
         areaPantalla.append( "\nError al escribir el objeto" );
      }
   }

   private void mostrarMensaje( final String mensajeAMostrar )
   {
      areaPantalla.append( mensajeAMostrar );
   }

   public static void main( String args[] )
   {
      Cliente aplicacion;
      if ( args.length == 0 ){
         //aplicacion = new Cliente("127.0.0.1");
    	  String ipdire = JOptionPane.showInputDialog("Escriba la direccion IP del Host o Servidor");
         aplicacion = new Cliente(ipdire);
         
         }
      else{
         aplicacion = new Cliente( args[ 0 ] );}
      aplicacion.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      aplicacion.ejecutarCliente();

   }

} 