#include <Amadeus.h>

#define MODO B00000100 // PB2

// ----------- Variables -----------

char regs[3];

char usb = MODO;  // Variable que guarda el modo (usb != 0 para recibir desde usb, usb = 0 para 

Amadeus amadeus = Amadeus(); // Iniciar la clase Amadeus

// ----------- Funciones -----------

void setup() {
  PORTB &= ~MODO;           // Poner el boton como input
  amadeus.begin();
}

void loop() {
  while (Serial.readBytes(regs, 3) != 3) {  // Primer byte es el chip, segundo byte es el registro y el tercero es el valor
    if ((PINB & MODO) ^ usb) {  // Mirar si el boton ha sido apretado //TODO: arreglar porque lo que hace ahora es, si esta pulsado el boton, cambia de estado, y no que cambie cuando pulsas
      usb = PINB & MODO;        // Si lo ha sido, actualizarlo
    
      // TODO: cambiar el tipo de Serial
    }
}
  
  
  if (usb)  // Si la variable usb != 0 entonces estamos en modo usb
    amadeus.out(regs[0], regs[1], regs[2]); // Escribir en el chip regs[0], en el registro regs[1] el valor [2]
  //else
    // MIDI
}

void sendMidiNote() { // Pone en un canal la nota que ha sido recibida
  
}


void cutMidiNote() { // Apaga el canal en el que esta sonando la nota
  
}
