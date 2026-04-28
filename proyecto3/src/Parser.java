/*********
* Parser.java – Esta clase se encarga de verificar que las lineas de archivo de entrada
si cumplan con las reglas de las instrucciones A, C y las etiquetas que son llamadas L en el codigo.
como? 
usando expresiones regulares,cada instruccion del assembly tiene su propio patron y 
cada linea se compara con esos patrones para determinar su tipo, y dependiendo de eso se separa la linea en partes
si es inst C en :  dest, comp y jump. Si es inst A o L se extrae  la parte despues de @ o entre parentesis

* Autor 1: Maria Laura Tafur Gomez
*********/
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

  


    // 1. Definición de los moldes (Regex) - Usamos tus versiones pulidas
    private static final Pattern REGEX_A = Pattern.compile("^@([a-zA-Z0-9_.$:]+)$");
    private static final Pattern REGEX_L = Pattern.compile("^\\(([a-zA-Z0-9_.$:]+)\\)$");
    
    // Instrucción C: Destino opcional, Cómputo obligatorio (con shifts), Salto opcional
    private static final Pattern REGEX_C = Pattern.compile(
        "^(?:([AMD]+)=)?([ADM][<>]{2}1|[ADM][\\-\\+\\|&][ADM1]|[01]|-1|[-!]?[ADM])(?:;(J(?:MP|LE|LT|GE|EQ|GT|NE)))?$"
    );

    private String lineaActual;
    private Matcher matcherA, matcherL, matcherC;
    private char tipoInstruccion;

    public Parser(String linea) {
        this.lineaActual = linea;
        this.determinarTipo();
    }

    private void determinarTipo() {
        matcherA = REGEX_A.matcher(lineaActual);
        matcherL = REGEX_L.matcher(lineaActual);
        matcherC = REGEX_C.matcher(lineaActual);

        if (matcherA.find()) {
            tipoInstruccion = 'A';
        } else if (matcherL.find()) {
            tipoInstruccion = 'L';
        } else if (matcherC.find()) {
            tipoInstruccion = 'C';
        } else {
            tipoInstruccion = 'E'; // Error de sintaxis
        }
    }

    public char getTipo() {
        return tipoInstruccion;
    }

    // --- Métodos de extracción ---

    // Para Instrucción A y Etiquetas (L)
    public String getSymbol() {
        if (tipoInstruccion == 'A') return matcherA.group(1);
        if (tipoInstruccion == 'L') return matcherL.group(1);
        return null;
    }

    // Para Instrucción C
    public String getDest() {

        if (tipoInstruccion == 'C' && matcherC.group(1) != null) {
            return matcherC.group(1);    
         } else { 
             return "null"; // Si no hay destino, devolvemos "null" como string   

         }
    }

    public String getComp() {
        if (tipoInstruccion == 'C') {
            return matcherC.group(2);

        }else{
            return null;
        }
       
    }

    public String getJump() {
        if (tipoInstruccion == 'C' && matcherC.group(3) != null) {
            return matcherC.group(3);
        } else {
            return "null"; // Si no hay salto, devolvemos "null" como string
        }
    }
}
    

