# Uso: python amadeussender.py "ARCHIVO.csv" "PUERTO" "VOLUMEN=10"

# -------- Esta versión modificada de amadeussender permite tocar pistas (tracks) simultaneas que en el archivo .csv no están la una después de la otra
#          también permite configurar canales como ruido o musica

import os
import sys
import csv
import time
import serial
import numpy as np

# ------------ CONSTANTES ------------

volumenMin = 3
volumenMax = 15

# ------------ VARIABLES ------------

# Combis
combis = [[-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [15, 209], [14, 238], [14, 24], [13, 77], [12, 142], [11, 218], [11, 47], [10, 143], [9, 247], [9, 104], [8, 224], [8, 97], [7, 232], [7, 119], [7, 12], [6, 166], [6, 71], [5, 237], [5, 151], [5, 71], [4, 251], [4, 180], [4, 112], [4, 48], [3, 244], [3, 187], [3, 134], [3, 83], [3, 35], [2, 246], [2, 203], [2, 163], [2, 125], [2, 90], [2, 56], [2, 24], [1, 250], [1, 221], [1, 195], [1, 169], [1, 145], [1, 123], [1, 101], [1, 81], [1, 62], [1, 45], [1, 28], [1, 12], [0, 253], [0, 238], [0, 225], [0, 212], [0, 200], [0, 189], [0, 178], [0, 168], [0, 159], [0, 150], [0, 142], [0, 134], [0, 126], [0, 119], [0, 112], [0, 106], [0, 100], [0, 94], [0, 89], [0, 84], [0, 79], [0, 75], [0, 71], [0, 67], [0, 63], [0, 59], [0, 56], [0, 53], [0, 50], [0, 47], [0, 44], [0, 42], [0, 39], [0, 37], [0, 35], [0, 33], [0, 31], [0, 29], [0, 28], [0, 26], [0, 25], [0, 23], [0, 22], [0, 21], [0, 19], [0, 18], [0, 17], [0, 16], [0, 15], [0, 14], [0, 14], [0, 13], [0, 12], [0, 11], [0, 11], [0, 10], [0, 9]]
combisRuido = [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 3, 5, 6, 8, 9, 10, 12, 13, 14, 15, 16, 17, 17, 18, 19, 20, 20, 21, 22]

# Variables tiempo

suma = 0 	# Se encarga de contar cuantos delays se han hecho, sumando se obtiene cuanto ha pasado desde el principio
total = 0 	# Tiempo total

# Variables los tracks

tracks = [] 		# Cada vez que se lea un comando Start_track, se mete aqui un 0, despues se preguntara al utilizador si quiere que cada canal sea nota (0) o ruido (1)
notasTracks = [] 	# Notas que hay en el track actual 

# Otras variables

archivo = sys.argv[1]	# Guarda el nombre del archivo .csv que se abre

puerto = sys.argv[2]	# Guarda el puerto al que hay que conectarse

volumen = 10			# Guarda el volumen al que se tienen que poner los canales una vez que estén activos (el predeterminado es 10)

if (len(sys.argv) >= 4):
	try:
		volumen = int(sys.argv[3])
		
		if (volumen > volumenMax or volumen < volumenMin):
			print("El valor máximo es",volumenMax,"y el minimo",volumenMin)
			quit()
	except ValueError:
		print("El volumen introducido", sys.argv[3], "no es válido, por favor introduce un número entero entre ", volumenMin," y", volumenMax)
		quit()

# Variables con respecto al archivo .csv

preNotas = [] # Guarda un array con todas las notas que hay, aunque sean de pistas diferentes. Es un array de arrays y cada array tiene [Tiempo, 1 = encender : 0 = apagar, nota, track]

ultimavez = 0 # Ultimo cambio entre activar / desactivar una nota

tempo = 0 # Guarda los milisegundos / midi clock pulses, para obtener los milisegundos solo hay que multiplicar por los clk pulses

pulsos = 24 # Pulsos cada cuarto de nota

preguntar  = False # Preguntar si desea continuar en caso de que la cancion pueda no sonar bien

canalesUtilizados = 0	# Utilizado para ver cuantos canales estan abiertos en un momento dado

# Variables para con respecto a los chips YM2149F

# Canales para saber si esta disponible el ruido o no, len(dispoT) + len(dispoN) = 6
dispoT = [-1, -1, -1, -1, -1, -1] 	# Guarda la nota que se esta tocando en cada canal de tune (los 3 primeros son los canales A, B y C [en ese order] del primer chip, y después el mismo orden A, B y C del segundo chip)
dispoN = [] 						# Guarda la nota que se esta tocando en cada canal de ruido

tune2chip  = [0, 0, 0, 1, 1, 1]
tune2canal = [0, 1, 2, 0, 2, 3]

noise2chip = []
noise2canal = []

# Si hay ruido, siempre es el canal C el que se atribuye

notas = [] 				# Array de arrays de 3 bytes que guardan: [chip, registro, valor]
tiemposEntreNotas = [] 	# Delay que hay entre cambios de notas

# ------------ FUNCIONES ---------------

def getCanalDisponible(tipo): # Devuelve la posicion del primer canal que esté disponible. tipo = 0 tune , 1 = ruido
	if (tipo == 0):
		for i in range(len(dispoT)):
			if (dispoT[i] == -1): return i
		return -1
	else:
		for i in range(len(dispoN)):
			if (dispoN[i] == -1): return i
		return -1

def getCanalNoDispo(nota, tipo): # Devuelve el canal que esta tocando la nota nota. tipo = 0 tune , 1 = ruido
	if (tipo == 0):
		for i in range(len(dispoT)):
			if (nota == dispoT[i]): return i
		return -1
	else:
		for i in range(len(dispoN)):
			if (nota == dispoN[i]): return i
		return -1

def getTiempo(tiempo):
	minutos = str(int(tiempo % 60))
	
	while (len(minutos) < 2):
		minutos = "0" + minutos
    
	temp = str(int(tiempo // 60)) + ":" + minutos
	return temp

def getLoadingBar(actual, final):
	actual = int(25 * (actual / final))
	
	temp = ""
	for i in range(actual): temp += "█"
	for i in range(25 - actual): temp += "░"
	
	if (len(temp) > 25): temp = temp[0:24]
	return temp

# ========== FUNCIONES PARA AÑADIR / QUITAR NOTAS ==========

def anadirNota(tiempo, nota): 	# Anade una nota al array notas (duh)
	global ultimavez, volumen
	if (tiempo - ultimavez < 0): return # Sanity check
	
	posicion = getCanalDisponible(0)	# Coger el primer canal que este disponible
	if (posicion == -1 or combis[nota][0] == -1): return		# Si no hay disponibles, i.e. la funcion devuelve -1, no hay nada que hacer, entonces no se guarda la nota
	
	chip = tune2chip[posicion]		# [0 -> primer chip; 1 -> segundo chip]
	canal = tune2canal[posicion]	# canal {0, 1, 2}
	
	tiemposEntreNotas.append(tiempo - ultimavez)	# Contar cuanto ha pasado desde la ultima nota y ponerlo en la lista de espera
	tiemposEntreNotas.append(0)
	tiemposEntreNotas.append(0)
	ultimavez = tiempo	# Hacer que esta sea la ultima nota guardando este "instante" como el ultimo
	
	dispoT[posicion] = nota # Indicar que el canal no esta disponible quitando el -1 y poniendo la nota que esta sonando

	# Ahora vamos a enviar 3 mensajes, el primero para configurar el registro inferior del canal, el segundo para modificar el registro superior y el ultimo para poner el volumen al maximo. TODO: OPTIMIZAR PARA QUE NO SE TENGA QUE PONER SIEMPRE EL VOLUMEN AL MAXIMO
	notas.append([chip, canal * 2, combis[nota][1]])
	notas.append([chip, (canal * 2) + 1, combis[nota][0]])
	notas.append([chip, canal + 8, volumen])

def anadirRuido(tiempo, nota):
	global ultimavez, volumen
	if (tiempo - ultimavez < 0): return # Sanity check
	
	posicion = getCanalDisponible(1)	# Coger el primer canal que este disponible
	if (posicion == -1 or combisRuido[nota] == -1): return		# Si no hay disponibles, i.e. la funcion devuelve -1, no hay nada que hacer, entonces no se guarda la nota
	
	chip  = noise2chip[posicion]	# El chip es el resultado de la division euclidiana (o entera) [posicion < 3 -> // 3 = 0 -> primer chip; posicion >= 3 -> // 3 = 1 -> segundo chip]
	canal = noise2canal[posicion]	# canal {0, 1, 2}
	
	tiemposEntreNotas.append(tiempo - ultimavez)	# Contar cuanto ha pasado desde la ultima nota y ponerlo en la lista de espera
	tiemposEntreNotas.append(0)
	ultimavez = tiempo	# Hacer que esta sea la ultima nota guardando este "instante" como el ultimo
	
	dispoN[posicion] = nota # Indicar que el canal no esta disponible quitando el -1 y poniendo la nota que esta sonando

	# Ahora vamos a enviar 3 mensajes, el primero para configurar el registro inferior del canal, el segundo para modificar el registro superior y el ultimo para poner el volumen al maximo. TODO: OPTIMIZAR PARA QUE NO SE TENGA QUE PONER SIEMPRE EL VOLUMEN AL MAXIMO
	notas.append([chip, 6, combisRuido[nota]])
	notas.append([chip, canal + 8, volumen])

def quitarNota(tiempo, nota, tipo):	# Quita la nota y pone el canal como disponible ; tipo = 0 tune ; tipo = 1 ruido
	global ultimavez
	if (tiempo - ultimavez < 0): return	# Sanity check
	
	posicion = getCanalNoDispo(nota, tipo)	# Coger el canal que esta tocando la nota nota
	if (posicion == -1): return		# Si por alguno razon algo fuera mal, pararse
	
	tiemposEntreNotas.append(tiempo - ultimavez)
	ultimavez = tiempo
	
	if (tipo == 0):
		dispoT[posicion] = -1	# Marcar el canal como disponible
	else:
		dispoN[posicion] = -1
	
	# Para desactivar un canal es facil, solamente ponemos el volumen a 0 :)
	notas.append([tune2chip[posicion], tune2canal[posicion] + 8, 0])

# ------------ CODIGO ------------

# Preparar el midi
with open(archivo) as csv_file:
	csv_reader = csv.reader(csv_file, delimiter=',')
	
	# Pasar por todo el archivo .csv
	for row in csv_reader:
		if (row[2] == " Header"): pulsos = int(row[5])	# Si es el Header, coger la información necesaria (los pulsos)
		if (tempo == 0 and row[2] == " Tempo" and int(row[1]) == 0): tempo = int(row[3])/(pulsos * 1000) # ms/clck para obtener ms multiplicar por el momento
		
		if (row[2] == " Start_track"): # Nuevo track
			tracks.append(0) # Por defecto, un canal es tune
			notasTracks.append(0)
		
		if (row[2] == " Note_on_c"):
			preNotas.append([int(row[1]) * tempo, 1, int(row[4]), len(tracks) - 1])
			notasTracks[-1] += 1
		elif (row[2] == " Note_off_c"):
			preNotas.append([int(row[1]) * tempo, 0, int(row[4]), len(tracks) - 1])

preNotasNP = np.asarray(preNotas) # Pasar el array preNotas a un array de Numpy
preNotasNP.view('d,i8,i8,i8').sort(axis = 0) # Ordenar el array con respecto a la columna 0 (tiempo)

# Mirar si hay mas de 6 canales abiertos a la vez
for nota in preNotasNP:
	if (nota[1] == 1): canalesUtilizados += 1
	else: canalesUtilizados -= 1
	
	if (canalesUtilizados > 6):
		print("En", nota[0], "se necesitarían", canalesUtilizados, "canales.")
		preguntar = True

if (preguntar): # Preguntar si quiere continuar
	if (input("El archivo introducido prodría sonar mal debido a la falta de canales, ¿quieres continuar? (s/n): \n") != "s"): quit()

if (len(tracks) == 1):
		input("Canción lista, dale a Enter para empezar. \n")
else:
	userIn = "lololol"
	while (True):
		os.system("cls")
		ins = userIn.split(" ")
		
		if (userIn == ""):
			break
		
		elif(len(ins) == 2 and int(ins[0]) > 0 and int(ins[0]) <= len(tracks) and (int(ins[1]) == 0 or int(ins[1]) == 1)):
			if (int(ins[1]) == 0 or (int(ins[1]) == 1 and sum(tracks) < 2)): # Comprobar que no haya max de 2 canales
				tracks[int(ins[0]) - 1] = int(ins[1])
			elif (int(ins[1]) == 1 and sum(tracks) == 2):
				print("Solo se pueden configurar maximo 2 tracks como ruido")
		
		# Comprobar cuantas notas se pueden tocar
		idxRuido = []
		for i in range(len(tracks)):
			if (tracks[i] == 1): idxRuido.append(i)
		
		for idT in idxRuido:
			toc = 0	# Notas tocables
			tot = 0 # Notas totales
			for pre in preNotas:
				if (int(pre[3]) != idT or pre[1] == 0): continue # Si no es el track que estamos examinando o es un comando de apagar nota, continuamos
				
				tot += 1
				if (combisRuido[int(pre[2])] != -1): toc += 1
			
			print("En el track #", idT + 1, " se pueden tocar ", toc, "/", tot, " notas")
		
		for i, t in enumerate(tracks):
			print("Track #", i+1, "(", notasTracks[i], " notas) está en modo", ("Ruido", "Tune")[t == 0])
		
		print("Para cambiar un track escribe el numero del track y un 0 si quieres que sea tune o 1 si quieres que sea ruido.\nDale a enter sin nada para acabar de editar los canales\nSolo se pueden configurar 2 tracks maximo como ruido")
		userIn = input("")

# Algoritmo para distribuir canales : por defecto el programa espera que todos los canales se dediquen a tune

if (sum(tracks) == 1): # 1 solo canal se usa para ruido
	dispoT.pop()
	dispoN = [-1]
	
	noise2chip.append(0)
	noise2canal.append(2)
	
	del tune2chip[2]  # Quitar el canal C del primer chip
	del tune2canal[2] # Quitar el canal C del primer chip
elif (sum(tracks) == 2):
	dispoT.pop()
	dispoT.pop()
	dispoN = [-1] * 2
	
	noise2chip.append(0) # Añadir el canal C del primer chip
	noise2canal.append(2)
	
	noise2chip.append(1) # Añadir el canal C del segundo chip
	noise2canal.append(2)
	
	del tune2chip[5]  # Quitar el canal C del segundo chip
	del tune2canal[5]
	
	del tune2chip[2] # Quitar el canal C del primer  chip
	del tune2canal[2]

if (len(dispoT) + len(dispoN) != 6): # Sanity check
	print("Error en los canales")
	quit()

for nota in preNotasNP:
	if (nota[1] == 1):
		if (tracks[int(nota[3])] == 0): 	# Track de tune
			anadirNota(nota[0], int(nota[2]))
		else:								# Track de ruido
			anadirRuido(nota[0], int(nota[2]))
	else:
		quitarNota(nota[0], int(nota[2]), tracks[int(nota[3])])

# Calcular el tiempo sumando todos los tiempos entre notas
for i in tiemposEntreNotas: total += i
total /= 1000 # Pasar a segundos

# Conexion serial
with serial.Serial(puerto, 115200, timeout=1) as ser:
	# Enviar la configuracion de los canales
	if (sum(tracks) == 1):
		t = bytearray([0, 7, 28]) 	# 0 : primer chip ; 7 : registro de los mixers ; 28 = 0b00 011 100 poner el canal C en modo ruido y no en tune
		ser.write(t)				# Enviar el array de bytes
	elif (sum(tracks) == 2):
		t = bytearray([0, 7, 28])
		ser.write(t)
		time.sleep(0.25)
		t = bytearray([1, 7, 28]) 	# 1 : segundo chip ; 7 : registro de los mixers ; 28 = 0b00 011 100 poner el canal C en modo ruido y no en tune
		ser.write(t)				# Enviar el array de bytes
	else: 							# No hay ningún canal de ruido, reseteamos los mixers para que solo haya tunes
		t = bytearray([0, 7, 56]) 	# 0 : primer chip ; 7 : registro de los mixers ; 56 = 0b00 111 000 poner todos los canales en modo tune
		ser.write(t)				# Enviar el array de bytes
		time.sleep(0.25)
		t = bytearray([1, 7, 56])	# Lo mismo que el anterior
		ser.write(t)

	time.sleep(0.5) # Medio segundo de preparación antes de la actuación
	
	print("Tocando:", archivo)
	
	for i in range(len(tiemposEntreNotas)):
		time.sleep(tiemposEntreNotas[i] / 1000)
		suma += tiemposEntreNotas[i] / 1000
		
		t = bytearray(notas[i])	# Transformar cada array en un array de bytes
		ser.write(t)			# Y enviar el array de bytes
		
		print(" {}".format(getTiempo(suma) + " " + getLoadingBar(i, len(tiemposEntreNotas))) + " " + getTiempo(total), end="\r")
	
	time.sleep(1)	# Descansar después de la actuación
	for c in range(len(dispo)):	# Desactivar todos los canales
		t = bytearray([c // 3, (c % 3) + 9, 0])	# Preparar el mensaje
		ser.write(t)	# Enviar el mensaje
		time.sleep(0.25) # Dejar un poco de tiempo entre canal mensaje y mensaje

print("Fin")