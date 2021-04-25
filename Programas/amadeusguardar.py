# Uso: python amadeusguardar.py "ARCHIVO.csv" "SALIDA.amds"

# -------- Guarda un archivo .csv que venga de un archivo midi (una canción), en un archivo .amds que pueda ser leido por el programa de edicion de musica para la placa Amadeus (o similar)

import sys
import csv
import numpy as np
import copy

# ------------ CONSTANTES ------------

volumenMax = 15

# ------------ VARIABLES ------------

# Combis
combis = [[-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [15, 209], [14, 238], [14, 24], [13, 77], [12, 142], [11, 218], [11, 47], [10, 143], [9, 247], [9, 104], [8, 224], [8, 97], [7, 232], [7, 119], [7, 12], [6, 166], [6, 71], [5, 237], [5, 151], [5, 71], [4, 251], [4, 180], [4, 112], [4, 48], [3, 244], [3, 187], [3, 134], [3, 83], [3, 35], [2, 246], [2, 203], [2, 163], [2, 125], [2, 90], [2, 56], [2, 24], [1, 250], [1, 221], [1, 195], [1, 169], [1, 145], [1, 123], [1, 101], [1, 81], [1, 62], [1, 45], [1, 28], [1, 12], [0, 253], [0, 238], [0, 225], [0, 212], [0, 200], [0, 189], [0, 178], [0, 168], [0, 159], [0, 150], [0, 142], [0, 134], [0, 126], [0, 119], [0, 112], [0, 106], [0, 100], [0, 94], [0, 89], [0, 84], [0, 79], [0, 75], [0, 71], [0, 67], [0, 63], [0, 59], [0, 56], [0, 53], [0, 50], [0, 47], [0, 44], [0, 42], [0, 39], [0, 37], [0, 35], [0, 33], [0, 31], [0, 29], [0, 28], [0, 26], [0, 25], [0, 23], [0, 22], [0, 21], [0, 19], [0, 18], [0, 17], [0, 16], [0, 15], [0, 14], [0, 14], [0, 13], [0, 12], [0, 11], [0, 11], [0, 10], [0, 9], [0, 9], [0, 8], [0, 8], [0, 7], [0, 7], [0, 7], [0, 6], [0, 6], [0, 5], [0, 5], [0, 5], [0, 4], [0, 4], [0, 4], [0, 4], [0, 3], [0, 3], [0, 3], [0, 3], [0, 3], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1]]

# Otras variables

archivo = sys.argv[1]	# Guarda el nombre del archivo .csv que se abre

salida = sys.argv[2]	# Guarda el nombre del archivo .amds

# Tratar un poco la salida para que no aparezcan dos veces el mismo .amds o .csv.amds
if (salida.endswith(".amds")):
	salida = salida[:-5]
elif (salida.endswith(".csv")):
	salida = salida[:-4]

volumen = volumenMax	# Guarda el volumen al que se tienen que poner los canales una vez que estén activos (el predeterminado es el máximo)

preNotas = [] # Guarda un array con todas las notas que hay, aunque sean de pistas diferentes. Es un array de arrays y cada array tiene [Tiempo, 1 = encender : 0 = apagar, nota]

tiempos = []					# Guarda todos los tiempos para hacer los numeros más pequeños
notas = [[], []] 				# Array de 2 arrays (0 = primero, 1 = segundo) de arrays de 1 int + 14 bytes que guardan cada uno el valor del momento + el valor de cada registor en ese momento
tiemposEntreNotas = [] 			# Delay que hay entre cambios de notas

canalesUtilizados = 0	# Utilizado para ver cuantos canales estan abiertos en un momento dado

ultimavez = 0 # Ultimo cambio entre activar / desactivar una nota

tempo = 0 # Guarda los milisegundos / midi clock pulses, para obtener los milisegundos solo hay que multiplicar por los clk pulses. Tambien se puede usar como la "resolución" del tiempo

dispo = [-1, -1, -1, -1, -1, -1] # Guarda la nota que se esta tocando en cada canal (los 3 primeros son los canales A, B y C [en ese order] del primer chip, y después el mismo orden A, B y C del segundo chip)

pulsos = 24 # Pulsos cada cuarto de nota

preguntar  = False # Preguntar si desea continuar en caso de que la cancion pueda no sonar bien

