#uso python cancionPianoVisualizer.py "ARCHIVO.csv"

import csv
import numpy as np
from PIL import Image, ImageDraw

# --------------- VARIABLES ---------------

archivo = sys.argv[1] # Coger el nombre del archivo

# --- VARIABLES MUSICA ---

tempo = 0
pulsos = 24

preNotas = []

# --- VARIABLES VIDEO ---

ancho = 1920
alto = 1080

vel = 10 # pixels / s

# --------------- CODIGO ---------------

def dibujarFotograma(teclas, posiciones, encendido): # teclas guarda el nombre de teclas de piano, posiciones guarda tuples de cada
	
	fotograma = Image.new(mode = "RGB", size = (ancho, alto), color = (0, 0, 255))
	
	draw = Imagen.Draw(fotograma)

# Preparar el midi
with open(archivo) as csv_file:
	csv_reader = csv.reader(csv_file, delimiter=',')
	
	# Pasar por todo el archivo .csv
	for row in csv_reader:
		if (row[2] == " Header"): pulsos = int(row[5])	# Si es el Header, coger la información necesaria (los pulsos)
		if (tempo == 0 and int(row[1]) == 0 and row[2] == " Tempo"): tempo = int(row[3])/(pulsos * 1000) # ms/clck para obtener ms multiplicar por el momento
		
		
		if (row[2] == " Note_on_c"):
			preNotas.append([int(row[1]) * tempo, 1, int(row[4])])
		elif (row[2] == " Note_off_c"):
			preNotas.append([int(row[1]) * tempo, 0, int(row[4])])
		else: continue

preNotasNP = np.asarray(preNotas) # Pasar el array preNotas a un array de Numpy
preNotasNP.view('d,i8,i8').sort(axis = 0) # Ordenar el array con respecto a la columna 0 (tiempo)



def anadirNota(time, nota):
	return

def quitarNota(time, nota):
	return

for nota in preNotasNP:
	if (nota[1] == 1): anadirNota(nota[0], int(nota[2]))
	else: quitarNota(nota[0], int(nota[2]))
