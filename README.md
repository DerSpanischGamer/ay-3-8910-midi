# ay-3-8910-midi
Una interfaz en Python para el chip AY-3-8910 que usa Arduino.

Para poder enviar los archivos midi primero hay qeu tranformarlos em csv con este programa: https://www.fourmilab.ch/webtools/midicsv/#Download

(o cualquier otro que transforme .mid en .csv)

Después utilizar python midisender.py [ARCHIVO] [PUERTO] (ex.: python midisender.py hack.csv COM4) para enviar el archivo [ARCHIVO] al Arduino a través del puerto [PUERTO].

midisender.py está configurado para un AY-3-8910 conectado a un reloj de 2MHz, para configurar un nuevo reloj, usar el programa frequency_calculator.py

frequency_calculator.py:  Calcula el valor que hay que poner en los registros del AY-3-8910 para obtener todas las frecuencias posibles. Esto es necesario porque luego el programa csv-midi.py lee la
frecuencia del archivo csv (que antes era midi) y necesita saber el equivalente para el AY-3-8910

frequency_calculator.py devuelve un string con forma de array de python para pegarlo directamente en la variable combis de csv-midi.py o midisender.py.

Para usar frequency_calculator.py se escribe: python thing_calculator.py [FRECUENCIA EN Hz (opcional)] (ex.: python thing_calculator.py o python thing_calculator.py 1000000 -> el primero ejecuta el programa con la frecuencia predeterminada de 2MHz, mientras que el segundo lo hace con una de 1Mhz).

Por último, csv-midi.py aún está en desarrollo pero guardará un midi en 768 bytes de midi para que lo lea un micro-procesador como el 6502 o Z80 :)


CRÉDITOS:
  -https://github.com/986-Studio/AY-3-3910-Player/blob/master/AY3910RegWrite/AY3910RegWrite.ino PARTE DE ARDUINO
