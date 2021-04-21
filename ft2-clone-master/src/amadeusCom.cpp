#include <cstring>
#include <Windows.h>
#include <iostream>

class amadeusCom {
private:
	DCB dcb;
	DWORD byteswritten;
	HANDLE hPort;
public:
	int amadeusCom::connect(char* port) {
		hPort = CreateFile(port, GENERIC_WRITE | GENERIC_READ, 0, NULL, OPEN_EXISTING, 0, NULL);	// Stablish connection
		if (!GetCommState(hPort, &dcb))
			return 0;

		dcb.BaudRate = CBR_115200; // 115200 Baud
		dcb.ByteSize = 8; // 8 data bits
		dcb.Parity = NOPARITY; // no parity
		dcb.StopBits = ONESTOPBIT; // 1 stop
		if (!SetCommState(hPort, &dcb))
			return 0;
		
		return 1;
	}

	void amadeusCom::write(const char* data, int length)	{ WriteFile(hPort, data, length, &byteswritten, NULL); } // Send command to board

	void amadeusCom::writeRead(const char* outGoing, int outL, char* Incoming, int inL) {
		write(outGoing, outL);
		Sleep(15);
		ReadFile(hPort, Incoming, inL, &byteswritten, 0);
	}

	void amadeusCom::disconnect() { CloseHandle(hPort); }	// Close the port connection
};

extern "C" void* initAmadeusCom() {
	return new amadeusCom;
}

extern "C" int amadeus_connect(void* amadeus, char* port) {
	return static_cast<amadeusCom*>(amadeus)->connect(port);
}

extern "C" void amadeus_write(void* amadeus, const char* data, int length) {
	static_cast<amadeusCom*>(amadeus)->write(data, length);
}

extern "C" void amadeus_writeRead(void* amadeus, const char* outG, int outL, char* inC, int inL) {
	static_cast<amadeusCom*>(amadeus)->writeRead(outG, outL, inC, inL);
}

extern "C" void amadeus_disconnect(void* amadeus) {
	static_cast<amadeusCom*>(amadeus)->disconnect();
}