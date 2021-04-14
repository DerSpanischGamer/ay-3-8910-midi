#include <stdint.h>

char* puertos;
uint8_t nPuertos;

void getPorts();
void togglePorts(void);
void updatePorts(void);
void hidePorts();
void showPorts();
void portPressed(int position);