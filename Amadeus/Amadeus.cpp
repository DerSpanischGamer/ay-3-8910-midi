#include <stdint.h>
#include <Arduino.h>
#include "Amadeus.h"

// -------------- VARIABLES Y CONSTANTES -------------------

#define volumen B00001111 // valores entre B00000000 Y B00001111

#define VERSION "v1.1"

const int Amadeus::freqOutputPin = 11;   // OC2A output pin for ATmega328 boards
// Constants are computed at compile time
// If you change the prescale value, it affects CS22, CS21, and CS20
// For a given prescale value, the eight-bit number that you
// load into OCR2A determines the frequency according to the
// following formulas:
//
// With no prescaling, an ocr2val of 3 causes the output pin to
// toggle the value every four CPU clock cycles. That is, the
// period is equal to eight slock cycles.
//
// With F_CPU = 16 MHz, the result is 2 MHz.
//
// Note that the prescale value is just for printing; changing it here
// does not change the clock division ratio for the timer!  To change
// the timer prescale division, use different bits for CS22:0 below
const int Amadeus::ocr2aval  = 3; 
// The following are scaled for convenient printing

// Funciones
enum { INACTIVE = 0, READ = 1, WRITE = 2, ADDRESS = 3};

// Masks para los pines analógicos
enum { PRIMERO = 0, SEGUNDO = 1 }; 

/* Registers */
enum
{
  REG_FREQ_A_LO = 0,
  REG_FREQ_A_HI,
  REG_FREQ_B_LO,
  REG_FREQ_B_HI,
  REG_FREQ_C_LO,
  REG_FREQ_C_HI,
  
  REG_FREQ_NOISE,
  REG_IO_MIXER,
  
  REG_LVL_A,
  REG_LVL_B,
  REG_LVL_C,
  
  REG_FREQ_ENV_LO,
  REG_FREQ_ENV_HI,
  REG_ENV_SHAPE,
  
  REG_IOA,
  REG_IOB
};

// -------------- CONSTRUCTOR -------------------

Amadeus::Amadeus() { }

// -------------- FUNCIONES ------------------

void Amadeus::begin() {
	  setup_clock(); // Empezar el reloj
  
	  setup_control(B00001111);
	  setup_data(INACTIVE);
	 
	  Serial.begin(115200);
	  // Be sure to kill all possible sound by setting volume to zero

	  out(PRIMERO, REG_LVL_A, 0);
	  out(PRIMERO, REG_LVL_B, 0);
	  out(PRIMERO, REG_LVL_C, 0);
	  
	  out(SEGUNDO, REG_LVL_A, 0);
	  out(SEGUNDO, REG_LVL_B, 0);
	  out(SEGUNDO, REG_LVL_C, 0);


	// Poner los chips en modo tunes
	 out(PRIMERO, REG_IO_MIXER, B00111000);
	 out(SEGUNDO, REG_IO_MIXER, B00111000);

}

void Amadeus::setup_clock()
{
    pinMode(freqOutputPin, OUTPUT); 
    // Set Timer 2 CTC mode with no prescaling.  OC2A toggles on compare match
    //
    // WGM22:0 = 010: CTC Mode, toggle OC 
    // WGM2 bits 1 and 0 are in TCCR2A,
    // WGM2 bit 2 is in TCCR2B
    // COM2A0 sets OC2A (arduino pin 11 on Uno or Duemilanove) to toggle on compare match
    //
    TCCR2A = ((1 << WGM21) | (1 << COM2A0));
    // Set Timer 2  No prescaling  (i.e. prescale division = 1)
    //
    // CS22:0 = 001: Use CPU clock with no prescaling
    // CS2 bits 2:0 are all in TCCR2B
    TCCR2B = (1 << CS20);
    // Make sure Compare-match register A interrupt for timer2 is disabled
    TIMSK2 = 0;
    // This value determines the output frequency
    OCR2A = ocr2aval;
}

