# Changelog: Proyecto de Arquitectura de Computadores (Hack System)


---

## [Proyecto 2] - Arquitectura de Hardware

### 1. Extensión de la ALU (`ALU.hdl`) 18-04-026
* **Implementación de Shifters:** Se integró la capacidad de realizar desplazamientos de bits (*shifts*) directamente en la Unidad Aritmético-Lógica.
* **Lógica de Activación (`allowsShift`):** Se añadió un circuito interno que detecta la combinación específica de bits de control (`zx=nx=zy=ny=0` y `no=1`) para activar el desplazamiento.
* **Flags de Salida:** Se agregó la salida `result` para capturar el bit desbordado del Shifter, permitiendo futuras operaciones de precisión.

### 2. Creación del Componente Shifter (`Shifter.hdl`) 19-04-26
* **Shift Left (x2):** Implementado mediante una operación de suma (`Add16`) del número consigo mismo.
* **Shift Right (/2):** Implementado mediante un recableado manual de los bits (`in[1..15]` hacia `out[0..14]`) inyectando un `0` constante en el bit más significativo (MSB).
* **Selector de Dirección:** Uso de un `Mux16` controlado por el bit `f` para elegir entre izquierda (0) o derecha (1).

### 3. Rediseño de la CPU (`CPU.hdl`) 26-04-26
* **Selector de Entrada Personalizado (Bit 14):** Se modificó el decodificador de instrucciones para usar el **Bit 14** (`instruction[14]`) como señal `selAorD`.
* **Multiplexor de Entrada ALU:** Se añadió un `Mux16` antes de la entrada `x` de la ALU.
    * **Bit 14 = 0:** Selecciona el **Registro D**.
    * **Bit 14 = 1:** Selecciona el **Registro A** (o valor de memoria M).
* **Control de Saltos:** Se refinó la lógica de comparación para que los saltos condicionales evalúen correctamente la salida de la ALU basándose en el registro seleccionado por el nuevo bus.

---

## [Proyecto 3] - Software del Ensamblador

### 4. Actualización del Traductor (`Traductor.java`) 26-04-26
* **Corrección de Trama Binaria:** Se eliminó el error que generaba cadenas de 17 bits, garantizando instrucciones de exactamente **16 bits**.
* **Lógica Dinámica del Bit 14:** Se implementó un algoritmo de decisión para el segundo bit de la instrucción C:
    * **Valor 0:** Asignado para operaciones que requieren explícitamente a `D` (ej. `D+M`, `D<<1`) y para todas las instrucciones de **Salto** (`jump`).
    * **Valor 1:** Asignado para operaciones puras con `A` o `M` (ej. `A<<1`, `M=A`).
* **Soporte de Mnemónicos:** Inclusión de nuevos símbolos en el diccionario de cómputo: `D<<1`, `D>>1`, `A<<1`, `A>>1`, `M<<1`, `M>>1`.

### 5. Robustez del Parser (`Parser.java`) 27-04-26
* **Expresiones Regulares (Regex):** Actualización de patrones para reconocer el operador de asignación `=` y los nuevos operadores de desplazamiento `<<` y `>>`.
* **Manejo de Errores:** Implementación del tipo de instrucción `'E'` para capturar fallos de sintaxis en el código fuente `.asm`.

### 6. Gestión de Símbolos y Archivos (`Archivo.java` & `tablaSimbolos.java`) 27-04-26
* **Ensamblador de Dos Pasadas:**
    * **Primera Pasada:** Recorrido para identificar etiquetas `(LABEL)` y mapearlas a direcciones ROM sin generar código binario.
    * **Segunda Pasada:** Sustitución de etiquetas por direcciones reales y generación del archivo `.hack` final.
* **Asignación de Variables:** Implementación de la reserva automática de direcciones RAM para símbolos nuevos a partir de la dirección `16`.



### 7. Desarrollo del Desensamblador (`Desensamblador.java`) 27-04-26
* **Decodificación Inversa:** Capacidad de convertir archivos binarios `.hack` de vuelta a mnemónicos legibles `.asm`.
* **Detección de Arquitectura Custom:** El software reconoce si una instrucción fue un *shift* analizando el Bit 14 y el Bit 'a', manteniendo coherencia con el hardware modificado.

---
