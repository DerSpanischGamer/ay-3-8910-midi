#include <stdint.h>

char* puertos;
uint8_t nPuertos;

void closeSerialPort();

void getPorts();

void togglePorts(void);
void hidePorts();
void showPorts();

void write(const char* buffer);

void updatePorts(void);

void portPressed(int position);