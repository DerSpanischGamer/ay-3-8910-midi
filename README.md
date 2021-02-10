# ay-3-8910-midi
##Una interfaz en Python para el chip AY-3-8910 que usa Arduino.

Para poder enviar los archivos midi primero hay qeu tranformarlos en csv con este programa: https://www.fourmilab.ch/webtools/midicsv/#Download

(o cualquier otro que transforme .mid en .csv)

## Explicaci�n de los archivos

### Carpeta *AY3910RegWrite (original para 1 chip solo conectado a Arduino)*

Como su nombre indica, en esta carpeta se encuentra el c�digo para que funcione un solo chip conectado a una placa Arduino de la misma manera que se muestra en la imagen [Conexiones.png](https://github.com/DerSpanischGamer/ay-3-8910-midi/blob/master/Conexiones.png)

### Carpeta *https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/AY3910RegWrite-library*

C�digo que soporta 1 o 2 chips simult�neos. Es una versi�n simplificada del c�digo anterior, adem�s de m�s facil de leer gracias a que el c�digo ha sido transformado en [librer�a](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Amadeus).

### Carpeta *Amadeus*

Es la librer�a para hacer m�s f�cil y limpio el c�digo.

Las �nicas funciones que hay que saberse son:

Importar la librer�a
'#include "Amadeus.h"

Iniciar una instancia de la liber�a
'Amadeus amadeus = Amadeus(); // Iniciar la clase Amadeus'

Iniciar todo el reloj y configurar los pines
'amadeus.begin()'

Poner un valor VAL en el registro REG del chip C
'amadeus.out(C, REG, VAL);'

El resto de funciones son privadas ya que solo sirven para llevar a cabo las funciones que est�n aqu� arriba. Si eres curioso echa un vistazo al c�digo, aunque es literalmente lo mismo que el c�digo de la primera carpeta.

### Carpeta *Canciones*

No hay mucho que decir, algunas canciones que he buscado yo el archivo .mid y lo he transforamdo en .csv para poder enviarlo.

### Carpeta *Programas*

Boy oh boy, es aqu� que empieza la diversi�n. Primero, los archivos .exe son medio a ignorar medio no.

- **Controlador Registros Dual YM2149.exe** : es un programa que estoy haciendo para controlar todos los registros de una manera visual. NO EST� ACABADO Y TIENE ERRORES SEGURAMENTE.

- **Midicsv.exe** : Transforma un archivo .mid en .csv. Para usarlo se necesita la consola de comandos y el comando es 'Midicsv.exe "ARCHIVO.mid" "OUTPUT.csv"'. Obviamente ARCHIVO es el nombre del archivo .mid que queremos transformar y OUTPUT es el nombre que queremos que tenga el archivo de salida.

- **bin2hex.exe** : A ignorar, no se ni lo que hace...

- **hexdump.exe** : �til para hacer un hexdump desde la consola de comandos, pero de momento me es un poco in�til ya que env�o los bytes directamente en vez de guardarlos, pero qui�n sabe, a lo mejor alg�n d�a me ser� �til.

Ok, llegamos a los archivos .py que son los interesantes:

- **amadeussender.py** : sirve para poder utilizar los dos chips de una [placa Amadeus](https://www.youtube.com/watch?v=V24AyQ2n8vY) o un Arduino que est� conectado de la misma manera. La diferencia entre este archivo y **midisender.py** es que este puede manejar **6** canales simult�neos, en vez de 3 tristes y solitarios canales.

Para usarlo: 'python amadeussender.py "ARCHIVO.csv" "PUERTO"'

IMPORTANTE: los datos que envia este archivo son (en este orden):

* CHIP : ( 0 -> PRIMERO, 1 -> SEGUNDO ).

* REGISTRO : el registro del chip que queremos modificar ( 0 -> 16 ).

* VALOR : el valor que queremos escribir en el registro ( 0 -> 256).

Para que funcione lo que env�a, hay que instalar en el Arduino el c�digo adecuado (el que no pone para 1 placa vamos).

- **csv-midi.py** : EN DESARROLLO. Cuando est� acabado guardar� los contenidos de un archivo .mid en 768 bytes para que lo lea un microprocesador como el 6502 o Z80 :)

- **midisender.py** : hace lo mismo que amadeussender.py, salvo que solo disponemos de 3 canales ya que es el n�mero de canales de un chip YM2149 / AY-3-8910, y los datos no se env�an de la misma manera. Ejercicio para el lector ver c�mo se env�an porque me da pereza mirarlo de nuevo, pero la diferencia es que con amadeussender, el que hace el trabajo es el ordenador, ya que descompone la nota en los valores para cada registro, mientras que en este, el Arduino tiene que "decodificar" un poco el mensaje que le llega para tocar la nota.

Uso: 'python midisender.py "ARCHIVO.csv" "PUERTO"'

- **frequency_calculator.py** :  Calcula el valor que hay que poner en los registros del YM2149/AY-3-8910 para obtener todas las frecuencias posibles. Esto es necesario porque luego el programa csv-midi.py lee la frecuencia del archivo csv (que antes era midi) y necesita saber el equivalente para el AY-3-8910

frequency_calculator.py devuelve un string con forma de array de python para pegarlo directamente en la variable combis de csv-midi.py o midisender.py.

Para usarlo: 'python thing_calculator.py [FRECUENCIA EN Hz (opcional)]'

Ex.: 'python thing_calculator.py'
o
'python thing_calculator.py 1000000'

=> El primero ejecuta el programa con la frecuencia predeterminada de 2MHz, mientras que el segundo lo hace con una de 1Mhz.

CRÉDITOS:
  -https://github.com/986-Studio/AY-3-3910-Player/blob/master/AY3910RegWrite/AY3910RegWrite.ino PARTE DE ARDUINO
