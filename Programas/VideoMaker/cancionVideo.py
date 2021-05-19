# USO: python cancionVideo.py "ARCHIVO.amds"
# Nota: siempre va a guardar los archivos en una carpeta llamada output

# ------------ LIBRERIAS ------------

import sys
import csv
import numpy as np
from PIL import Image, ImageDraw, ImageFont

# ------------ VARIABLES ------------

archivo = sys.argv[1] # Coger el nombre del archivo

FPS = 0			# Guarda el numero de fotogramas por segundo
tempo = 0		# Numero de ms que pasan cada clk
pulsos = 24		# Numero de midi clks por ms

posicion = 0	# Indica el numero de fotograma

notas = [[0] * 13]	# Array que va a guardar [momento, prim_regA_0, prim_regA_1, prim_regB_0, prim_regB_1, prim_regC_0, prim_regC_1, sec_regA_0, sec_regA_1, sec_regB_0, sec_regB_1, sec_regC_0, sec_regC_1]

utilizados = [[0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0]] # Guarda los valores de los registros

nombres = ["A", "B", "C"]	# El nombre de cada canal

fontChip = ImageFont.truetype("arial.ttf", 35)		# Fuente para el texto CHIP x
fontLetras = ImageFont.truetype("arial.ttf", 25)	# Fuente para el texto A, B, C

ultimoFotograma = None

# ------------ FUNCIONES ------------

def dibujarFotograma(reciclar):
	global posicion, ultimoFotograma
	
	if (reciclar):
		if (posicion == 0):
			dibujarFotograma(False)
			return
		fotograma = ultimoFotograma
	else:
		fotograma = Image.new(mode = "RGB", size = (1920, 1080), color = (0, 0, 255))
		
		draw = ImageDraw.Draw(fotograma)
		
		draw.text((320, 800), "CHIP 1", font = fontChip)
		
		for i in range(3):
			if (utilizados[i][0] == 0 and utilizados[i][1] == 0):
				draw.rectangle(((50 + 250 * i, 900), (200 + 250 * i, 1000)), fill="red")	# Poner el cuadrado rojo para indicar que el canal está apagado
				
				# Dibujar el valor de cada registro
				draw.text((120 + 250 * i, 910), "0", font = fontLetras)
				draw.text((120 + 250 * i, 960), "0", font = fontLetras)
			else:
				draw.rectangle(((50 + 250 * i, 900), (200 + 250 * i, 1000)), fill="green")	# Poner el cuadrado verde para indiar qeu el canal está abierto
			
				# Dibujar el valor de cada registro
				draw.text((120 + 250 * i, 910), str(utilizados[i][0]), font = fontLetras)
				draw.text((120 + 250 * i, 960), str(utilizados[i][1]), font = fontLetras)
			
			draw.line((50 + 250 * i, 950, 200 + 250 * i, 950), fill = 128)
			draw.text((115 + 250 * i, 1010), nombres[i % 3], font = fontLetras)
		
		draw.text((1490, 800), "CHIP 2", font = fontChip)
		for i in range(3, 6):
			if (utilizados[i][0] == 0 and utilizados[i][1] == 0):
				draw.rectangle(((475 + 250 * i, 900), (625 + 250 * i, 1000)), fill="red")	# Poner el cuadrado rojo para indicar que el canal está apagado
				
				# Dibujar el valor de cada registro
				draw.text((545 + 250 * i, 910), "0", font = fontLetras)
				draw.text((545 + 250 * i, 960), "0", font = fontLetras)
			else:
				draw.rectangle(((475 + 250 * i, 900), (625 + 250 * i, 1000)), fill="green")	# Poner el cuadrado verde para indicar que el canal está abierto
				
				# Dibujar el valor de cada registro
				draw.text((545 + 250 * i, 910), str(utilizados[i][0]), font = fontLetras)
				draw.text((545 + 250 * i, 960), str(utilizados[i][1]), font = fontLetras)
			
			draw.line((475 + 250 * i, 950, 625 + 250 * i, 950), fill = 128)
			draw.text((545 + 250 * i, 1010), nombres[i % 3], font = fontLetras)
			
			ultimoFotograma = fotograma									# Guardar el fotograma que acabamos de dibujar para que no haya que redibujarlo en caso de tener que reutilizarlo
	
	fotograma.save("output/" + str(posicion) + "_" + str(archivo[:-5]) + ".png")		# Guardar el fotograma
	
	posicion += 1		# Aumentar la posicion

def	fotogramaManager(pos):
	global posicion
	
	# Hacer que todos los fotogramas sean iguales al ultimo estado
	while (posicion < notas[pos][0] - 1):
		dibujarFotograma(True)
	
	# Actualizar los registros
	for i in range(1, 13):
		utilizados[(i - 1) // 2][(i - 1) % 2] = notas[pos][i]	# Actualizar el array desde donde dibujarFotograma lee el estado de los registros
	dibujarFotograma(False)

# Print iterations progress
def printProgressBar (iteration, total, prefix = '', suffix = '', decimals = 1, length = 100, fill = '█', printEnd = "\r"):
	percent = ("{0:." + str(decimals) + "f}").format(100 * (iteration / float(total)))
	filledLength = int(length * iteration // total)
	bar = fill * filledLength + '-' * (length - filledLength)
	print(f'\r{prefix} |{bar}| {percent}% {suffix}', end = printEnd)
	# Print New Line on Complete
	if iteration == total: 
		print()

def tiempoYaExiste(tiempo):
	for i in range(len(notas)):
		if (notas[i][0] == tiempo):
			return i
	return -1

with open(archivo, "r") as csv_file:
	csv_reader = csv.reader(csv_file, delimiter = ',')

	tempo = float(next(csv_reader)[1]) / 60000 # Guarda el tempo

	FPS = 1 / (24 * tempo)							# Guarda los FPS del video (BPM / 60 = BPs)
	
	print("Datos de la canción: \n Tempo es ", tempo, "beats/ms, por lo que el video ira a", FPS, "FPS.")
	
	chip = 0
	for nota in csv_reader:
			temp = [0] * 13
			
			for i in range(0, 7):		# Coger el estado de los primeros registros
				temp[i] = int(nota[i])
			
			for i in range(14, 20):
				temp[i - 7] = int(nota[i]) 
			
			notas.append(temp)

for i in range(len(notas)):
	notas[i][0] = int(notas[i][0] * FPS / 1000) # Pasar de tiempo a fotogramas

input("Todo listo, pulsa Enter para empezar")

final = len(notas)
totalTiempos = notas[-1][0]
for i in range(final):
	printProgressBar(i + 1, final, prefix = 'Progreso:', suffix = 'Completado', length = 50)
	fotogramaManager(i)