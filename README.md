# Proyecto Organizacion de computadores


## Proyecto 2
**ALU, CPU y circuitos de soporte**

---

## Visión general — Cómo se conectan los cinco chips

El flujo de datos es un ciclo continuo:

1. La **ROM** le dice a la **CPU** qué instrucción ejecutar.
2. La **CPU** la procesa usando la **ALU** (que internamente puede usar el **Shifter**).
3. Si la instrucción necesita leer o escribir datos, la **CPU** se comunica con la **Memory**.
4. La **Memory** devuelve el dato leído a la **CPU** para el siguiente cálculo.
5. La **CPU** decide cuál es la próxima instrucción y apunta de nuevo a la **ROM**.

---

## Chip 1 — Shifter

**Archivo:** `Shifter.hdl`

### ¿Qué hace?

Toma un número de 16 bits y lo desplaza un bit hacia la izquierda o hacia la derecha, dependiendo de una señal de control llamada `direction`.

- **Desplazar a la izquierda** equivale a multiplicar el número por 2.
- **Desplazar a la derecha** equivale a dividir el número por 2.

### Entradas y salidas

| Señal | Tipo | Descripción |
|---|---|---|
| `in[16]` | Entrada | El número de 16 bits a desplazar |
| `direction` | Entrada | 0 = izquierda, 1 = derecha |
| `out[16]` | Salida | El número ya desplazado |
| `result` | Salida | El bit que "cayó" por el extremo al desplazar |

### Cómo funciona por dentro

Para el shift izquierdo: sumar un número consigo mismo produce exactamente el mismo resultado que desplazarlo a la izquierda, al sumarse consigo mismo el carry sirve para desplazar los bits. 

```
in = 0000 0000 0000 0011  (= 3)
in + in = 0000 0000 0000 0110  (= 6)  ← ¡es un shift izquierdo!
```

Para el shift derecho se "recablean" los bits: el bit 1 pasa a la posición 0, el bit 2 pasa a la posición 1, y así sucesivamente. El bit más significativo (posición 15) queda siempre en 0.

Después, un `Mux16` elige cuál de los dos resultados entregar según `direction`.

El bit `result` captura qué bit "salió volando" por el extremo:
- En shift izquierdo: el bit 15 (el más significativo) es el que se pierde.
- En shift derecho: el bit 0 (el menos significativo) es el que se pierde.

### Ejemplo visual

```
Shift IZQUIERDA (direction=0):
  Entrada:  1000 0000 0000 0001
  Salida:   0000 0000 0000 0010
  result:   1  ← el bit 15 que salió

Shift DERECHA (direction=1):
  Entrada:  1000 0000 0000 0001
  Salida:   0100 0000 0000 0000
  result:   1  ← el bit 0 que salió
```

---

## Chip 2 — ALU (Unidad Aritmético-Lógica)

**Archivo:** `ALU.hdl`

### ¿Qué hace?

 Recibe dos números de 16 bits (`x` e `y`) y seis señales de control que le dicen qué operación realizar. El resultado puede ser una suma, un AND lógico, una negación, un desplazamiento, y muchas combinaciones más.

### Entradas y salidas

| Señal | Tipo | Descripción |
|---|---|---|
| `x[16]`, `y[16]` | Entrada | Los dos operandos de 16 bits |
| `zx` | Control | Si es 1, fuerza `x` a cero antes de operar |
| `nx` | Control | Si es 1, niega `x` después de aplicar `zx` |
| `zy` | Control | Si es 1, fuerza `y` a cero antes de operar |
| `ny` | Control | Si es 1, niega `y` después de aplicar `zy` |
| `f`  | Control | Si es 0: AND. Si es 1: SUMA. En modo shift: 0=izquierda, 1=derecha |
| `no` | Control | Si es 1, niega el resultado final. En modo shift: activa el Shifter |
| `out[16]` | Salida | El resultado de la operación |
| `zr` | Salida | 1 si el resultado es cero, 0 si no |
| `ng` | Salida | 1 si el resultado es negativo, 0 si no |
| `result` | Salida | El bit que salió del Shifter (cuando se hace shift) |

### Cómo funciona por dentro

La ALU tiene tres etapas que procesan los datos, y una cuarta que decide si el resultado es un shift o no. **Todas las etapas corren al mismo tiempo** (en paralelo), y al final se elige el resultado correcto.

