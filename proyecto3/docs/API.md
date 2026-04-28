# Documentacion clases

### 1.  Clase HackAssembler
Es la clase principal que maneja el flujo de trabajo.

● Responsabilidad: Gestionar los argumentos de la terminal y decidir si el programa debe
actuar como ensamblador o desensamblador.

● Flujo: Instancia a las clases Archivo, tablaSimbolos y Traductor para coordinar las
pasadas de traducción.

### 2.  Clase Archivo
Encargada del procesamiento del texto de entrada

● Responsabilidad: Leer archivos .asm/.hack y escribir los resultados finales. ademas de quitar espacios y comentarios.


● Métodos Clave:

○ primeraPasada(): Recorre el archivo para llenar la tabla de símbolos con etiquetas.

○ segundaPasada(): Realiza la traducción línea por línea y genera el archivo .hack.

### 3. Clase Parser

Analiza el texto de la entrada y verifica su sintaxis

● Responsabilidad: Utilizar expresiones regulares (Regex) para descomponer cada línea
de assembly en sus componentes (dest, comp, jump).

● Lógica: Clasifica cada instrucción en tipo A (Dirección), C (Cálculo) o L (Etiqueta).


### 4. Clase tablaSimbolos

Almacena las direcciones predeterminadas del hack y las nuevas del codigo.

● Responsabilidad: Almacenar y recuperar pares (nombre, dirección). Maneja símbolos
predefinidos (SP, R0-R15), etiquetas y variables dinámicas.

● Manejo de variables: Si un símbolo no existe, le asigna automáticamente una dirección

a partir de la RAM 16.

### 5. Clase Traductor

Convierte las intrucciones en binario

● Responsabilidad: Convertir los componentes extraídos por el Parser en cadenas de 16
bits (0 y 1).

● Tablas internas: Contiene los diccionarios de bits para cada operación de cómputo,
destino y salto de la arquitectura Hack.

### 6. Clase Desensamblador

Vuelve a retomar el binario a codigo asm

● Responsabilidad: Toma archivos .hack y reconstruye el código mnemónico original
(sustituyendo etiquetas por direcciones numéricas).
