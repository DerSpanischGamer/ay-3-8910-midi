#include "Amadeus.h"

// ----------- Variables -----------

char regs[3];

Amadeus amadeus = Amadeus(); // Iniciar la clase Amadeus

// ----------- Funciones -----------

void setup() {
  Serial.begin(115200);
  amadeus.begin();
}

void loop() {
  while (Serial.readBytes(regs, 3) != 3); // Primer byte es el chip, segundo byte es el registro y el tercero es el valor
    amadeus.out(regs[0], regs[1], regs[2]); // Escribir en el chip regs[0], en el registro regs[1] el valor [2]
}