```
     x[16] ──[zx]──[nx]──► x2 ──────────────────────────────► Shifter
                                  │                                │
     y[16] ──[zy]──[ny]──► y2    ├──[AND]──►                      │
                                  └──[ADD]──► [Mux f] ──[Mux no]  │
                                                                    │
                                             [Mux allowsShift] ◄───┘
                                                    │
                                                  out[16]
```

**Etapa 1 — Preparar x:**
Se aplica `zx` (pone x en cero si es 1) y luego `nx` (niega el resultado si es 1). El resultado es `x2`.

**Etapa 2 — Preparar y:**
Lo mismo con `y`: primero `zy`, luego `ny`. El resultado es `y2`.

**Etapa 3 — Calcular:**
Se calculan `x2 AND y2` y `x2 + y2` al mismo tiempo. El bit `f` elige cuál usar.
Luego `no` decide si negar ese resultado o dejarlo igual → esto produce `outNo`.

**Etapa 4 — ¿Shift o resultado normal?**
El Shifter siempre está corriendo con `x2` como entrada. La ALU detecta si está en "modo shift" comprobando que `zx=nx=zy=ny=0` y `no=1`. Si se cumple, un `Mux16` final elige la salida del Shifter en vez de `outNo`.

### ¿Por qué el Shifter recibe `x2` y no el resultado final?

Porque el Shifter corre **en paralelo** con el resto del cálculo, no después. El bit `no=1` en modo shift **no significa "negá el resultado y luego shiftéalo"** — simplemente es la señal que le dice a la ALU "estás en modo shift, ignorá el resultado de la ALU normal y usá el Shifter". El `x2` cuando `zx=nx=0` es exactamente igual al registro de entrada original (D o A/M), que es precisamente lo que se quiere desplazar.

### Tabla de operaciones más comunes

| zx | nx | zy | ny | f | no | Resultado |
|---|---|---|---|---|---|---|
| 1 | 0 | 1 | 0 | 1 | 0 | 0 |
| 1 | 1 | 1 | 1 | 1 | 1 | 1 |
| 0 | 0 | 0 | 0 | 0 | 0 | x AND y |
| 0 | 0 | 0 | 0 | 1 | 0 | x + y |
| 0 | 0 | 0 | 0 | 0 | **1** | **Shift left** (via Shifter) |
| 0 | 0 | 0 | 0 | 1 | **1** | **Shift right** (via Shifter) |

---

## Chip 3 — Memory

**Archivo:** `Memory.hdl`

### ¿Qué hace?

Gestiona todo el espacio de memoria del computador Hack: la RAM donde viven los datos, la pantalla, y el teclado. Desde el punto de vista de la CPU, todo parece una sola memoria grande — la Memory se encarga de redirigir cada acceso al lugar correcto según la dirección.

### Mapa de memoria

```
Dirección       Dispositivo
──────────────────────────────
0x0000 – 0x3FFF  RAM de 16K  (datos del programa)
0x4000 – 0x5FFF  Pantalla    (cada bit = un píxel)
0x6000           Teclado     (código de la tecla presionada)
> 0x6000         Inválido    (devuelve 0)
```

### Cómo decide a dónde ir

La Memory usa los bits más significativos de la dirección como "pista" para saber a qué región apuntar:

- **Bit 14 = 0** → Es RAM (dirección menor a 0x4000).
- **Bit 14 = 1, Bit 13 = 0** → Es la pantalla (0x4000–0x5FFF).
- **Bit 14 = 1, Bit 13 = 1** → Podría ser el teclado. Pero solo si los demás 13 bits son todos 0 (es exactamente 0x6000). Para verificar esto, se hace un OR de todos esos bits: si alguno es 1, la dirección es inválida y se devuelve 0.

Dos `DMux` separan la señal de escritura (`load`) para que solo llegue al dispositivo correcto (no tendría sentido "escribir" en el teclado, por ejemplo).

---

## Chip 4 — CPU

**Archivo:** `CPU.hdl`

### ¿Qué hace?

Es el cerebro del computador. Recibe una instrucción de 16 bits, la interpreta, y la ejecuta. Maneja dos registros internos (A y D), se comunica con la Memory, y controla qué instrucción ejecutar a continuación.

