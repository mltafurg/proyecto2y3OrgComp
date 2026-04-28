# Guía de Usuario: Hack Assembler

### Requisitos para compilar el proyecto

deben tener:

● Java Runtime Environment (JRE) o Java Development Kit (JDK) instalado. 

● Terminal de comandos (PowerShell, CMD o Bash).

● Los archivos compilados del proyecto (.class).


###  Compilación del Proyecto

Antes de utilizar la herramienta, asegúrese de compilar todas las clases de Java en el directorio
src:

javac HackAssembler.java


### Modo Ensamblador (ASM a HACK)

Este modo traduce un archivo de lenguaje ensamblador (.asm) a un archivo binario ejecutable
(.hack).

Uso:

java HackAssembler nombre_del_archivo.asm

###  Modo Desensamblador (HACK a ASM)

Realiza el proceso inverso, convirtiendo código binario (.hack) en código mnemónico legible
(.asm).

Uso:

java HackAssembler -d nombre_del_archivo.hack


Importante para tener en cuenta: 

● el uso de "-d" es obligatorio
● Los símbolos originales y comentarios no se recuperan, se usan direcciones numéricas
(ejemplo: @16 en lugar de @variable).
