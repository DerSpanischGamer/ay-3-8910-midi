# Uso: python midisender.py "ARCHIVO.csv" "PUERTO"

import sys
import csv
import time
import serial

# Constantes

activar = 64		# B01000000
desactivar = 128	# B10000000

canales = [1, 2, 4]

# Combis calculado con thing_calculator.py
combis = [[-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [15, 209], [14, 238], [14, 24], [13, 77], [12, 142], [11, 218], [11, 47], [10, 143], [9, 247], [9, 104], [8, 224], [8, 97], [7, 232], [7, 119], [7, 12], [6, 166], [6, 71], [5, 237], [5, 151], [5, 71], [4, 251], [4, 180], [4, 112], [4, 48], [3, 244], [3, 187], [3, 134], [3, 83], [3, 35], [2, 246], [2, 203], [2, 163], [2, 125], [2, 90], [2, 56], [2, 24], [1, 250], [1, 221], [1, 195], [1, 169], [1, 145], [1, 123], [1, 101], [1, 81], [1, 62], [1, 45], [1, 28], [1, 12], [0, 253], [0, 238], [0, 225], [0, 212], [0, 200], [0, 189], [0, 178], [0, 168], [0, 159], [0, 150], [0, 142], [0, 134], [0, 126], [0, 119], [0, 112], [0, 106], [0, 100], [0, 94], [0, 89], [0, 84], [0, 79], [0, 75], [0, 71], [0, 67], [0, 63], [0, 59], [0, 56], [0, 53], [0, 50], [0, 47], [0, 44], [0, 42], [0, 39], [0, 37], [0, 35], [0, 33], [0, 31], [0, 29], [0, 28], [0, 26], [0, 25], [0, 23], [0, 22], [0, 21], [0, 19], [0, 18], [0, 17], [0, 16], [0, 15], [0, 14], [0, 14], [0, 13], [0, 12], [0, 11], [0, 11], [0, 10], [0, 9], [0, 9], [0, 8], [0, 8], [0, 7], [0, 7], [0, 7], [0, 6], [0, 6], [0, 5], [0, 5], [0, 5], [0, 4], [0, 4], [0, 4], [0, 4], [0, 3], [0, 3], [0, 3], [0, 3], [0, 3], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1]]

# Variables tiempo

suma = 0 	# Se encarga de contar cuantos delays se han hecho, sumando se obtiene cuanto ha pasado desde el principio
total = 0 	# Tiempo total

# Otras vaiables
archivo = sys.argv[1]

puerto = sys.argv[2]

notas = [] 				# Array de arrays de 3 bytes que guardan: [instruccion, low byte, high byte]
tiemposEntreNotas = [] 	# Delay que hay entre cambios de notas

ultimavez = 0 # Ultimo cambio entre activar / desactivar una nota

tempo = 0 # Guarda los milisegundos / midi clock pulses, para obtener los milisegundos solo hay que multiplicar por los clk pulses

dispo = [-1, -1, -1] # Guarda la nota que se esta tocando en cada canal

pulsos = 24 # Pulsos cada cuarto de nota

preguntar = False

# Mirar si hay más de tres lineas abiertas al msismo tiempo
with open(archivo) as csv_file:
	canalesUtilizados = 0
	
	csv_reader = csv.reader(csv_file, delimiter=',')
	for row in csv_reader:
		
		if (row[2] == " Note_on_c"): canalesUtilizados += 1
		elif (row[2] == " Note_off_c"): canalesUtilizados -= 1
		else: continue
		
		if (canalesUtilizados > 3):
			preguntar = True # El midi podría sonar mal, hay que preguntar si quiere jugarsela :S
			print("En", row[1], " se están utilizando más de 3 canales al mismo tiempo. Una vez que los 3 canales están siendo utilizados, el resto se ignoran.")	

if (preguntar): # Preguntar si quiere continuar
	if (input("\n \nEl archivo introducido prodría sonar mal debido a la falta de canales, ¿quieres continuar? (s/n): \n") != "s"): quit()

def getCanalDisponible():
	for i in range(len(dispo)):
		if (dispo[i] == -1): return i
	return -1

def anadirNota(tiempo, nota):
	global ultimavez
	if (tiempo - ultimavez < 0): return
	tiemposEntreNotas.append(tiempo - ultimavez)
	ultimavez = tiempo
	
	canal = getCanalDisponible()
	if (canal == -1): return
	
	dispo[canal] = nota
	notas.append([activar | canales[canal], combis[nota][1], combis[nota][0]])

def getCanalNoDispo(nota):
	for i in range(len(dispo)):
		if (nota == dispo[i]): return i
	return i

def quitarNota(tiempo, nota):
	global ultimavez
	if (tiempo - ultimavez < 0): return
	tiemposEntreNotas.append(tiempo - ultimavez)
	ultimavez = tiempo
	
	canal = getCanalNoDispo(nota)
	if (canal == -1): return
	
	dispo[canal] = -1
	notas.append([desactivar | canales[canal], 0, 0])

# Preparar el midi
with open(archivo) as csv_file:
	csv_reader = csv.reader(csv_file, delimiter=',')
	for row in csv_reader:
		if (row[2] == " Header"): pulsos = int(row[5])
		if (tempo == 0 and int(row[1]) == 0 and row[2] == " Tempo"): tempo = int(row[3])/(pulsos * 1000) # ms/clck para obtener ms multiplicar por el momento
		
		if (row[2] == " Note_on_c"):
			anadirNota(int(row[1]) * tempo, int(row[4]))
		elif (row[2] == " Note_off_c"):
			quitarNota(int(row[1]) * tempo, int(row[4]))
		else: continue

def getTiempo(tiempo):
	temp = str(int(tiempo // 60)) + ":" + str(int(tiempo % 60))
	return temp

def getLoadingBar(actual, final):
	actual = int(25 * (actual / final))
	
	temp = ""
	for i in range(actual): temp += "█"
	for i in range(25 - actual): temp += "░"
	
	if (len(temp) > 25): temp = temp[0:24]
	return temp

# Conexion serial
with serial.Serial(puerto, 115200, timeout=1) as ser:
	for i in tiemposEntreNotas: total += i
	total /= 1000 # Pasar a segundos
	print("Tocando:", archivo)
	for i in range(len(tiemposEntreNotas)):
		time.sleep(tiemposEntreNotas[i] / 1000)
		suma += tiemposEntreNotas[i] / 1000
		
		t = bytearray(notas[i])
		ser.write(t)
		
		print(" {}".format(getTiempo(suma) + " " + getLoadingBar(i, len(tiemposEntreNotas))) + " " + getTiempo(total), end="\r")
	
	time.sleep(1)
	for c in canales:
		t = bytearray([desactivar | c, 0, 0])
		ser.write(t)
		time.sleep(0.2)

print("Fin")