### Entradas y salidas

| Señal | Tipo | Descripción |
|---|---|---|
| `inM[16]` | Entrada | Dato leído desde la Memory |
| `instruction[16]` | Entrada | La instrucción a ejecutar |
| `reset` | Entrada | Si es 1, reinicia el programa desde el principio |
| `outM[16]` | Salida | Dato a escribir en la Memory |
| `writeM` | Salida | Si es 1, la Memory debe guardar `outM` |
| `addressM[15]` | Salida | Dirección de memoria a leer o escribir |
| `pc[15]` | Salida | Dirección de la próxima instrucción |

### Los dos tipos de instrucción

**Instrucción tipo A** (bit 15 = 0):  
Carga un valor numérico en el registro A. Ejemplo: `@42` carga el número 42.

**Instrucción tipo C** (bit 15 = 1):  
Especifica una operación de la ALU, dónde guardar el resultado, y si saltar a otra instrucción.  
Formato: `111 a cccccc ddd jjj`

### Cómo funciona por dentro

**1. Decodificación:**  
La instrucción entra a un `Add16` sumando cero (truco para extraer bits individuales sin modificar el valor). Así se obtienen: `selMSB` (tipo A o C), los bits de control de la ALU (`zx nx zy ny f no`), los bits de destino (`RegA RegD mWrite`), y los bits de salto (`j1 j2 j3`).

**2. Registro A:**  
Puede recibir dos cosas: un número de una instrucción tipo A, o el resultado de la ALU (cuando la instrucción tipo C especifica `A` como destino). Un `Mux16` elige cuál de las dos cargar.

**3. Registro D:**  
Solo guarda el resultado de la ALU cuando la instrucción tipo C lo indica (`RegD=1`).

**4. Selección del operando Y para la ALU:**  
El bit `a` de la instrucción decide si la ALU opera con el valor del registro A o con el valor de memoria `M` (que es `RAM[A]`).

**5. Selección del operando X para shifts:**  
El bit 14 de la instrucción (`selAorD`) decide si el operando que se desplaza es D (el registro) o A/M. Esto es necesario porque en la instrucción `D=M<<1`, la ALU necesita recibir M (no D) como el valor a shiftear.

**6. Lógica de salto:**  
Para cada tipo de salto (JGT, JEQ, JGE, JLT, JNE, JLE, JMP) se verifica que los bits del salto estén presentes **y** que las flags de la ALU (`zr`, `ng`) confirmen la condición. Si algún salto se activa, el PC carga la dirección del registro A. Si ninguno se activa, el PC simplemente incrementa en 1.

```
¿Hay salto?
  SÍ → PC = registro A  (salta a esa dirección)
  NO → PC = PC + 1      (siguiente instrucción)
  reset=1 → PC = 0      (reinicia el programa)
```

---

## Chip 5 — Computer

**Archivo:** `Computer.hdl`

### ¿Qué hace?

Es el nivel más alto: conecta la CPU, la Memory y la ROM en un solo chip. No tiene lógica propia — su único trabajo es cablear los tres componentes correctamente.

### La conexión es un ciclo cerrado

- La **ROM32K** usa el `pc` que sale de la CPU para saber qué instrucción mostrar.
- La **CPU** recibe esa instrucción, la ejecuta, y produce el resultado.
- Si el resultado debe guardarse en memoria, `outM` y `writeM` se lo indican a la **Memory**.
- La **Memory** devuelve el dato almacenado en `addressM` de vuelta a la CPU como `inM`.
- La CPU actualiza el `pc` y el ciclo comienza de nuevo.

La única entrada externa es `reset`: cuando es 1, el PC se reinicia a 0 y el programa empieza desde el principio.

---

## Relaciones entre los cinco chips

```
Shifter
  └── Es usado por: ALU
        └── Es usado por: CPU
              ├── Usa: Memory
              └── Es conectada por: Computer
                    └── También conecta: ROM32K y Memory
```

El `Shifter` no depende de nadie. La `ALU` lo incorpora para agregar capacidad de desplazamiento. La `CPU` usa la `ALU` como su motor de cálculo. La `Memory` es el banco de datos. Y el `Computer` es el integrador que une todo en un sistema funcional.



## Proyecto 3
