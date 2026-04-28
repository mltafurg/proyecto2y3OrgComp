/*********
* Archivo.java – Es el encargado de la lectura y escritura de archivos,
ademas de la limpieza de las lineas para eliminar comentarios y espacios en blanco
Lo realiza a traves de dos recorridos en el archivo de entrada,
el primero para encontrar las etiquetas y el segundo para traducir las instrucciones a binario

pero porque? 

porque las etiquetas sirven para anotar la direccion de memoria de las instrucciones 
y solo podemos conocer esa direccion si recorremos por completo el archivo,
asi que hacemos esa primera pasada para encontrar etiquetas y relacionarlas con su direccion de memoria, 
y luego en la segunda pasada traducimos las instrucciones a binario utilizando esa informacion de las etiquetas

* Autor 1: Maria Laura Tafur Gomez
*********/


import java.io.*;

public class Archivo {

    
    private String limpiarLinea(String linea) {
        linea = linea.trim();
        if (linea.isEmpty() || linea.startsWith("//")) {
            return "";
        }
        int indexComentario = linea.indexOf("//");
        if (indexComentario != -1) {
            linea = linea.substring(0, indexComentario).trim();
        }
        return linea;
    }

    // PRIMERA PASADA: Solo para encontrar etiquetas (LOOP)
    public void primeraPasada(String nombreArchivo, tablaSimbolos tabla) {
        try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            int romAddress = 0;

            while ((linea = reader.readLine()) != null) {
                linea = limpiarLinea(linea);
                if (linea.isEmpty()) continue;

                Parser parser = new Parser(linea);
                if (parser.getTipo() == 'L') {
                    // Se asocia la etiqueta con la dirección de la siguiente instrucción
                    tabla.addEntry(parser.getSymbol(), romAddress);
                } else {
                    // Solo las instrucciones A y C ocupan espacio en la memoria ROM
                    romAddress++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error en Primera Pasada: " + e.getMessage());
        }
    }

    // SEGUNDA PASADA: Traducción y escritura del archivo .hack
    public void segundaPasada(String archivoEntrada, String archivoSalida, tablaSimbolos tabla, Traductor traductor) {
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoEntrada));
             PrintWriter writer = new PrintWriter(new FileWriter(archivoSalida))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = limpiarLinea(linea);
                if (linea.isEmpty()) continue;

                Parser parser = new Parser(linea);
                String instruccionBinaria = null;

                if (parser.getTipo() == 'A') {
                    instruccionBinaria = traductor.traducirA(parser.getSymbol(), tabla);
                } else if (parser.getTipo() == 'C') {
                    instruccionBinaria = traductor.traducirC(parser.getDest(), parser.getComp(), parser.getJump());
                    if (instruccionBinaria == null) {
                        System.err.println("Error: instrucción no reconocida: " + linea);
                        return;
                    }
                } else if (parser.getTipo() == 'E') {
                    System.err.println("Error de sintaxis en la instrucción: " + linea);
                    return;
                }

                // Escribimos en el archivo solo si es una instrucción válida (A o C)
                if (instruccionBinaria != null) {
                    writer.println(instruccionBinaria);
                }
            }
        } catch (IOException e) {
            System.err.println("Error en Segunda Pasada: " + e.getMessage());
        }
    }
}