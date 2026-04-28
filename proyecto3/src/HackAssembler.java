public class HackAssembler {
    public static void main(String[] args) {
        
        if (args.length == 0) {
            System.out.println("Uso para Ensamblar: java HackAssembler Prog.asm");
            System.out.println("Uso para Desensamblar: java HackAssembler -d Prog.hack");
            return;
        }

        // --- MODO DESENSAMBLADOR ---
        if (args[0].equals("-d")) {
            if (args.length < 2) {
                System.err.println("Error: Falta el nombre del archivo .hack");
                return;
            }
            String archivoHACK = args[1];
            Desensamblador des = new Desensamblador();
            des.ejecutar(archivoHACK);
        } 
        
        // --- MODO ENSAMBLADOR ORIGINAL ---
        else {
            String archivoASM = args[0];
            String archivoHACK = archivoASM.replace(".asm", ".hack");

            Archivo archivo = new Archivo();
            tablaSimbolos tabla = new tablaSimbolos();
            Traductor traductor = new Traductor();

            // Primera pasada: etiquetas
            archivo.primeraPasada(archivoASM, tabla);
            // Segunda pasada: traducción
            archivo.segundaPasada(archivoASM, archivoHACK, tabla, traductor);
            
            System.out.println("Ensamblaje finalizado con éxito.");
        }
    }
}