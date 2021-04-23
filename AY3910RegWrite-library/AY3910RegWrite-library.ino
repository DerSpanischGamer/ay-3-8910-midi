// ----------- Variables -----------

char regs[3];

// ----------- Funciones -----------

void setup() {
  Serial.begin(115200);
}

void loop() {
  while (Serial.readBytes(regs, 3) != 3); // Primer byte es el chip, segundo byte es el registro y el tercero es el valor
  for (int i = 0; i < 3; i++)
    Serial.write(regs[i] + 1);
}
