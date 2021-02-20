# Super Abuela

_Servicio de Accesibilidad (AccessibilityService) pensado para funcionar con un teclado númerico conectado mediante USB._

_Este Servicio proporciona un control simplificado de la aplicación Kindle para personas mayores y con destreza motora reducida. Habilita varias tecla que simplifica los
comandos más comunes para permitir una experiencia de lectura básica en la App de Kindle._

_A las personas mayores les resulta imposible moverse entre opciones y menús de una aplicación de la sofisticación de Kindle, entender las diferentes pantallas y a veces no pueden ni ejecutar los más simples gestos de desplazamiento de ventana
 a los que estamos acostumbrados_.

Recuerda **activar** el servicio en el menú de Accesibilidad. Y darle permisos al servicio para que actue en nombre del usuario.

## Funcionalidad  
La persona que quiera leer en Kindle sólo se tiene que preocuparse de pulsar la Tecla [-]. Y usar las teclas [4] y [6] para pasar páginas.

Si se le ha perdido la página, usar la función 'Saltar a página' para volver rápidamente a la página que recuerde.

Dejando a servicio 'Super Abuela' que le abstraiga de las complejidades de Android y del Kindle.


## Comandos
Funcionalidad asignada a cada tecla del teclado numérico:
- Tecla [4] del teclado
  - **Página anterior**. Realiza el gesto '_desplazar a la derecha_'.
- Tecla [6] del teclado
  - **Página siguiente**. Realiza el gesto '_desplazar a la izquierda_'.
- Tecla [*] del teclado
  - **Configura el libro activo**.
  - Este comando sólo funciona si en el momento de pulsarlo el usuario está en la aplicación de Kindle, con un libro abierto.
  El titulo del libro abierto en el momento de pulsar, se guarda en las preferencias para recordarlo como _libro de lectura_.
  - El _libro de lectura_ será el libro que se use cuando se abra la App de Kindle con el teclado a partir de ahora.
- Tecla [-] del teclado
  - **Abrir Kindle**.
  - Atajo para abrir Kindle. Se asegura de que el App de Kindle acabe mostrando el libro configurado en _libro de lectura_.
  - Este comando funciona desde cualquier parte donde esté el usuario.
  - Si se abre en biblioteca, abre el libro para el usuario.
  - Si se abre con un libro abierto. Se asegura que sea el libro configurado, y en caso de no serlo, cierra el libro actual y buca el libro configurado en la biblioteca para terminar abriéndolo.
  - El objetivo es que la persona no tenga que preocuparse de buscar el libro que está leyendo, independientemente de como estuviera la App de Kindle.
  - Tened en cuenta que ejecuta los movimientos de forma automática como si lo hiciera el usuario. El servicio configura un tiempo de esperar entre comando y comando
  hasta asegurarse de que se a abierto la siguiente opción ó página. En algunos casos el tiempo puede ser de hasta 10 segundos. Lo importante es que a la persona mayor se simplifique las variantes 
  y la complejidad de la App de Kindle.
- Tecla [Enter] del teclado
  - **Iniciar el reconocimiento de ordenes de Voz**.
  - Al pulsar el [Enter] se pueden ejecutar los comandos anteriores por medio de voz.
  - En la próxima mejora, intentaré que no haga falta pulsar [Enter], reconocerá a '_Conchi_' como inicio del reconocimiento de ordenes. 
 
 ## Ordenes por Voz
 _Ordenes de voz reconocidas por el servicio de accessibilidad:_
- '_**página siguiente**_', misma funcionalidad que la tecla [6]
  - También acepta variaciones como '_adelante_', _siguiente'_
- '_**página anterior**_', misma funcionalidad que la tecla [4].
  - También acepta variaciones como '_atrás_', '_anterior_'
- A los comandos anteriores se le puede añadir un número si se quiere **repetir**, por ejemplo:
```
las 3 páginas siguientes
```
_para avanzar 3 páginas seguidas_

- '_**Saltar a página**_', implementa la funcionalidad 'Ir a' del menú del Kindle.
  - Este comando ejecuta la misma secuencia de menús que tendría que hacer un usuario. Es decir, sacar el menú, abrir el menú lateral, pulsar la opción 'Ir a ...', meter el número 
  de la página en el el pop-up y pulsar la opción 'PÁGINA'
  - Tened en cuenta que ejecuta estos movimientos de forma automática y debe esperar entre comando y comando hasta asegurarse de que se a abierto la página.
  -  Ejemplo:

```
Saltar a la página 25
```

  
  Y creo que no se me olvida nada más.
  
