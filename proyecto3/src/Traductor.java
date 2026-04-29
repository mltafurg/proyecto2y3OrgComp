
/*********
* Traductor.java – Esta clase tiene el proposito de traducir las inst A y C
a binario. 
como?
 para la inst C con el uso de arreglos: cada parte de la instruccion (dest, comp y jump)
tiene su arreglo con los simbolos del hack y otro arreglo con esos simbolos en binario.
 y para la inst A usamos la clase
tablaSimbolos para las etiquetas y variables (buscamos si estan en la tabla) y el metodo Integer.toBinaryString para convertir a binario los numeros.

* Autor 1: Maria Laura Tafur Gomez

*********/





public class Traductor {
    // Tablas para dest (3 bits)
    private final String[] destNombre = {"null", "M", "D", "MD", "A", "AM", "AD", "AMD"};
    private final String[] destBits  = {"000", "001", "010", "011", "100", "101", "110", "111"};

    // Tablas para jump (3 bits)
    private final String[] jumpNombre = {"null", "JGT", "JEQ", "JGE", "JLT", "JNE", "JLE", "JMP"};
    private final String[] jumpBits  = {"000", "001", "010", "011", "100", "101", "110", "111"};

    //  Tablas para comp (6 bits)
    // Important!  solo se incluyen los 6 bits de comp, el bit 'a' se consigue por separado
    // debido a que tenemos que ver si M esta en el comp o no para asignar ese valor a 'a'
    private final String[] compNombre = {
        "0", "1", "-1", "D", "A", "!D", "!A", "-D", "-A", "D+1", "A+1", "D-1", "A-1", 
        "D+A", "D-A", "A-D", "D&A", "D|A", "M", "!M", "-M", "M+1", "M-1", "D+M", "D-M", 
        "M-D", "D&M", "D|M", "D<<1", "A<<1", "M<<1", "D>>1", "A>>1", "M>>1"
    };

    private final String[] compBits = {
        "101010", "111111", "111010", "001100", "110000", "001101", "110001", "001111", "110011", "011111", "110111", "001110", "110010",
        "000010", "010011", "000111", "000000", "010101", "110000", "110001", "110011", "110111", "110010", "000010", "010011", "000111", 
        "000000", "010101", "000001", "000001", "000001", "000011", "000011", "000011"
    };

    // Buscamos con este metodo el indice del simbolo en el arreglo de nombres
    // para luego usar ese valor para buscarlo en el arreglo de binario, important! pues tienen el mismo orden
    private int getIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return -1; // No encontrado el indice (para verificar si hay errores en el arhivo de entrada)
    }

    public String traducirC(String dest, String comp, String jump) {
        // usamos el metodo anterior para buscar el inidice de cada parte de la inst C
        int dIdx = getIndex(destNombre, dest);
        int jIdx = getIndex(jumpNombre, jump);
        int cIdx = getIndex(compNombre, comp);

        if (dIdx == -1 || jIdx == -1 || cIdx == -1) {
            System.err.println("Error: instrucción C no reconocida — dest=" + dest + " comp=" + comp + " jump=" + jump);
            return null;  // como get index devuelve -1 si no encuentra el simbolo, usamos este valor como
            // condicion para verificar si hay errores en el archivo.
        }

        // Construcción del binario final:
        // 111 (Cabecera) + a (Bit de memoria) + cccccc (Comp) + ddd (Dest) + jjj (Jump)
        
        String inicio = "1"; // siempre en una inst C el inicio es 111 pero como el bit 14 lo usamos para 
        // decidir si es A o M, entonces el inicio es solo 1 y el bit 14 se asigna despues dependiendo del comp.
        String bit13 = "1";

        String bit14;
        String aBit; 
         //LOGICA DEL BIT 14
       // 1. Desplazamientos: Si es A o M, bit14=1. Si es D, bit14=0.
        if (comp.contains("A<<") || comp.contains("M<<") || comp.contains("A>>") || comp.contains("M>>")) {
            bit14 = "1";
        } else if (comp.contains("D<<") || comp.contains("D>>")) {
            bit14 = "0";
        } 
        // 2. Operaciones normales donde A o M se necesitan aislar del D
        else if (!comp.contains("D") && (comp.contains("A") || comp.contains("M"))) {
            // Ejemplo: M=A, A=M, M=M+1. Aquí no necesitamos a D, y si ponemos bit14=1, A/M entra a la ALU.
            bit14 = "1";
        } 
        // 3. Todo lo demás (D+M, D=A, saltos, 0, 1, -1) usa D por defecto
        else {
            bit14 = "0";
        }
        
        if (comp.contains("M")) {
            aBit = "1";
        } else {
            aBit = "0";
        }

        String parteC = compBits[cIdx];
        String parteD = destBits[dIdx];
        String parteJ = jumpBits[jIdx];

        return inicio + bit14 + bit13 + aBit + parteC + parteD + parteJ;
    }

    public String traducirA(String symbol, tablaSimbolos table) {
    int value;
    
    // 1. ¿Es un número o un símbolo?
    if (symbol.matches("\\d+")) { 
        // Es un número (ej: @123)
        value = Integer.parseInt(symbol);
    } else {
        // Es un símbolo (ej: @LOOP), buscamos su dirección en la tabla
        value = table.getAddress(symbol);
    }

    // 2. Convertir a binario de 15 bits
    String binary = Integer.toBinaryString(value);
    
    // 3. Rellenar con ceros a la izquierda para completar 16 bits (0 + 15 bits)
    return String.format("%16s", binary).replace(' ', '0');
}

}