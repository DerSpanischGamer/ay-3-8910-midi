#include "Amadeus.h"

// ----------- Constantes -----------

// Valores de cada chip
#define PRIMERO 0
#define SEGUNDO 1

#define volumen B00001111 // valores entre B00000000 Y B00001111

// Registros
#define REG_LVL_A 8
#define REG_LVL_A 9
#define REG_LVL_A 10

// ----------- Variables -----------

char regs[3];

Amadeus amadeus = Amadeus(); // Iniciar la clase Amadeus

// ----------- Funciones -----------

void setup() {
  amadeus.begin();
}

void loop() {
  while (Serial.readBytes(regs, 3) != 3); // Primer byte es el chip, segundo byte es el registro y el tercero es el valor
  for (int i = 0; i < 3; i++)
    Serial.write(regs[i] + 1);
}
