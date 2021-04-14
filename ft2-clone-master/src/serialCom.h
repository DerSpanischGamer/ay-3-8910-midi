#include <stdint.h>

char* puertos;
uint8_t nPuertos;

void getPorts();
void togglePorts();
void hidePorts();
void showPorts();
void portPressed(int position);