# Uso: python thing_calculator.py o python thing_calculator "FRECUENCIA" -> Donde FREQUENCIA (en Hz) es la frecuencia del reloj conectado al AY 3 8910

import sys

between_freq = (2.0)**(1/float(12))		# (n+1)/n
base_freq = 16.35                       # C_0 [Hz]

freq_reloj = 2000000 # Frecuencia predeterminada es 1MHz

if (len(sys.argv) == 2): freq_reloj = int(sys.argv[1]) # Si ha especificado 

ranges = [] # Array de arrays que contiene minFreq, el valor a escribir en el registro es el index + 1

for i in range(31, 0, -1): # El registro de ruido va entre 1 y 31 (incluidos)
	ranges.append(freq_reloj/(16*i))

combis = []
def relleno(n):
	for i in range(n): combis.append(-1)

relleno(12)
for i in range(150): # 150 es un valor arbitrario, pero representa el valor de la nota más alta en midi (?) -> para que esta el relleno entonces ?
	f = base_freq*(between_freq**i)
	
	for j in range(len(ranges)):
		if (j == len(ranges) - 1):
			if (f > ranges[j]):
				combis.append(j + 1)
			else:
				combis.append(-1)
		else:
			if (f > ranges[j] and f < ranges[j + 1]):
				combis.append(j + 1)
				break

print("Python : ")
print(combis)

temp = str(combis)	# Added support for Java

print()
print()
print()
print("Java : ")
print(temp.replace('[', '{').replace(']', '}'))