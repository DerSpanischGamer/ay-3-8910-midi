# Uso: python amadeussender.py "ARCHIVO.csv" "PUERTO" "VOLUMEN=10"

# -------- Esta versión modificada de amadeussender permite tocar pistas (tracks) simultaneas que en el archivo .csv no están la una después de la otra

import sys
import csv
import time
import serial
import numpy as np

# ------------ CONSTANTES ------------

volumenMax = 15

# ------------ VARIABLES ------------

# Combis
combis = [[-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [15, 209], [14, 238], [14, 24], [13, 77], [12, 142], [11, 218], [11, 47], [10, 143], [9, 247], [9, 104], [8, 224], [8, 97], [7, 232], [7, 119], [7, 12], [6, 166], [6, 71], [5, 237], [5, 151], [5, 71], [4, 251], [4, 180], [4, 112], [4, 48], [3, 244], [3, 187], [3, 134], [3, 83], [3, 35], [2, 246], [2, 203], [2, 163], [2, 125], [2, 90], [2, 56], [2, 24], [1, 250], [1, 221], [1, 195], [1, 169], [1, 145], [1, 123], [1, 101], [1, 81], [1, 62], [1, 45], [1, 28], [1, 12], [0, 253], [0, 238], [0, 225], [0, 212], [0, 200], [0, 189], [0, 178], [0, 168], [0, 159], [0, 150], [0, 142], [0, 134], [0, 126], [0, 119], [0, 112], [0, 106], [0, 100], [0, 94], [0, 89], [0, 84], [0, 79], [0, 75], [0, 71], [0, 67], [0, 63], [0, 59], [0, 56], [0, 53], [0, 50], [0, 47], [0, 44], [0, 42], [0, 39], [0, 37], [0, 35], [0, 33], [0, 31], [0, 29], [0, 28], [0, 26], [0, 25], [0, 23], [0, 22], [0, 21], [0, 19], [0, 18], [0, 17], [0, 16], [0, 15], [0, 14], [0, 14], [0, 13], [0, 12], [0, 11], [0, 11], [0, 10], [0, 9], [0, 9], [0, 8], [0, 8], [0, 7], [0, 7], [0, 7], [0, 6], [0, 6], [0, 5], [0, 5], [0, 5], [0, 4], [0, 4], [0, 4], [0, 4], [0, 3], [0, 3], [0, 3], [0, 3], [0, 3], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1]]

# Variables tiempo

suma = 0 	# Se encarga de contar cuantos delays se han hecho, sumando se obtiene cuanto ha pasado desde el principio
total = 0 	# Tiempo total

# Otras variables

archivo = sys.argv[1]	# Guarda el nombre del archivo .csv que se abre

puerto = sys.argv[2]	# Guarda el puerto al que hay que conectarse

volumen = volumenMax	# Guarda el volumen al que se tienen que poner los canales una vez que estén activos (el predeterminado es el máximo)

if (len(sys.argv) >= 4):
	try:
		volumen = int(sys.argv[3])
		
		if (volumen > 15):
			print("El valor máximo es 15")
			quit()
	except ValueError:
		print("El volumen introducido", sys.argv[3], "no es válido, por favor introduce un número intero entre 0 y", volumenMax)
		quit()

preNotas = [] # Guarda un array con todas las notas que hay, aunque sean de pistas diferentes. Es un array de arrays y cada array tiene [Tiempo, 1 = encender : 0 = apagar, nota]

notas = [] 				# Array de arrays de 3 bytes que guardan: [chip, registro, valor]
tiemposEntreNotas = [] 	# Delay que hay entre cambios de notas

ultimavez = 0 # Ultimo cambio entre activar / desactivar una nota

tempo = 0 # Guarda los milisegundos / midi clock pulses, para obtener los milisegundos solo hay que multiplicar por los clk pulses

dispo = [-1, -1, -1, -1, -1, -1] # Guarda la nota que se esta tocando en cada canal (los 3 primeros son los canales A, B y C [en ese order] del primer chip, y después el mismo orden A, B y C del segundo chip)

pulsos = 24 # Pulsos cada cuarto de nota

preguntar  = False # Preguntar si desea continuar en caso de que la cancion pueda no sonar bien

# ------------ FUNCIONES ---------------

# Mirar si hay más de seis lineas abiertas al msismo tiempo
with open(archivo) as csv_file:
	canalesUtilizados = 0
	
	csv_reader = csv.reader(csv_file, delimiter=',')
	for row in csv_reader:
		
		if (row[2] == " Note_on_c"): canalesUtilizados += 1
		elif (row[2] == " Note_off_c"): canalesUtilizados -= 1
		else: continue
		
		if (canalesUtilizados > 6):
			preguntar = True # El midi podría sonar mal, hay que preguntar si quiere jugarsela :S
			print("En", row[1], " se están utilizando más de 6 canales al mismo tiempo. Una vez que los 6 canales están siendo utilizados, el resto se ignoran.")	

