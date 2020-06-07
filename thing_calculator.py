# Uso: python thing_calculator.py o python thing_calculator "FRECUENCIA" -> Donde FREQUENCIA (en Hz) es la frecuencia del reloj conectado al AY 3 8910

import sys

between_freq = (2.0)**(1/float(12))		# (n+1)/n
base_freq = 16.35                       # C_0

freq_reloj = 2000000 # Frecuencia predeterminada es 1MHz

if (len(sys.argv) == 2): freq_reloj = int(sys.argv[1]) # Si ha especificado 

def buscarFreq(f):
	TP = int(freq_reloj/(16*f))
	
	for j in range(16): # CT
		for i in range(256): # FT
			if ((256 * j) + i == TP): return [j, i]
	return [-1, -1]

combis = []
def relleno(n):
	for i in range(n): combis.append([-1, -1])

relleno(12)
for i in range(150): # 150 es un valor arbitrario, pero representado el valor de la nota más alta en midi (?)
	combis.append(buscarFreq(base_freq*between_freq**i))

print(combis)