#include <stdlib.h>
#include <stdio.h>
#include <stdint.h> // I like small ints sorry girls

#include "ft2_structs.h"
#include "ft2_gui.h"

#define FILENAME_TEXT_X 170

#define MAXPUERTOS 10

FILE* portsFile;

uint8_t nPuertos = 0;
char puertos[MAXPUERTOS][255];

// ---- FUNCTION DECLARATION
static void drawPortNames();
static void printRightText(const char* outText);
void closePort();

#pragma region gettingPorts

static void removeComa(char* s_in) {  // Remove coma at the end of the string
    uint8_t pos = 0;
    while (s_in[pos++] != ',') { }
    s_in[pos - 1] = '\0';  // Null character at where was the coma
}

static void initPuertos() {
    for (uint8_t i = 0; i < MAXPUERTOS; i++)
        strcpy(puertos[i], "COM");
}

void getPorts()
{
    initPuertos();  // Reset them to their original state

    initAmadeusCom();   // Init the amad class
    
    uint8_t disponibles[MAXPUERTOS] = { 0 };

    nPuertos = amadeus_showPorts(disponibles);

    for (int i = 0; i < nPuertos; i++) {
        char temp[5];
        itoa(disponibles[i], temp, 10);
        strcat(puertos, temp);
    }
}

#pragma endregion gettingPorts

#pragma region connectingWritingUpdating

void writeRead(const char* dataOut, int outL, char* dataIn, int inL) {
    if (!editor.connected)
        return;

    amadeus_writeRead(dataOut, outL, dataIn, inL);
}

void write(const char* buffer, int length)
{
    if (!editor.connected)
        return;

    amadeus_write(buffer, length);
}

void connectPort(const char* port)
{
    editor.connected = false;

    if (!amadeus_connect(port))    // If 0 => there has been an error
        printRightText("Error!");
    else {
        editor.connected = true;
        printRightText("Connected");
    }
}

void closeSerialPort() {
    if (!editor.connected)
        return;
    
    editor.connected = false;
    amadeus_disconnect();
}

void portPressed(int position) {
    if (position > nPuertos - 1)
        return;

    connectPort(puertos[position]);
}


void updatePorts(void) {
    if (!ui.portShown)
        return;

    printRightText("Updating...");
    getPorts();
    
    drawPortNames();
    
    char temp[16] = { 0 };
    sprintf(temp, "Found %d port%s", nPuertos, nPuertos == 1 ? "." : "s.");
    printRightText(temp);
}

#pragma endregion connectingWritingUpdating

#pragma region GUI

void hidePorts(void)
{
    ui.portShown = false;
    showTopScreen(true);
}

static void drawPortNames() {
    clearRect(4, 4, 164, 164);      // Here we draw the texts (left)

    for (uint16_t i = 0; i < nPuertos; i++)
    {
        const uint16_t y = 6 + (i * (FONT1_CHAR_H + 1));    // Compute the height

        textOut(6, y, PAL_BLCKTXT, puertos[i]);             // Draw the texts
    }
}

static void printRightText(const char* outText) {
    clearRect(176, 4, 113, 164);    // Nothing for now (right)
    
    textOut(178, 6, PAL_BLCKTXT, outText);
}

static void drawPortsScreen() {
    // Draw the background
    drawFramework(0, 0, 172, 172, FRAMEWORK_TYPE1);     // Left
    drawFramework(172, 0, 121, 172, FRAMEWORK_TYPE1);   // Right
    
    printRightText(""); // Send empty string to just clear the right side

    drawPortNames();
}

void showPorts(void) {
    hideTopScreen();
    showTopRightMainScreen();
    drawPortsScreen();

    if (editor.connected)
        printRightText("Conencted");
    else {
        char temp[16] = { 0 };
        sprintf(temp, "Found %d port%s", nPuertos, nPuertos == 1 ? "." : "s.");
        printRightText(temp);
    }

    drawPortNames();

    ui.portShown = true;
}

void togglePorts(void) {
    if (ui.diskOpShown)
        return;

    if (ui.portShown) // Using code from diskop.c hehehe
        hidePorts();
    else
        showPorts();
}

#pragma endregion GUI