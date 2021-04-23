#include <stdint.h>

char* puertos;
uint8_t nPuertos;

void closeSerialPort();

void getPorts();

void togglePorts(void);
void hidePorts();
void showPorts();

void writeRead(const char* dataOut, int outL, char* dataIn, int inL);
void write(const char* buffer, int length);

void updatePorts(void);

void portPressed(int position);