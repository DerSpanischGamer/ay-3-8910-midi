# ay-3-8910-midi
Una interfaz en Python para el chip AY-3-8910 que usa Arduino.

Para poder enviar los archivos midi primero hay qeu tranformarlos em csv con este programa: https://www.fourmilab.ch/webtools/midicsv/#Download

(o cualquier otro que transforme .mid en .csv)

Después utilizar python midisender.py [ARCHIVO] [PUERTO] (ex.: python midisender.py hack.csv COM4) para enviar el archivo [ARCHIVO] al Arduino a través del puerto [PUERTO].

midisender.py está configurado para un AY-3-8910 conectado a un reloj de 2MHz, para configurar un nuevo reloj, usar el programa thing_calculator.py.

Para usar thing_calculator.py se escribe: python thing_calculator.py [FRECUENCIA EN Hz (opcional)] (ex.: python thing_calculator.py o python thing_calculator.py 1000000 -> el primero ejecuta el programa con la frecuencia predeterminada de 2MHz, mientras que el segundo lo hace con una de 1Mhz). Este programa devuelve un string con forma de array de python para pegarlo en la variable combis de csv-midi.py o midisender.py.

Por último, csv-midi.py aún está en desarrollo pero guardará un midi en 768 bytes de midi para que lo lea un ordandor :)


CRÉDITOS:
  -https://github.com/986-Studio/AY-3-3910-Player/blob/master/AY3910RegWrite/AY3910RegWrite.ino PARTE DE ARDUINO
