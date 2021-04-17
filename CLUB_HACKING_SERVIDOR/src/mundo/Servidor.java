package mundo;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.TreeMap;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Servidor extends JFrame {
   private JTextField campoIntroducir;
   private JTextArea areaPantalla;
   private JTextArea comandos;
   private ObjectOutputStream salida;
   private ObjectInputStream entrada;
   private ServerSocket servidor;
   private Socket conexion;
   private int contador = 1;
   
   public Servidor()
   {
      super( "Servidor" );

      Container contenedor = getContentPane();

      campoIntroducir = new JTextField();
      
      //campoIntroducir.setEditable( false );
      
      ManejaCampo manejador = new ManejaCampo();
      campoIntroducir.addActionListener(manejador);
      contenedor.add( campoIntroducir, BorderLayout.NORTH );
      
      areaPantalla = new JTextArea();
      contenedor.add( new JScrollPane( areaPantalla ), BorderLayout.CENTER );

      TreeMap<String, String > listaComandos = new TreeMap<>();
      listaComandos.put("INFO", "Obtiene informacion del sistema operativo y hardware.");
      listaComandos.put("APAGAR", "Apaga el computador (Valido para Windows, Linux y Mc Os).");
      listaComandos.put("CERRAR", "Cierra sesion en Windows.");
      listaComandos.put("RUTA", "Obtiene los nombres de todos los archivos guardados.");
      listaComandos.put("HISTORIAL", "Obtiene el historial de navegacion.");
      
      String listica = "";
      for(Map.Entry<String,String> entry : listaComandos.entrySet()) {
    	  String key = entry.getKey();
    	  String value = entry.getValue();
    	  listica= listica+ key+" "+value+"\n";
    	}
      
      comandos = new JTextArea(listica);
      comandos.setEditable(false);
      
      contenedor.add(comandos, BorderLayout.WEST);
      
      setSize( 800, 250 );
      setVisible( true );

   } 
  
  private class ManejaCampo implements ActionListener
  {
     public void actionPerformed( ActionEvent evento )
     {
       enviarDatos(campoIntroducir.getText());
       campoIntroducir.setText("");
     }
  }  
  
  
   public void ejecutarServidor()
   {    
      try {
         // Paso 1: crear un objeto ServerSocket.
         servidor = new ServerSocket(12345);

         while ( true ) {

            try {
               esperarConexion();  // Paso 2: esperar una conexión.
               obtenerFlujos();    // Paso 3: obtener flujos de entrada y salida.
               procesarConexion(); // Paso 4: procesar la conexión.
            }
            // procesar excepción EOFException cuando el cliente cierre la conexión 
            catch ( EOFException excepcionEOF ) {
               System.err.println( "El servidor terminó la conexión" );
            }
            finally {
               cerrarConexion();   // Paso 5: cerrar la conexión.
               contador++;
            }
         } 
      } // fin del bloque try

      // procesar problemas con E/S
      catch ( IOException excepcionES ) {
         excepcionES.printStackTrace();
      }

   } // fin del método ejecutarServidor
  
   private void esperarConexion() throws IOException
   {
      mostrarMensaje( "Esperando una conexion" );
      conexion = servidor.accept();
      mostrarMensaje( "Conexion " + contador + " recibida de: " +
         conexion.getInetAddress().getHostName() );
   }

   private void obtenerFlujos() throws IOException
   {      
      salida = new ObjectOutputStream( conexion.getOutputStream() );
      salida.flush(); 
      entrada = new ObjectInputStream( conexion.getInputStream() );
      mostrarMensaje( "\nSe establecieron los flujos de E/S\n" );
   }

   private void procesarConexion() throws IOException
   {
      String mensaje = "Conexion exitosa";
      enviarDatos( mensaje );
      //establecerCampoTextoEditable( true );

      do {
         try {
            mensaje = ( String ) entrada.readObject();
            mostrarMensaje( "\n" + mensaje );
         }
         catch ( ClassNotFoundException excepcionClaseNoEncontrada ) {
            mostrarMensaje( "\nSe recibió un tipo de objeto desconocido" );
         }
      } while ( !mensaje.equals( "CLIENTE>>> TERMINAR" ) );
   } 

   private void cerrarConexion() 
   {
      mostrarMensaje( "\nFinalizando la conexión\n" );
 //     establecerCampoTextoEditable( false );

      try {
         salida.close();
         entrada.close();
         conexion.close();
      }
      catch( IOException excepcionES ) {
         excepcionES.printStackTrace();
      }
   }

   private void enviarDatos( String mensaje )
   {
      try {
         salida.writeObject( "SERVIDOR>>> " + mensaje );
         salida.flush();
         mostrarMensaje( "\nSERVIDOR>>> " + mensaje );
      }
      catch ( IOException excepcionES ) {
         areaPantalla.append( "\nError al escribir objeto" );
      }
   }

   private void mostrarMensaje(String mensajeAMostrar )
   {
      areaPantalla.append( mensajeAMostrar );    
   }
   
/*
   // método utilitario que es llamado desde otros subprocesos para manipular a 
   // campoIntroducir en el subproceso despachador de eventos
   private void establecerCampoTextoEditable( final boolean editable )
   {
      // mostrar mensaje del subproceso de ejecución despachador de eventos
      SwingUtilities.invokeLater(
         new Runnable() {  // clase interna para asegurar que la GUI se actualice apropiadamente

            public void run()  // establece la capacidad de modificar a campoIntroducir
            {
               campoIntroducir.setEditable( editable );
            }

         }  // fin de la clase interna

      ); // fin de la llamada a SwingUtilities.invokeLater
   }
*/
   public static void main( String args[] )
   {
      Servidor aplicacion = new Servidor();
      aplicacion.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      aplicacion.ejecutarServidor();
   }
}