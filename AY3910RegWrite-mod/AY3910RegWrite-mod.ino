// CODIGO ORIGINAL: https://github.com/986-Studio/AY-3-3910-Player/blob/master/AY3910RegWrite/AY3910RegWrite.ino


/*******************************************************************
 *               AY-3-3910 Register writer for Arduino
 *                 (c) 2014 Manoel "Godzil" Trapier
 *
 * All the code is made by me apart from the the timer code that 
 * was inspired from code found on the internet. I'm sorry, I can't
 * remmember where.
 **************************** Licence ******************************
 * This file is licenced under the licence:
 *                    WTFPL v2 Postal Card Edition:
 *
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *                    Version 2, December 2004
 *
 * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this license document, and changing it is allowed as long
 * as the name is changed.
 *
 *            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO.
 *  1. If you like this software you can send me a (virtual) postals
 *     card. Details bellow:
 *
 *             < godzil-nospambot at godzil dot net >
 *
 * If you want to send a real postal card, send me an email, I'll
 * give you my address. Of course remove the -nospambot from my
 * e-mail address.
 *
 ******************************************************************/
#define volumen B00001111 // valores entre B00000000 Y B00001111

const int freqOutputPin = 11;   // OC2A output pin for ATmega328 boards
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
const int ocr2aval  = 3; 
// The following are scaled for convenient printing
//
void setup_clock()
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

// Funciones
enum { INACTIVE = B00, READ = B01, WRITE = B10, ADDRESS = B11};

// Masks para los pines analógicos
enum { PRIMERO = 0, SEGUNDO = 1 }; 

// Configura el modo en el que hay que poner los pines conectados a los pines DA0 -> DA7
void setup_data(int mode)
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
void setup_control(char modo)
{
    DDRC |= modo;  // Hacer que los pines analógicos A0 -> A3 sean outputs
    PORTC &= ~modo;      // Escribir todo 1 en todos los pines para que espere una direccion que vamos a mandar después
}

// Escibir en los pines analógicos para elegir la funcion del YM2149
/*
 * 
 */
void set_control(char chip, char mode)
{
  if (chip == 0) { // Primer ym2149 y no necesitamos cambiar nada
    PORTC = mode; // Es MUY importante que SOLO escribamos mode porque si ponemos 1 en los otros pines analógicos, NO estaremos en el modo inactivo del otro chip por lo que la hemos jodido
  } else {
    PORTC = (mode << 2); // Igual de importante, pero ahora estamos haciendo que el segundo chip este en el modo que queremos. El primer chip será puesto a inactivo si no lo estaba ya
  }
}

// Poner los datos en el bus. Hay que recordar que los dos bits inferiores del puerto B corresponde a los dos inferiores de los datos, y lo contrario para el puerto D
void SetData(unsigned char data)
{
  PORTD = data & 0xFC; //11111100
  PORTB = data & 0x03; //00000011
} 

// Leer del bus
unsigned char GetData(void)
{
  return (PORTD & 0xFC) | (PORTB & 0x03); 
}


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



void write_2149_reg(char chip, uint8_t reg, uint8_t value)
{
  // Primero escrbir la dirección
  setup_data(ADDRESS);    // Poner la direccion en los registros del bus
  SetData(reg & 0x0F);    // Configurar los pines del bus para que sean output
  set_control(chip, ADDRESS);   // Poner los pines analogicos que controlan la funcion en modo direccion
  delayMicroseconds(3);   // idk, tining and shit
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

// Leer un registro, imagino que no hace falta poner la dirección ? O eso, o me estoy perdiendo algo
uint8_t read_2149_reg(uint8_t reg)
{
  uint8_t ret = 0;

  return ret;
}

// Definir las funciones
#define DESACTIVAR B10000000
#define ACTIVAR B01000000

#define A B00000001
#define B B00000010
#define C B00000100

#define A_2 B00001000
#define B_2 B00010000
#define C_2 B00100000


void orden(char a, char b, char c) {

  char chip = (a & B00111000) == 0 ? PRIMERO : SEGUNDO; // Si alguno de los tres bits de "en medio" no son 0, entonces es una orden para el segundo chip
  
  switch (a)
  {
    // Acciones que tienen que ver con el primer chip
    case ACTIVAR | A:
      write_2149_reg(chip, REG_LVL_A, volumen);
      write_2149_reg(chip, REG_FREQ_A_LO, b);
      write_2149_reg(chip, REG_FREQ_A_HI, c);
      break;
    case ACTIVAR | B:
      write_2149_reg(chip, REG_LVL_B, volumen);
      write_2149_reg(chip, REG_FREQ_B_LO, b);
      write_2149_reg(chip, REG_FREQ_B_HI, c);
      break;
    case ACTIVAR | C:
      write_2149_reg(chip, REG_LVL_C, volumen);
      write_2149_reg(chip, REG_FREQ_C_LO, b);
      write_2149_reg(chip, REG_FREQ_C_HI, c);
      break;

    // Acciones que tienen que ver con el segundo chip
    case ACTIVAR | A_2:
      write_2149_reg(chip, REG_LVL_A, volumen);
      write_2149_reg(chip, REG_FREQ_A_LO, b);
      write_2149_reg(chip, REG_FREQ_A_HI, c);
      break;
    case ACTIVAR | B_2:
      write_2149_reg(chip, REG_LVL_B, volumen);
      write_2149_reg(chip, REG_FREQ_B_LO, b);
      write_2149_reg(chip, REG_FREQ_B_HI, c);
      break;
    case ACTIVAR | C_2:
      write_2149_reg(chip, REG_LVL_C, volumen);
      write_2149_reg(chip, REG_FREQ_C_LO, b);
      write_2149_reg(chip, REG_FREQ_C_HI, c);
      break;
  
    case DESACTIVAR| A:
     write_2149_reg(chip, REG_LVL_A, 0);
     break;
    case DESACTIVAR | B:
     write_2149_reg(chip, REG_LVL_B, 0);
     break;
    case DESACTIVAR | C:
     write_2149_reg(chip, REG_LVL_C, 0);
     break;
  
    // Este parte del código SEGURO que se puede optimizar, pero me da pereza hacerlo.... TODO: optimizar
    case DESACTIVAR| A_2:
     write_2149_reg(chip, REG_LVL_A, 0);
     break;
    case DESACTIVAR | B:
     write_2149_reg(chip, REG_LVL_B, 0);
     break;
    case DESACTIVAR | C:
     write_2149_reg(chip, REG_LVL_C, 0);
     break;
  }
}

void setup()
{
  setup_clock();
  
  setup_control(B00001111);
  setup_data(INACTIVE);
 
  Serial.begin(115200);

  write_2149_reg(PRIMERO, REG_IO_MIXER, B00111000); // Activar solo los de tune
  write_2149_reg(SEGUNDO, REG_IO_MIXER, B00111000); // Activar solo los de tune
  
  // Be sure to kill all possible sound by setting volume to zero

  write_2149_reg(PRIMERO, REG_LVL_A, 0);
  write_2149_reg(PRIMERO, REG_LVL_B, 0);
  write_2149_reg(PRIMERO, REG_LVL_C, 0);
  
  write_2149_reg(SEGUNDO, REG_LVL_A, 0);
  write_2149_reg(SEGUNDO, REG_LVL_B, 0);
  write_2149_reg(SEGUNDO, REG_LVL_C, 0);
  
}

char regs[3];

void loop()
{
  while (Serial.readBytes(regs, 3) != 3); // Primer byte es la orden segundo byte es el valor lower y segundo es el higher
  orden(regs[0], regs[1], regs[2]); 
}