# ------------ FUNCIONES ---------------

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

# Anade una nota al array notas (duh)
def anadirNota(tiempo, nota):
	global ultimavez, volumen
	
	if (tiempo - ultimavez < 0): return     # Sanity check
	
	posicion = getCanalDisponible()	# Coger el primer canal que este disponible
	if (posicion == -1): return		# Si no hay disponibles, i.e. la funcion devuelve -1, no hay nada que hacer, entonces no se guarda la nota
	
	chip = posicion // 3	# El chip es el resultado de la division euclidiana (o entera) [posicion < 3 -> // 3 = 0 -> primer chip; posicion >= 3 -> // 3 = 1 -> segundo chip]
	canal = posicion % 3	# canal {0, 1, 2}
	
	tiemposEntreNotas.append(tiempo - ultimavez)	# Contar cuanto ha pasado desde la ultima nota y ponerlo en la lista de espera
	tiemposEntreNotas.append(0)
	tiemposEntreNotas.append(0)
	ultimavez = tiempo	# Hacer que esta sea la ultima nota guardando este "instante" como el ultimo
	
	dispo[posicion] = nota # Indicar que el canal no esta disponible quitando el -1 y poniendo la nota que esta sonando

	if (notas[chip][-1][0] == tiempo):
		notas[chip][-1][(canal * 2) + 1] = combis[nota][1]
		notas[chip][-1][(canal * 2) + 2] = combis[nota][0]
		notas[chip][-1][canal + 9] = volumen
	else:
		ultima = notas[chip][-1][:]	# Coger el ultimo cambio que paso

		tiempos.append(tiempo)
		
		ultima[0] = tiempo								# Poner el nuevo tiempo
		ultima[(canal * 2) + 1] = combis[nota][1]		# Poner la parte inferior de la nota
		ultima[(canal * 2) + 2] = combis[nota][0]		# Poner la parte superior de la nota
		ultima[canal + 9] = volumen						# Poner el volumen al nivel seleccionado
		
		notas[chip].append(ultima)						# Guardar la versión modificada de la nota

# Quita la nota y pone el canal como disponible
def quitarNota(tiempo, nota):
	global ultimavez
	if (tiempo - ultimavez < 0): return		# Sanity check
	
	posicion = getCanalNoDispo(nota)		# Coger el canal que esta tocando la nota nota
	if (posicion == -1): return				# Si por alguno razon algo fuera mal, pararse
	
	chip = posicion // 3
	canal = posicion % 3
	
	dispo[posicion] = -1	# Marcar el canal como disponible
	
	# Para desactivar un canal es facil, solamente ponemos el volumen a 0 :)
	if (notas[chip][-1][0] == tiempo): # Si el tiempo es el mismo, de nada sirve añadir otro momento con el mismo tiempo, solo hay que modificar los registros necesarios
		notas[chip][-1][(canal * 2) + 1] = 0
		notas[chip][-1][(canal * 2) + 2] = 0
		notas[chip][-1][canal + 9] = 0
	else:								# Si el tiempo es diferente, sí que hay que añadir otra "entrada"
		ultima = notas[chip][-1][:]
		
		tiempos.append(tiempo)
		
		ultima[0] = tiempo				# Guardar el tiempo en el que hay que silenciarlo
		ultima[(canal * 2) + 1] = 0		# Poner la frecuencia a 0
		ultima[(canal * 2) + 2] = 0		# Poner la frecuencia a 0
		ultima[canal + 9] = 0			# Poner el volumen a 0
		
		notas[chip].append(ultima)		# Añadir la nueva version actualizada

# ------------ CODIGO ------------

# Preparar el midi
with open(archivo) as csv_file:
	csv_reader = csv.reader(csv_file, delimiter=',')
	
	# Pasar por todo el archivo .csv
	for row in csv_reader:
		if (row[2] == " Header"): pulsos = int(row[5])	# Si es el Header, coger la información necesaria (los pulsos)
		if (tempo == 0 and int(row[1]) == 0 and row[2] == " Tempo"):
			ogTime = int(row[3])
			tempo = int(row[3])/(pulsos * 1000) # ms/clck para obtener ms multiplicar por el momento
		
		if (row[2] == " Note_on_c"):
			preNotas.append([int(row[1]), 1, int(row[4])])
		elif (row[2] == " Note_off_c"):
			preNotas.append([int(row[1]), 0, int(row[4])])
		else: continue