if (preguntar): # Preguntar si quiere continuar
	if (input("\n \nEl archivo introducido prodría sonar mal debido a la falta de canales, ¿quieres continuar? (s/n): \n") != "s"): quit()

# Devuelve la posicion del primer canal que esté disponible
def getCanalDisponible():
	for i in range(len(dispo)):
		if (dispo[i] == -1): return i
	return -1

# Devuelve el canal que esta tocando la nota nota
def getCanalNoDispo(nota):
	for i in range(len(dispo)):
		if (nota == dispo[i]): return i
	return -1

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


# Anade una nota al array notas (duh)
def anadirNota(tiempo, nota):
	global ultimavez, volumen
	if (tiempo - ultimavez < 0): return # Sanity check
	
	posicion = getCanalDisponible()	# Coger el primer canal que este disponible
	if (posicion == -1): return		# Si no hay disponibles, i.e. la funcion devuelve -1, no hay nada que hacer, entonces no se guarda la nota
	
	chip = posicion // 3	# El chip es el resultado de la division euclidiana (o entera) [posicion < 3 -> // 3 = 0 -> primer chip; posicion >= 3 -> // 3 = 1 -> segundo chip]
	canal = posicion % 3	# canal {0, 1, 2}
	
	tiemposEntreNotas.append(tiempo - ultimavez)	# Contar cuanto ha pasado desde la ultima nota y ponerlo en la lista de espera
	tiemposEntreNotas.append(0)
	tiemposEntreNotas.append(0)
	ultimavez = tiempo	# Hacer que esta sea la ultima nota guardando este "instante" como el ultimo
	
	dispo[posicion] = nota # Indicar que el canal no esta disponible quitando el -1 y poniendo la nota que esta sonando

	# Ahora vamos a enviar 3 mensajes, el primero para configurar el registro inferior del canal, el segundo para modificar el registro superior y el ultimo para poner el volumen al maximo. TODO: OPTIMIZAR PARA QUE NO SE TENGA QUE PONER SIEMPRE EL VOLUMEN AL MAXIMO
	notas.append([chip, canal * 2, combis[nota][1]])
	notas.append([chip, (canal * 2) + 1, combis[nota][0]])
	notas.append([chip, canal + 8, volumen])

# Quita la nota y pone el canal como disponible
def quitarNota(tiempo, nota):
	global ultimavez
	if (tiempo - ultimavez < 0): return	# Sanity check
	
	posicion = getCanalNoDispo(nota)	# Coger el canal que esta tocando la nota nota
	if (posicion == -1): return		# Si por alguno razon algo fuera mal, pararse
	
	tiemposEntreNotas.append(tiempo - ultimavez)
	ultimavez = tiempo
	
	dispo[posicion] = -1	# Marcar el canal como disponible
	
	# Para desactivar un canal es facil, solamente ponemos el volumen a 0 :)
	notas.append([posicion // 3, (posicion % 3) + 8, 0])

# ------------ CODIGO ------------

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

if (input("\n \n Cancion lista, pulsa cualquier tecla para continuar \n")): print()

preNotasNP = np.asarray(preNotas) # Pasar el array preNotas a un array de Numpy
preNotasNP.view('d,i8,i8').sort(axis = 0) # Ordenar el array con respecto a la columna 0 (tiempo)

for nota in preNotasNP:
	if (nota[1] == 1): anadirNota(nota[0], int(nota[2]))
	else: quitarNota(nota[0], int(nota[2]))

for a in notas:
	print(a)

# Conexion serial
with serial.Serial(puerto, 115200, timeout=1) as ser:
	for i in tiemposEntreNotas: total += i
	
	total /= 1000 # Pasar a segundos
	
	print("Tocando:", archivo)
	
	for i in range(len(tiemposEntreNotas)):
		time.sleep(tiemposEntreNotas[i] / 1000)
		suma += tiemposEntreNotas[i] / 1000
		
		t = bytearray(notas[i])	# Transformar cada array en un array de bytes
		ser.write(t)			# Y enviar el array de bytes
		
		print(" {}".format(getTiempo(suma) + " " + getLoadingBar(i, len(tiemposEntreNotas))) + " " + getTiempo(total), end="\r")
	
	time.sleep(10)	# Descansar después de la actuación
	for c in range(len(dispo)):	# Desactivar todos los canales
		t = bytearray([c // 3, (c % 3) + 9, 0])	# Preparar el mensaje
		ser.write(t)	# Enviar el mensaje
		time.sleep(0.2) # Dejar un poco de tiempo entre canal mensaje y mensaje

print("Fin")