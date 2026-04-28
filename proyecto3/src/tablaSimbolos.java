
/*********
* tablaSimbolos.java – Esta clase se utiliza para representar la tabla de simbolos del HACk, donde desde la
posicon 0 a la 15 se dan los registros y desde la 16 las variables.
Lo importante es que tenemos dos arreglos dinamicos, uno para los nombres de los simbolos
y otro para las direcciones correspondientes, cada vez que se agrega un simbolo nuevo, es decirr,
aquel que no esta en la tabla, (no es conocido en la HACK)
se le asigna la siguiente direccion disponible que seria a partir de la 16.
Y se guarda esa relacion para posibles busquedas en el codigo. 




* Autor 1: Maria Laura Tafur Gomez
*********/



import java.util.ArrayList;

public class tablaSimbolos {
   


    private ArrayList<String> nombres = new ArrayList<>();
    private ArrayList<Integer> direcciones = new ArrayList<>();
    private int sigVariableDireccion = 16; // Las variables en Hack empiezan en la RAM 16

    public tablaSimbolos() {
        // Símbolos predefinidos del sistema Hack
        addEntry("SP", 0);
        addEntry("LCL", 1);
        addEntry("ARG", 2);
        
        // Registros R0 a R15
     for (int i = 0; i <= 15; i++) {
        addEntry("R" + i, i);
     }
    
     // Periféricos
     addEntry("SCREEN", 16384);
     addEntry("KBD", 24576);
    }

    public void addEntry(String symbol, int address) {
        nombres.add(symbol);
        direcciones.add(address);
    }

    public int getAddress(String symbol) {
        int index = nombres.indexOf(symbol);
        if (index != -1) {
            return direcciones.get(index);
        }
        
        // Si no existe, es una variable nueva
        addEntry(symbol, sigVariableDireccion);
        return sigVariableDireccion++;
    }
}