preNotasNP = np.asarray(preNotas) 			# Pasar el array preNotas a un array de Numpy
preNotasNP[preNotasNP[:, 0].argsort()]		# Ordenar el array gracias a numpy

notas[0].append([0] * 15)   # El estado inicial de todos los registros del primer chip
notas[1].append([0] * 15)   # El estado inicial de todos los registros del segundo chip

for nota in preNotasNP:
	if (nota[1] == 1): anadirNota(nota[0], int(nota[2]))
	else: quitarNota(nota[0], int(nota[2]))

if (preguntar): # Preguntar si quiere continuar
	if (input("El archivo introducido prodría sonar mal debido a la falta de canales, ¿quieres continuar? (s/n): \n") != "s"): quit()
else:
	input("Canción lista, dale a Enter para guardar. \n")

# TODO: Hacer aqui que las notas se pongan juntas
a = 0;
b = 0;

finalNotas = []
finalNotas.append([0] * 29) # Estado inicial de todos los registros


# LO QUE HAY DEBAJO ES FEO DE COJONES PERO FUNCIONA, TOOD: ARREGLAR EL TIMESTAMP PARA QUE PUEDA SER LEIDO POR EL OTRO PROGRAMA


while a < len(notas[0]) and b < len(notas[1]):          # Mientras no hayamos pasado todas las notas al array finalNotas
    if a == len(notas[0]):                              # El array notas[0] ya ha sido copiado por completo, faltan las notas del array notas[1]
        if finalNotas[-1][0] != notas[1][b][0]:         # Si el timestamp de la nota es diferente
            temp = copy.deepcopy(finalNotas[-1])                       # Copiar el estado anterior (para solo cambiar los registros que son diferentes)
            temp[0] = notas[1][b][0]                    # Actualizar el timestamp
            finalNotas.append(temp)
            
        for i in range(15, 29):                         # Como estamos en el segundo chip, tenemos que cambiar los ultimos registros
            finalNotas[-1][i] = notas[1][b][i - 14]          # Copiar los registros
        b += 1
    elif b == len(notas[1]):
        if finalNotas[-1][0] != notas[0][a][0]:
            temp = copy.deepcopy(finalNotas[-1])
            temp[0] = notas[0][a][0]
            finalNotas.append(temp)
            
        for i in range(1, 15):
            finalNotas[-1][i] = notas[0][a][i]
        a += 1
    else:                                                   # En este caso significa que aun quedan notas por copiar de los dos arrays (notas[0] y notas[1])
        if notas[0][a][0] <= notas[0][b][0]:                    # Si el array notas[0] pasa antes
            if finalNotas[-1][0] != notas[0][a][0]:             # Si el timestamp es diferente
                temp  = copy.deepcopy(finalNotas[-1])                          # Primero copiar el ultimo estado
                temp[0] = notas[0][a][0]              # Actualizar el timestamp
                finalNotas.append(temp)
            
            for i in range(1, 15):
                finalNotas[-1][i] = notas[0][a][i]
            a += 1
        elif notas[0][a][0] > notas[0][b][0]:
            if finalNotas[-1][0] != notas[1][b][0]:
                temp = copy.deepcopy(finalNotas[-1])
                temp[0] = notas[0][a][0]
                finalNotas.append(temp)
            
            for i in range(15, 29):
                finalNotas[-1][i] = notas[1][b][i - 14]
            b += 1

# Guardar array
with open(salida + ".amds", mode = 'w', newline = '') as output:
	writer = csv.writer(output, delimiter = ',', quotechar = "'", quoting = csv.QUOTE_MINIMAL)

	# Escribir información sobre la canción que hemos guardado
	writer.writerow(["INFOS", int(round((60 * 1000000) / ogTime, 0)) , 2]) 	# El primer valor guarda primero el string INFO para que de error si se intenta parsear, el segundo valor es el tempo en BPM, el tercer valor indica el número de chips
	
	# Escribir toda la información de lo que hacen los dos chips
	for nota in finalNotas:
		writer.writerow(nota)
        
print("Fin")