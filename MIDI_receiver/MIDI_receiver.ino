#include <MIDI.h>  // Add Midi Library
#include <Amadeus.h>

// Variables

const char combis[][2] = {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {15, 209}, {14, 238}, {14, 24}, {13, 77}, {12, 142}, {11, 218}, {11, 47}, {10, 143}, {9, 247}, {9, 104}, {8, 224}, {8, 97}, {7, 232}, {7, 119}, {7, 12}, {6, 166}, {6, 71}, {5, 237}, {5, 151}, {5, 71}, {4, 251}, {4, 180}, {4, 112}, {4, 48}, {3, 244}, {3, 187}, {3, 134}, {3, 83}, {3, 35}, {2, 246}, {2, 203}, {2, 163}, {2, 125}, {2, 90}, {2, 56}, {2, 24}, {1, 250}, {1, 221}, {1, 195}, {1, 169}, {1, 145}, {1, 123}, {1, 101}, {1, 81}, {1, 62}, {1, 45}, {1, 28}, {1, 12}, {0, 253}, {0, 238}, {0, 225}, {0, 212}, {0, 200}, {0, 189}, {0, 178}, {0, 168}, {0, 159}, {0, 150}, {0, 142}, {0, 134}, {0, 126}, {0, 119}, {0, 112}, {0, 106}, {0, 100}, {0, 94}, {0, 89}, {0, 84}, {0, 79}, {0, 75}, {0, 71}, {0, 67}, {0, 63}, {0, 59}, {0, 56}, {0, 53}, {0, 50}, {0, 47}, {0, 44}, {0, 42}, {0, 39}, {0, 37}, {0, 35}, {0, 33}, {0, 31}, {0, 29}, {0, 28}, {0, 26}, {0, 25}, {0, 23}, {0, 22}, {0, 21}, {0, 19}, {0, 18}, {0, 17}, {0, 16}, {0, 15}, {0, 14}, {0, 14}, {0, 13}, {0, 12}, {0, 11}, {0, 11}, {0, 10}, {0, 9}, {0, 9}, {0, 8}, {0, 8}, {0, 7}, {0, 7}, {0, 7}, {0, 6}, {0, 6}, {0, 5}, {0, 5}, {0, 5}, {0, 4}, {0, 4}, {0, 4}, {0, 4}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 1}, {0, 1}, {0, 1}, {0, 1}, {0, 1}, {0, 1}, {0, 1}};

char canales[6] = { -1, -1, -1, -1, -1, -1};

Amadeus amadeus = Amadeus();

// Funciones

char findLibre() {
  for (char i = 0; i < 6; i++)
    if (canales[i] == -1)
      return i;
  return -1;
}

char findNota(char nota) {
  for (char i = 0; i < 6; i++)
    if (canales[i] == nota)
      return i;
  return -1;
}

void MyHandleNoteOn(byte channel, byte pitch, byte velocity) {
  if (combis[pitch][0] == -1) { return; }
  
  if (velocity == 0) {//A NOTE ON message with a velocity = Zero is actualy a NOTE OFF
    const char can = findNota(pitch);

    if (can == -1) { return; }

    canales[can] = -1;

    const char chip = can / 3;
    const char canal = can % 3;

    amadeus.out(chip, canal + 8, 0);

    return;
  }

  const char can = findLibre();

  const char chip = can / 3;
  const char canal = can % 3;

  canales[can] = pitch;

  amadeus.out(chip, 2 * canal, combis[pitch][1]);
  delayMicroseconds(10);
  amadeus.out(chip, (2 * canal) + 1, combis[pitch][0]);
  delayMicroseconds(10);
  amadeus.out(chip, canal + 8, 15);
}

void setup() {
  MIDI.begin(MIDI_CHANNEL_OMNI); // Initialize the Midi Library.
// OMNI sets it to listen to all channels.. MIDI.begin(2) would set it
// to respond to channel 2 notes only.
  MIDI.setHandleNoteOn(MyHandleNoteOn); // This is important!! This command
  // tells the Midi Library which function I want called when a Note ON command
  // is received. in this case it's "MyHandleNoteOn".

  amadeus.begin();
}

void loop() { // Main loop
  MIDI.read(); // Continually check what Midi Commands have been received.
}
