#define LED A5    // PC5
#define BOTON 10  // PB2

void setup() {
 pinMode(LED, OUTPUT);
 pinMode(BOTON, INPUT);

  Serial.begin(112500);

  delay(1000);

  Serial.write("Serial ok");
}

void loop() {
  if (digitalRead(BOTON))
    digitalWrite(LED, HIGH);
  else
    digitalWrite(LED, LOW);
}
