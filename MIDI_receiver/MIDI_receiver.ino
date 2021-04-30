#define LED A5

char regs[3];

void setup() {
  pinMode(LED, OUTPUT);
  digitalWrite(LED, HIGH);
  delay(250);
  digitalWrite(LED, LOW);
  
  Serial.begin(31250);
}

void flashLED(char times) {
  for (char i = 0; i < times; i++) {
    digitalWrite(LED, HIGH);
    delay(250);
    digitalWrite(LED, LOW);
    delay(250);
  }
}

void loop() {
  while (Serial.readBytes(regs, 3) != 3); // Primer byte es el chip, segundo byte es el registro y el tercero es el valor
  if (regs[0] == 0x90 && regs[1] == 0x90 && regs[2] == 0x90)
    flashLED(3);
  else
    flashLED(1);
}
