package psp.redes;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class GestorDescargas {
    InputStream is = null;       // Flujo de bytes desde la URL
    BufferedReader br = null;    // Buffer para leer texto línea por línea
    FileWriter fw = null;        // Flujo para escribir texto en un fichero

    public void descargarArchivo(String url_descarga, String nombreFich){
        System.out.println("Descargando: "+url_descarga);

        try {
            URL url = new URL(url_descarga);                 // Creamos un objeto URL a partir del String
            is = url.openStream();                           // Abrimos un flujo de bytes desde la URL
            br = new BufferedReader(new InputStreamReader(is)); // Envolvemos el InputStream para leer línea a línea
            fw = new FileWriter(nombreFich);                // Flujo para escribir en el fichero local

            String linea;
            while((linea=br.readLine())!=null){            // Leemos el contenido línea por línea
                fw.write(linea + "\n");                    // Escribimos cada línea en el fichero con salto de línea
            }
        } catch (MalformedURLException e) {                // Captura si la URL tiene formato inválido
            throw new RuntimeException(e);
        } catch (IOException e) {                          // Captura errores de lectura/escritura
            throw new RuntimeException(e);
        }finally {
            try {
                fw.close();   // Cerramos el FileWriter para liberar recursos
                br.close();   // Cerramos el BufferedReader
                is.close();   // Cerramos el InputStream
            } catch (IOException e) {                       // Captura errores al cerrar los flujos
                throw new RuntimeException(e);
            }
        }

    }

}



