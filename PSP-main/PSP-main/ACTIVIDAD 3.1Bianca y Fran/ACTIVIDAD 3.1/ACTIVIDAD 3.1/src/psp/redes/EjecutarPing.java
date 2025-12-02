package psp.redes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class EjecutarPing implements Runnable {

    private static final Object lock = new Object();
    ProcessBuilder pb;
    Process p;
    int MAX_PUERTOS = 1000;
    String[] comando = null;

    public EjecutarPing(String[] comando){
        this.comando = comando;
    }

    @Override
    public void run() {
        ejecutarPN(comando);
    }

    public void ejecutarPN(String[] comando) {
        try {
            // Creamos un ProcessBuilder para ejecutar el comando externo (ping)
            pb = new ProcessBuilder(comando);
            // Redirigimos los errores del proceso al flujo de salida estándar (para no perder mensajes)
            pb.redirectErrorStream(true);
            // Iniciamos el proceso y lo guardamos en 'p'
            p = pb.start();

            // Obtenemos la dirección IP que se está usando en el comando
            // Suponemos que la IP siempre está en la posición 3 del array
            String ip = comando[3]; // Recogemos el host de la ip para la salida
            // Esperamos a que el proceso de ping termine y capturamos su código de salida
            // 0 significa éxito (la IP respondió)
            int retorno = p.waitFor(); // devolvemos el código de salida
            // Si el ping fue exitoso
            if (retorno == 0) {
                // Construimos toda la salida de la IP en un buffer
                StringBuilder salida = new StringBuilder();
                salida.append("IP ").append(ip).append(" ACTIVA\n");

                // Recorremos los puertos desde 0 hasta MAX_PUERTOS
                for (int i = 0; i < MAX_PUERTOS; i++) {
                    // Comprobamos si el puerto está abierto
                    if (estaAbierto(ip, i, 2000)) {
                        // Si está abierto, obtenemos la descripción del puerto desde NombrePuertos.txt
                        String descripcion = obtenerDescripcionPuerto(i,"NombrePuertos.txt");
                        // Mostramos por pantalla el puerto abierto y su descripción
                        salida.append("\tPuerto: ").append(i)
                                .append(" ABIERTO: ").append(descripcion)
                                .append("\n");
                    }
                }
                // Imprimir el resultado
                synchronized (lock) {
                    System.out.print(salida);
                }
            }
        } catch (IOException | InterruptedException e) {
            // Capturamos cualquier error de ejecución del proceso o interrupción y lo lanzamos como RuntimeException
            throw new RuntimeException(e);
        }
    }

    public static boolean estaAbierto(String IP, int puerto, int tiempo) {
        // Creamos un socket dentro de un try-with-resources, para cerrarlo automáticamente
        try (Socket socket = new Socket()) {
            // Creamos una dirección de socket con la IP y el puerto a comprobar
            SocketAddress socketAddress = new InetSocketAddress(IP, puerto);
            // Intentamos conectarnos al puerto con un timeout de 'tiempo' milisegundos
            socket.connect(socketAddress, tiempo);
            // Si no lanza excepción, el puerto está abierto
            return true;
        } catch (IOException e) {
            // Si ocurre cualquier excepción (timeout, rechazo de conexión), el puerto está cerrado
            return false;
        }
    }
    public static String obtenerDescripcionPuerto(int puertoBuscado, String filePath) {
        // Abrimos el archivo para lectura con BufferedReader dentro de un try-with-resources,
        // así se cierra automáticamente al terminar.
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            // Leemos el archivo línea por línea
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                // Ignoramos líneas vacías o que sean comentarios (#)
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                // Dividimos la línea en palabras usando cualquier cantidad de espacios como separador
                String[] partes = linea.split("\\s+");
                for (String parte : partes) {
                    // Recorremos cada palabra buscando la que contiene "/tcp"
                    if (parte.contains("/tcp")) {
                        // Extraemos la parte antes del "/tcp" (posible número de puerto)
                        String numeroStr = parte.split("/")[0]; // Parte antes del "/tcp"
                        int puerto;
                        // Intentamos convertir esa parte en un número entero
                        // Si falla (ej: "any", "tcpmux") lo ignoramos con continue
                        try {
                            puerto = Integer.parseInt(numeroStr);
                        } catch (NumberFormatException e) {
                            // no es un número válido, seguimos con la siguiente palabra
                            continue; // Ignorar si no es número
                        }
                        // Si el puerto coincide con el que estamos buscando
                        if (puerto == puertoBuscado) {
                            // La descripción es todolo que queda en la línea después del "/tcp"
                            int index = linea.indexOf(parte) + parte.length();
                            String descripcion = linea.substring(index).trim();
                            // Si no hay descripción, usamos la primera palabra como respaldo
                            if (descripcion.isEmpty() && partes.length > 1) {
                                // Si no hay descripción, usar la siguiente palabra
                                descripcion = partes[0];
                            }
                            // Devolvemos la descripción encontrada
                            return descripcion;
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Capturamos errores de lectura del archivo
            System.err.println("Error leyendo archivo: " + e.getMessage());
        }
        // Si no encontramos el puerto, devolvemos un mensaje por defecto
        return "Descripción no encontrada";
    }

}
