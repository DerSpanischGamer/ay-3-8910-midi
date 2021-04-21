#include <cstring>
#include <Windows.h>
#include <iostream>

class amadeusCom {
private:
	DCB dcb;
	DWORD byteswritten;
	HANDLE hPort;
public:
	int connect(const char* port) {
		hPort = CreateFile(port, GENERIC_WRITE | GENERIC_READ, 0, NULL, OPEN_EXISTING, 0, NULL);
		if (!GetCommState(hPort, &dcb))
			goto error;
		dcb.BaudRate = CBR_115200; //9600 Baud
		dcb.ByteSize = 8; //8 data bits
		dcb.Parity = NOPARITY; //no parity
		dcb.StopBits = ONESTOPBIT; //1 stop
		if (!SetCommState(hPort, &dcb))
			goto error;
		return 1;

		error:
			CloseHandle(hPort); //close the handle
			return 0;
	}

	void write(const char* data, int length) { WriteFile(hPort, data, length, &byteswritten, NULL); }

	void writeRead(const char* dataOut, int outL, char* dataIn, int inL) {
		write(dataOut, outL);
		Sleep(15);
		ReadFile(hPort, dataIn, inL, &byteswritten, NULL);
	}

	void disconnect() { CloseHandle(hPort); }
};

amadeusCom* amad;

extern "C" int initAmadeusCom() {
	amad = new amadeusCom();
	return 1;
}

extern "C" int amadeus_connect(const char* port) {
	return amad->connect(port);
}

extern "C" void amadeus_write(const char* data, int length) {
	amad->write(data, length);
}

extern "C" void amadeus_writeRead(const char* dataOut, int outL, char* dataIn, int inL) {
	amad->writeRead(dataOut, outL, dataIn, inL);
}

extern "C" void amadeus_disconnect() {
	amad->disconnect();
}