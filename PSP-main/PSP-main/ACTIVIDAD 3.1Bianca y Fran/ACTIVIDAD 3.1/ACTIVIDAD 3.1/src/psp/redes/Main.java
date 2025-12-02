package psp.redes;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GestorDescargas gd = new GestorDescargas(); // Creamos instancia de la clase
        String url = "http://ftp.sun.ac.za/ftp/pub/documentation/security/port-numbers.txt"; // URL del archivo
        String ruta ="NombrePuertos.txt";
        gd.descargarArchivo(url, ruta); // Llamada al métodos para descargarlo y guardarlo

        Scanner tecl = new Scanner(System.in);
        String direccion="";

        System.out.println("Introduce la dirección:");
        if (tecl.hasNextLine()) {
            direccion = tecl.nextLine();
        } else {
            System.out.println("Dirección no válida");
            tecl.next();
        }

        String os = System.getProperty("os.name").toLowerCase();
        String[] comando;

        if (validarSubred(direccion)) {
            for (int i = 0; i < 255; i++) {
                if (os.contains("win")) { // para windows
                    comando = new String[]{"ping", "-n", "2", direccion + "." + i};
                } else {
                    comando = new String[]{"ping", "-c", "2", direccion + "." + i};
                }

                Thread h1 = new Thread(new EjecutarPing(comando));
                h1.start();
            }
        }else {
            System.out.println("Direccion IP no valida: "+ direccion);
        }
    }
    /**
     * Método proporcionado por el profesor para validar la dirección de una subred de 24 bits (/24) a partir de una
     * String. Para ser considerada válida, la dirección de red debería cumplir el formato X.X.X dónde cada X puede
     * tomar un valor entre 0 y 255 (p.e.: 192.168.0).
     * @param ip La dirección de la subred /24 (X.X.X) a validar en formato texto (NO INCLUYE LA PARTE DE DIRECCIÓN DEL
     *          HOST).
     * @return true en caso de que se valide que el identificador de la subred cumple los criterios exigidos y false
     *          en caso contrario.
     */
    public static boolean validarSubred(String ip) {
        // Step 1: Separate the given string into an array of strings using the dot as delimiter
        String[] parts = ip.split("\\.");

        // Step 2: Check if there are exactly 3 parts
        if (parts.length != 3) {
            return false;
        }

        // Step 3: Check each part for valid number
        for (String part : parts) {
            try {
                // Step 4: Convert each part into a number
                int num = Integer.parseInt(part);

                // Step 5: Check whether the number lies in between 0 to 255
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // If parsing fails, it's not a valid number
                return false;
            }
        }

        // If all checks passed, return true
        return true;
    }
}
