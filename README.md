# ay-3-8910-midi
##Una interfaz en Python para el chip AY-3-8910 que usa Arduino.

Para poder enviar los archivos midi primero hay qeu tranformarlos en csv con este programa: https://www.fourmilab.ch/webtools/midicsv/#Download

(o cualquier otro que transforme .mid en .csv)

## Explicación de los archivos

### Carpeta *AY3910RegWrite (original para 1 chip solo conectado a Arduino)*

Como su nombre indica, en esta carpeta se encuentra el código para que funcione un solo chip conectado a una placa Arduino de la misma manera que se muestra en la imagen [Conexiones.png](https://github.com/DerSpanischGamer/ay-3-8910-midi/blob/master/Conexiones.png)

### Carpeta *[https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/AY3910RegWrite-library](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Amadeus)*

Código que soporta 1 o 2 chips simultáneos. Es una versión simplificada del código anterior, además de más facil de leer gracias a que el código ha sido transformado en [librería](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Amadeus).

### Carpeta *Amadeus*

Es la librería para hacer más fácil y limpio el código.

Las únicas funciones que hay que saberse son:

Importar la librería
`#include "Amadeus.h"`

Iniciar una instancia de la libería
`Amadeus amadeus = Amadeus(); // Iniciar la clase Amadeus`

Iniciar todo el reloj y configurar los pines
`amadeus.begin();`

Poner un valor VAL en el registro REG del chip C
`amadeus.out(C, REG, VAL);`

El resto de funciones son privadas ya que solo sirven para llevar a cabo las funciones que están aquí arriba. Si eres curioso echa un vistazo al código, aunque es literalmente lo mismo que el código de la primera carpeta.

### Carpeta *[Canciones](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Canciones)*

No hay mucho que decir, algunas canciones que he buscado yo el archivo .mid y lo he transforamdo en .csv para poder enviarlo.

### Carpeta *[Programas](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Programas)*

Boy oh boy, es aquí que empieza la diversión. Primero, los archivos .exe son medio a ignorar medio no.

- **Controlador Registros Dual YM2149.exe** : es un programa que estoy haciendo para controlar todos los registros de una manera visual. NO ESTÁ ACABADO Y TIENE ERRORES SEGURAMENTE.

- **Midicsv.exe** : Transforma un archivo .mid en .csv. Para usarlo se necesita la consola de comandos y el comando es `Midicsv.exe "ARCHIVO.mid" "OUTPUT.csv"`. Obviamente ARCHIVO es el nombre del archivo .mid que queremos transformar y OUTPUT es el nombre que queremos que tenga el archivo de salida.

- **bin2hex.exe** : A ignorar, no se ni lo que hace...

- **hexdump.exe** : Útil para hacer un hexdump desde la consola de comandos, pero de momento me es un poco inútil ya que envío los bytes directamente en vez de guardarlos, pero quién sabe, a lo mejor algún día me será útil.

Ok, llegamos a los archivos .py que son los interesantes:

- **amadeusguardar.py** : sirve para guardar una canción que ya está en .csv desde un midi en un archivo .amds (es un archivo .csv pero he puesto la extensión personalizada para no confundirme y que mole más jejeje).

Uso: `python amadeusguardar.py "ARCHIVO.csv" "SALIDA"`

Nota: no hace falta poner SALIDA.amds, aunque tambien se puede. Tambien se puede poner SALIDA.csv, aunque el archivo saldrá como SALIDA.amds. Para abrir los archivos .amds se puede cambiar la extensión a .csv o con click derecho > Abrir con > Notepad++ para los pros o Excel/Libreoffice Calc para los noobs.

- **amadeussender_legacy.py** : sirve para poder utilizar los dos chips de una [placa Amadeus](https://www.youtube.com/watch?v=V24AyQ2n8vY) o un Arduino que esté conectado de la misma manera. La diferencia entre este archivo y **midisender.py** es que este puede manejar **6** canales simultáneos, en vez de 3 tristes y solitarios canales.

Para usarlo: `python amadeussender_legacy.py "ARCHIVO.csv" "PUERTO"`

IMPORTANTE: los datos que envia este archivo son (en este orden):

* CHIP : ( 0 -> PRIMERO, 1 -> SEGUNDO ).

* REGISTRO : el registro del chip que queremos modificar ( 0 -> 16 ).

* VALOR : el valor que queremos escribir en el registro ( 0 -> 256).

Por lo que, si enviamos los bytes [0,1,2], tendremos el valor 2 en el registro 1 del chip (PRIMERO).

Para que funcione lo que envía, hay que instalar en el Arduino el código adecuado (el que no pone para 1 placa vamos).

- **amadeussender.py** : una versión mejorada de amadeussender_legacy.py que permite detectar diferentes pistas que tocarían simultaneamente. Se obtiene pasando por todo el archivo .csv y una vez que se tienen todas las notas, se ordenan por tiempo, y después es todo igual a amadeussender.py

Para usarlo: `python amadeussender.py "ARCHIVO.csv" "PUERTO" "VOLUMEN=15"`

El valor predeterminado para el volumen es el máximo.

- **csv-midi.py** : EN DESARROLLO. Cuando esté acabado guardará los contenidos de un archivo .mid en 768 bytes para que lo lea un microprocesador como el 6502 o Z80 :)

- **midisender.py** : hace lo mismo que amadeussender.py, salvo que solo disponemos de 3 canales ya que es el número de canales de un chip YM2149 / AY-3-8910, y los datos no se envían de la misma manera. Ejercicio para el lector ver cómo se envían porque me da pereza mirarlo de nuevo, pero la diferencia es que con amadeussender, el que hace el trabajo es el ordenador, ya que descompone la nota en los valores para cada registro, mientras que en este, el Arduino tiene que "decodificar" un poco el mensaje que le llega para tocar la nota.

Uso: `python midisender.py "ARCHIVO.csv" "PUERTO"`

- **frequency_calculator.py** :  Calcula el valor que hay que poner en los registros del YM2149/AY-3-8910 para obtener todas las frecuencias posibles. Esto es necesario porque luego el programa csv-midi.py lee la frecuencia del archivo csv (que antes era midi) y necesita saber el equivalente para el AY-3-8910

frequency_calculator.py devuelve un string con forma de array de python para pegarlo directamente en la variable combis de csv-midi.py o midisender.py.

Para usarlo: `python thing_calculator.py [FRECUENCIA EN Hz (opcional)]`

Ex.: `python thing_calculator.py`
o
`python thing_calculator.py 1000000`

=> El primero ejecuta el programa con la frecuencia predeterminada de 2MHz, mientras que el segundo lo hace con una de 1Mhz.

### Carpeta *[VideoMaker](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Programas/VideoMaker)* (dentro de la carpeta *Programas*)

Contiene el programa **cancionVideo.py**. Este programa necesita un archivo .amds generado por el programa amadeusguardar.py. Lo que hace es generar una secuencia de imágenes (o fotogramas), que luego pueden ser reproducidos a la velocidad que indicada para que muestre el estado de los registros a la vez que estos suenan.

Uso: `python cancionVideo.py "ARCHIVO.amds"`

Nota: Aún tengo que ver por qué el vídeo que sale y la canción que se tocan no van a la par cuando deberían durar lo mismo, algo tiene que ver el tiempo de ejecución del código seguramente, pero me da pereza ponerme a hacer timers. Puede que lo haga, pero no en python sino en un programa en C o algo parecido idk. Para pasar de imágenes a vídeo recomiendo utilizar ffmpeg, no lo subo porque no es mío, pero dejo el comando de como pasar las imágenes a vídeo escrito en el archivo comando.txt

CRÉDITOS:
  -CÓDIGO PARA ENVIAR ÓRDENES A UN AY-3-8910 o YM2149 DESDE UN ARDUIO: https://github.com/986-Studio/AY-3-3910-Player/blob/master/AY3910RegWrite/AY3910RegWrite.ino PARTE DE ARDUINO
