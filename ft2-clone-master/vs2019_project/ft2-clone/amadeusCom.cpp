#include <string>
#include <Windows.h>
#include <iostream>

class amadeusCom {
private:
	DCB dcb;
	DWORD byteswritten;
	HANDLE hPort;
public:
	int searchPorts(uint8_t* puertos) //added function to find the present serial 
	{
		char lpTargetPath[5000]; // buffer to store the path of the COMPORTS
		bool gotPort = false; // in case the port is not found

		int pos = 0;

		for (int i = 0; i <= 10; i++) // checking ports from COM0 to COM255
		{
			std::string str = "COM" + std::to_string(i); // converting to COM0, COM1, COM2
			DWORD test = QueryDosDevice(str.c_str(), lpTargetPath, 5000);

			// Test the return value and error if any
			if (test != 0)		 //QueryDosDevice returns zero if it didn't find an object
				puertos[pos++] = i;
		}

		return pos;
	}

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

amadeusCom* amad = NULL;	// Stores the instance of 

extern "C" void initAmadeusCom() {
	if (amad == NULL)
		amad = new amadeusCom();
}

extern "C" int amadeus_showPorts(uint8_t* puertos) {
	return amad->searchPorts(puertos);
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