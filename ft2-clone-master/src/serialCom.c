#include <stdlib.h>
#include <stdio.h>
#include <stdint.h> // I like small ints sorry girls

#include "ft2_structs.h"
#include "ft2_gui.h"

#define FILENAME_TEXT_X 170

#define MAXPUERTOS 10

FILE* portsFile;

char numPuertos[5];     // Stores the first line which has the number of ports
uint8_t nPuertos;       // Idem but as an int
char puertos[MAXPUERTOS][255];  // 10 string, each with 255 characters to write the ports that CAN be accessed

void* amadeusC = NULL;

int connected = 0;  // This will hold if we are connected to a board or not

// ---- FUNCTION DECLARATION
static void drawPortNames();
static void printRightText(const char* outText);

#pragma region gettingPorts

static void removeComa(char* s_in) {  // Remove coma at the end of the string
    uint8_t pos = 0;
    while (s_in[pos++] != ',') { }
    s_in[pos - 1] = '\0';  // Null character at where was the coma
}

static void initPuertos() {
    for (uint8_t i = 0; i < MAXPUERTOS; i++)
        strcpy(puertos[i], "\\\\.\\");
}

void getPorts()
{
    initPuertos();  // Reset them to their original state

    system("python ../../src/ports.py");

    Sleep(500); // Wait for python :>
    
    if ((portsFile = fopen("portList.txt", "r")) == NULL) {
        printf("Error loading the ports !");
        return;     // Nothing to do with ports if error upon reading
    }

    fgets(numPuertos, 5, portsFile);  // Get the first line that stores the number of ports availables

    nPuertos = atoi(numPuertos);

    uint8_t i = 0;
    const char temp[255];
    while (fgets(temp, 255, portsFile) != NULL) { strcat(puertos[i], temp); removeComa(puertos[i++]); } // Keep the lines coming

    fclose(portsFile);
    return;
}

#pragma endregion gettingPorts

#pragma region connectingWritingUpdating

void writeRead() {  // TODO: return a pointer to the string returned
}

void write(const char* buffer, int length)
{
    if (!editor.connected)
        return;

    amadeus_write(amadeusC, buffer, length);
}

void connectPort(const char* port)
{
    if (editor.connected)
        closePort();

    editor.connected = false;

    amadeusC = initAmadeusCom();
    if (!amadeus_connect(amadeusC, port))    // If 0 => there has been an error
        printRightText("Error!");
    else {
        editor.connected = true;
        printRightText("Connected\n");
    }
}

void closePort() {
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

        textOut(4, y, PAL_BLCKTXT, puertos[i]);     // Draw the texts
    }
}

static void printRightText(const char* outText) {
    clearRect(176, 4, 113, 164);    // Nothing for now (right)
    
    textOut(176, 4, PAL_BLCKTXT, outText);
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