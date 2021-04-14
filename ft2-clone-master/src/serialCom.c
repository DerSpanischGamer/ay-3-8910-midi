#include <stdlib.h>
#include <stdio.h>
#include <stdint.h> // I like small ints sorry girls

#include "ft2_structs.h"
#include "ft2_gui.h"

#define FILENAME_TEXT_X 170

FILE* portsFile;

char numPuertos[5];     // Stores the first line which has the number of ports
uint8_t nPuertos;       // Idem but as an int
char puertos[10][255];  // 10 string, each with 255 characters to write the ports that CAN be accessed

static void removeComa(char* s_in) {  // Remove coma at the end of the string
    uint8_t pos = 0;
    while (s_in[pos++] != ',') { }
    s_in[pos] = '\0';  // Null character at where was the coma
}

void getPorts()
{
    system("python ../../src/ports.py");

    Sleep(500); // Wait for python :>
    
    if ((portsFile = fopen("portList.txt", "r")) == NULL) {
        printf("Error loading the ports !");
        return;     // Nothing to do with ports if error upon reading
    }

    fgets(numPuertos, 5, portsFile);  // Get the first line that stores the number of ports availables

    nPuertos = atoi(numPuertos);

    uint8_t i = 0;
    while (fgets(puertos[i], 255, portsFile) != NULL) { removeComa(puertos[i++]); } // Keep the lines coming

    fclose(portsFile);
    return;
}

void portPressed(int position) {
    if (position > nPuertos - 1)
        return;

    connectPort(position);
}

void hidePorts(void)
{
    ui.diskOpShown = false;
    showTopScreen(true);
}

void drawPortsScreen() {
    // Draw the background
    drawFramework(0, 0, 172, 172, FRAMEWORK_TYPE1);     // Left
    drawFramework(172, 0, 121, 172, FRAMEWORK_TYPE1);   // Right
    
    clearRect(4, 4, 164, 164);      // Here we draw the texts (left)
    clearRect(176, 4, 113, 164);    // Nothing for now (right)

    for (uint16_t i = 0; i < 15; i++)
    {
        const uint16_t y = 6 + (i * (FONT1_CHAR_H + 1));    // Compute the height

        //textOut(4, y, PAL_BLCKTXT, puertos[i]);     // Draw the texts
        textOut(6, y, PAL_BLCKTXT, "HEY");
    }
}

void showPorts(void) {
    hideTopScreen();
    showTopRightMainScreen();
    drawPortsScreen();

    ui.diskOpShown = true;
}

void togglePorts(void) {
    if (ui.diskOpShown) // Using code from diskop.c hehehe
        hidePorts();
    else
        showPorts();
}

void connectPort(int port) {
    printf("Connecting");
}