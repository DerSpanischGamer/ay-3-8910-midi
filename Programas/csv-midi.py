# Uso: python csv-midi.py archivoCSV


# Funciones:
#	B3 | B2 | B1 | B0
#
#	B3: Noise/~Normal    B2: Activar/~Desactivar    B1 y B0: Canal
#	
#	B3: 1 -> Acción el canal noise, 0 -> Accion sobre el canal normal
#	B2: 1 -> Activar el canal 0 -> Desactivar el canal
#	B1 y B0: 00: canal A, 01: canal B, 10: canal C

import sys, csv

pulsos = 24 # midiCLK / cuarto de nota
tempo = 0 	# ms / midiCLK

NMIinter = 17.448 # intervalo de tiempo entre señales NMI enviadas [ms]

canales = [-1, -1, -1] # Canales A, B, C si están utilizados o no

bin = [[], [], []] # bytes guardando en orden: tiempo entre el actual y el siguiente, 8 high bits contiene el comando y los 4 bits superiores de la nota, despues los 8 bites inferiores con el resto de la info de la nota

archivoEntrada = sys.argv[1]

combis = [[-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [-1, -1], [14, 238], [14, 24], [13, 77], [12, 142], [11, 218], [11, 47], [10, 143], [9, 247], [9, 104], [8, 224], [8, 97], [7, 232], [7, 119], [7, 12], [6, 166], [6, 71], [5, 237], [5, 151], [5, 71], [4, 251], [4, 180], [4, 112], [4, 48], [3, 244], [3, 187], [3, 134], [3, 83], [3, 35], [2, 246], [2, 203], [2, 163], [2, 125], [2, 90], [2, 56], [2, 24], [1, 250], [1, 221], [1, 195], [1, 169], [1, 145], [1, 123], [1, 101], [1, 81], [1, 62], [1, 45], [1, 28], [1, 12], [0, 253], [0, 238], [0, 225], [0, 212], [0, 200], [0, 189], [0, 178], [0, 168], [0, 159], [0, 150], [0, 142], [0, 134], [0, 126], [0, 119], [0, 112], [0, 106], [0, 100], [0, 94], [0, 89], [0, 84], [0, 79], [0, 75], [0, 71], [0, 67], [0, 63], [0, 59], [0, 56], [0, 53], [0, 50], [0, 47], [0, 44], [0, 42], [0, 39], [0, 37], [0, 35], [0, 33], [0, 31], [0, 29], [0, 28], [0, 26], [0, 25], [0, 23], [0, 22], [0, 21], [0, 19], [0, 18], [0, 17], [0, 16], [0, 15], [0, 14], [0, 14], [0, 13], [0, 12], [0, 11], [0, 11], [0, 10], [0, 9], [0, 9], [0, 8], [0, 8], [0, 7], [0, 7], [0, 7], [0, 6], [0, 6], [0, 5], [0, 5], [0, 5], [0, 4], [0, 4], [0, 4], [0, 4], [0, 3], [0, 3], [0, 3], [0, 3], [0, 3], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 2], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 1], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0]]

ultimaVez = 0
line_count = 0

def getCanalLibre():
	for i in range(len(canales)):
		if (canales[i] == -1): return i
	return -1

def getCanal(nota):
	for i in range(len(canales)):
		if (canales[i] == nota): return i
	return 255 # o -1, la cosa es que tiene que dar error

def anadirNote(tiempo, nota):
	if (getCanalLibre() == -1): return # Ningún canal libre, ignorar nota
	global line_count, ultimaVez
	line_count += 1
	
	dif_tiempo = int(tiempo - ultimaVez)
	while (dif_tiempo >= 256 and line_count <= 252):
		bin[0].append(255)
		bin[1].append(0)
		bin[2].append(240) # 240 = 0b11110000
		
		dif_tiempo -= 256
		line_count += 1

	bin[0].append(int(dif_tiempo))
	bin[1].append(combis[nota][1])
	
	comando = 4 # 4 = 0b0100
	comando |= getCanalLibre()
	canales[getCanalLibre()] = nota
	bin[2].append((comando << 4) | combis[nota][0])
	
	ultimaVez = tiempo
	
def quitarNote(tiempo, nota):
	global line_count, ultimaVez, canales
	line_count += 1
	
	dif_tiempo = int(tiempo - ultimaVez)
	
	while (dif_tiempo >= 256 and line_count <= 252):
		bin[0].append(255)
		bin[1].append(0)
		bin[2].append(240) # 240 = 0b11110000
		
		dif_tiempo -= 256
		line_count += 1

	bin[0].append(int(dif_tiempo))
	bin[1].append(0)
	
	comando = 0 # 4 = 0b0000
	comando |= getCanal(nota)
	canales[getCanal(nota)] = -1
	bin[2].append((comando << 4) | combis[nota][0])
	if (((comando << 4) | combis[nota][0]) > 256): print(getCanal(nota))
	
	ultimaVez = tiempo

with open(archivoEntrada) as csv_file:
	csv_reader = csv.reader(csv_file, delimiter=',')
	for row in csv_reader:
		if (row[2] == " Header"): pulsos = int(row[5])
		if (tempo == 0 and int(row[1]) == 0 and row[2] == " Tempo"): tempo = int(row[3])/(pulsos * 1000) # ms/clck para obtener ms multiplicar por el momento
		
		if (row[2] == " Note_on_c"):
			anadirNote(int(row[1]) * (tempo/NMIinter), int(row[4]))
		elif (row[2] == " Note_off_c"):
			quitarNote(int(row[1]) * (tempo/NMIinter), int(row[4]))
		else: continue
		
		if (line_count == 253): break


# acabar las notas para que no suenen hasta el infinito
for i in range(len(canales)):
	if (canales[i] != -1): quitarNote(ultimaVez + 60, canales[i])

for i in range(len(bin)):
	while (len(bin[i]) < 256): bin[i].append(64)

rom = bytearray([0] * 8191) #  Se escriben los datos de bin en el mismo orden que estan arriba
for i in range(len(bin)):
	for j in range(len(bin[i])):
		rom[(i*len(bin[i])) + j] = bin[i][j]

with open("audio.bin", "wb") as out_file:
	out_file.write(rom)

print(bin[0])
print(len(bin[0]))