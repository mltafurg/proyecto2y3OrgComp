import java.io.*;

public class Desensamblador {
    // Reutilizamos los mismos datos de tu Traductor pero para el proceso inverso
    private final String[] destNombre = {"null", "M", "D", "MD", "A", "AM", "AD", "AMD"};
    private final String[] destBits  = {"000", "001", "010", "011", "100", "101", "110", "111"};

    private final String[] jumpNombre = {"null", "JGT", "JEQ", "JGE", "JLT", "JNE", "JLE", "JMP"};
    private final String[] jumpBits  = {"000", "001", "010", "011", "100", "101", "110", "111"};

    // Para el cómputo, usamos los 6 bits de control (c1...c6)
    private final String[] compNombre = {
        "0", "1", "-1", "D", "A", "!D", "!A", "-D", "-A", "D+1", "A+1", "D-1", "A-1", 
        "D+A", "D-A", "A-D", "D&A", "D|A", "D<<1", "D>>1", "A<<1", "A>>1"
    };
    private final String[] compBits = {
        "101010", "111111", "111010", "001100", "110000", "001101", "110001", "001111", "110011", "011111", "110111", "001110", "110010",
        "000010", "010011", "000111", "000000", "010101", "000001", "000011", "000001", "000011"
    };

    // Método de búsqueda inversa en arreglos
    private int buscarIndice(String[] arregloBits, String bitsBuscados) {
        for (int i = 0; i < arregloBits.length; i++) {
            if (arregloBits[i].equals(bitsBuscados)) return i;
        }
        return -1; 
    }

    public void ejecutar(String archivoEntrada) {
        String archivoSalida = archivoEntrada.replace(".hack", "Dis.asm");
        File salidaFile = new File(archivoSalida);

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoEntrada));
             PrintWriter writer = new PrintWriter(new FileWriter(salidaFile))) {

            String linea;
            int numLinea = 1;

            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) { numLinea++; continue; }

                // VALIDACIÓN: 16 bits y solo 0 o 1
                if (linea.length() != 16 || !linea.matches("[01]+")) {
                    System.err.println("Error de sintaxis en la línea " + numLinea + ": " + linea);
                    writer.close();
                    salidaFile.delete(); // Borramos el archivo si hay error
                    return;
                }

                String resultadoASM = "";

                // --- TIPO A ---
                if (linea.startsWith("0")) {
                    int valorDecimal = Integer.parseInt(linea.substring(1), 2);
                    resultadoASM = "@" + valorDecimal;
                } 
                // --- TIPO C ---
                else if (linea.startsWith("1")) {
                    String b14 = linea.substring(1, 2);
                    String aBit = linea.substring(3, 4);
                    String cPart = linea.substring(4, 10);
                    String dPart = linea.substring(10, 13);
                    String jPart = linea.substring(13, 16);

                    int idxD = buscarIndice(destBits, dPart);
                    int idxJ = buscarIndice(jumpBits, jPart);

                    if (idxD == -1 || idxJ == -1) {
                        System.err.println("Error en la línea " + numLinea + ": Bits de instrucción no reconocidos.");
                        writer.close(); salidaFile.delete(); return;
                    }

                    String dest = destNombre[idxD];
                    String jump = jumpNombre[idxJ];
                    String comp;

                    // Caso especial: shifts (000001 = <<1, 000011 = >>1)
                    // El bit 'a' determina el registro: 0 = D, 1 = M
                    if (cPart.equals("000001") || cPart.equals("000011")) {
                        String direccion = cPart.equals("000001") ? "<<1" : ">>1";
                        String registro  = (b14.equals("0")) ? "D" : (aBit.equals("1") ? "M" : "A");
                        comp = registro + direccion;
                    } else {
                        int idxC = buscarIndice(compBits, cPart);
                        if (idxC == -1) {
                            System.err.println("Error en la línea " + numLinea + ": Bits de instrucción no reconocidos.");
                            writer.close(); salidaFile.delete(); return;
                        }
                        comp = compNombre[idxC];
                        // Si el bit 'a' es 1, cambiamos las 'A' por 'M'
                        if (aBit.equals("1")) {
                            comp = comp.replace('A', 'M');
                        }
                    }

                    if (!dest.equals("null")) resultadoASM += dest + "=";
                    resultadoASM += comp;
                    if (!jump.equals("null")) resultadoASM += ";" + jump;
                }

                writer.println(resultadoASM);
                numLinea++;
            }
        } catch (IOException e) {
            System.err.println("Error al leer/escribir: " + e.getMessage());
        }
    }
}