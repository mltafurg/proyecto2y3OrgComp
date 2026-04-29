// Programa: Prueba de sumas con desplazamiento de A
@SP          
D=A
@R1          
M=D
@1           
D=A
@temp        
M=D

(LOOP)       
@temp
D=M
@R1
M=D+M        // R1 = R1 + temp

@temp
D=M
@temp
M=D<<1       // Desplazamiento de M (Bit 14=1, a=1)

@2           // Cargamos un valor en A
A=A<<1       // PRUEBA DE SHIFT EN A: A ahora debería ser 4 (Bit 14=1, a=0)
D=A          // Pasamos el resultado a D para verificarlo

@LOOP
D;JGT        // Si A desplazado (D) > 0, sigue el bucle

(END)
@END
0;JMP