// Configura el modo en el que hay que poner los pines conectados a los pines DA0 -> DA7
void Amadeus::setup_data(int mode)
{
  switch(mode)
  {
    default:
    case READ:
    case INACTIVE:
      DDRD = B00000000; // Hacer que todos los pines del puerto D sean inputs (enviaran los 6 bits superiores de los datos [DA2 -> DA7])
      DDRB &= ~0x03;    // Hacer que los pines del puerto B conectados a D0 y D1 sean inputs
      break;            // Hacemos que sean inputs para que en el caso de leer, leer los datos, y para inactivo, no poner nada en el bus por accidente.
    case ADDRESS:
    case WRITE:
      DDRD = B11111111; // Hacer justo lo contrario que antes, poner todos en outputs para controlar el bus.
      DDRB |= 0x03;
      break;
  }
}


// Configurar los pines de control (analógicos) (Solo se ejecuta una vez al principio)
/*
 * Que usar para modo:
 *    DOS YM2149: B00001111
 *    UN  YM2149: B00000011
 */
void Amadeus::setup_control(char modo)
{
    DDRC |= modo;  // Hacer que los pines analógicos A0 -> A3 sean outputs
    PORTC &= ~modo;      // Escribir todo 1 en todos los pines para que espere una direccion que vamos a mandar después
}

// Escibir en los pines analógicos para elegir la funcion del YM2149
/*
 * 
 */
void Amadeus::set_control(char chip, char mode)
{
  if (chip == 0) { // Primer ym2149 y no necesitamos cambiar nada
    PORTC = mode; // Es MUY importante que SOLO escribamos mode porque si ponemos 1 en los otros pines analógicos, NO estaremos en el modo inactivo del otro chip por lo que la hemos jodido
  } else {
    PORTC = (mode << 2); // Igual de importante, pero ahora estamos haciendo que el segundo chip este en el modo que queremos. El primer chip será puesto a inactivo si no lo estaba ya
  }
}

// Poner los datos en el bus. Hay que recordar que los dos bits inferiores del puerto B corresponde a los dos inferiores de los datos, y lo contrario para el puerto D
void Amadeus::SetData(unsigned char data)
{
  PORTD = data & 0xFC; //11111100
  PORTB = data & 0x03; //00000011
} 

// Leer del bus
unsigned char Amadeus::GetData(void)
{
  return (PORTD & 0xFC) | (PORTB & 0x03); 
}

void Amadeus::out(char chip, uint8_t reg, uint8_t value)
{
  // Primero escrbir la dirección
  setup_data(ADDRESS);    		// Poner la direccion en los registros del bus
  SetData(reg & 0x0F);    		// Configurar los pines del bus para que sean output
  set_control(chip, ADDRESS);   // Poner los pines analogicos que controlan la funcion en modo direccion
  delayMicroseconds(3);   		// idk, timing and shit
  set_control(chip, INACTIVE);  // Poner los pines analogicos en inactivo para que no sobreescriban el valor o algo

  // Después escribir el valor en la dirección que hemos elegido antes
  delayMicroseconds(1);   // Timing bullshit
  setup_data(WRITE);      // Poner lo que queremos escribir en el registro del bus
  SetData(value);         // Hacer que el bus muestre el registro (es tímido)
  delayMicroseconds(1);   // Bruh
  
  set_control(chip, WRITE);     // Dar la orden de escribir
  
  delayMicroseconds(5);   // BRUUUUUUUUUH
  
  set_control(chip, INACTIVE);  // Dejar de ser mandón y quitar la orden para no sobreescribir el registro
  PORTD = 0;                    // Borrar el registro del bus para no dejar pruebas (aunque hemos dejado pruebas en el puerto B, para hacerlo bien deberíamos hacer setup_data(0);)
  //setup_data(INACTIVE);       // A mí también me gustaría saber por qué esto está comentado :(
}

void Amadeus::versionOut() { Serial.write(VERSION); }

// Leer un registro, imagino que no hace falta poner la dirección ? O eso, o me estoy perdiendo algo
uint8_t Amadeus::read_2149_reg(uint8_t reg)
{
  uint8_t ret = 0;

  return ret;
}