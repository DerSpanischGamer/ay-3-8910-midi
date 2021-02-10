// Codigo original https://github.com/986-Studio/AY-3-3910-Player/blob/master/AY3910RegWrite/AY3910RegWrite.ino


class Amadeus {
	public:
		Amadeus();			// Crear una instancia de este clase
		void begin();		// Iniciar todo
		void out(char chip, uint8_t reg, uint8_t value);	// Enviar una nota a uno de los dos chips
	private:
		void setup_clock();
		void setup_data(int mode);
		void setup_control(char modo);
		void set_control(char chip, char mode);
		void SetData(unsigned char data);
		unsigned char GetData(void);
		uint8_t read_2149_reg(uint8_t reg);
		
		static const int ocr2aval;
		static const int freqOutputPin;
};