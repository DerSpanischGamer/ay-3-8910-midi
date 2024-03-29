// for finding memory leaks in debug mode with Visual Studio
#if defined _DEBUG && defined _MSC_VER
#include <crtdbg.h>
#endif

#include <stdio.h>
#include <stdint.h>
#include "ft2_header.h"
#include "ft2_config.h"
#include "ft2_keyboard.h"
#include "ft2_audio.h"
#include "ft2_midi.h"
#include "ft2_pattern_ed.h"
#include "ft2_sysreqs.h"
#include "ft2_textboxes.h"
#include "ft2_tables.h"
#include "ft2_structs.h"

enum
{
	KEYTYPE_NUM = 0,
	KEYTYPE_ALPHA = 1
};

static double dVolScaleFK1 = 1.0, dVolScaleFK2 = 1.0;

// for block cut/copy/paste
static bool blockCopied;
static int16_t markXSize, markYSize;
static uint16_t ptnBufLen, trkBufLen;

// for transposing - these are set and tested accordingly
static int8_t lastTranspVal;
static uint8_t lastInsMode, lastTranspMode;
static uint32_t transpDelNotes; // count of under-/overflowing notes for warning message
static tonTyp clearNote;

static tonTyp blkCopyBuff[MAX_PATT_LEN * MAX_VOICES];
static tonTyp ptnCopyBuff[MAX_PATT_LEN * MAX_VOICES];
static tonTyp trackCopyBuff[MAX_PATT_LEN];

static const int8_t tickArr[16] = { 16, 8, 0, 4, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1 };

static const uint8_t maxValuesRegs[14] = { 0xFF, 0x0F, 0xFF, 0x0F, 0xFF, 0x0F, 0x1F, 0x3F, 0x1F, 0x1F, 0x1F, 0xFF, 0xFF, 0x0F };

void recordNote(uint8_t note, int8_t vol, bool changeNote);

// when the cursor is at the note slot
static bool testNoteKeys(SDL_Scancode scancode)
{
	const int8_t noteNum = scancodeKeyToNote(scancode);

#pragma region noteNum==97
	if (noteNum == 97)
	{
		// inserts "note off" if editing song
		if (playMode == PLAYMODE_EDIT || playMode == PLAYMODE_RECPATT || playMode == PLAYMODE_RECSONG)
		{
			if (!allocatePattern(editor.editPattern))
				return true; // key pressed

			patt[editor.editPattern][(editor.pattPos * MAX_VOICES) + cursor.ch].ton = 97;

			const uint16_t pattLen = pattLens[editor.editPattern];
			if (playMode == PLAYMODE_EDIT && pattLen >= 1)
				setPos(-1, (editor.pattPos + editor.ID_Add) % pattLen, true);

			ui.updatePatternEditor = true;
			setSongModifiedFlag();
		}

		return true; // key pressed
	}

#pragma endregion noteNum==97

	if (noteNum >= 0 && noteNum <= 9)
	{
		recordNote(noteNum, -1, true);
		return true; // note key pressed (and note triggered)
	}

	return false; // no note key pressed
}

// when the cursor is at the note slot
void testNoteKeysRelease(SDL_Scancode scancode)
{
	const int8_t noteNum = scancodeKeyToNote(scancode); // convert key scancode to note number
	if (noteNum > 0 && noteNum <= 96)
		recordNote(noteNum, 0, true); // release note
}

static bool testEditKeys(SDL_Scancode scancode, SDL_Keycode keycode)
{
	int8_t i;
	uint8_t oldVal;

	if (cursor.object == CURSOR_NOTE)
	{
		// the edit cursor is at the note slot

		if (testNoteKeys(scancode))	// Keep going here to record note
		{
			keyb.keyRepeat = (playMode == PLAYMODE_EDIT); // repeat keys only if in edit mode
			return true; // we jammed an instrument
		}

		return false; // no note key pressed, test other keys
	}

	// ===== USELES CODE DOWN BELOW ======

#pragma region BOOOOORING
	if (playMode != PLAYMODE_EDIT && playMode != PLAYMODE_RECSONG && playMode != PLAYMODE_RECPATT)
		return false; // we're not editing, test other keys

	// convert key to slot data

	if (cursor.object == CURSOR_VOL1)
	{
		// volume column effect type (mixed keys)

		for (i = 0; i < KEY2VOL_ENTRIES; i++)
		{
			if (keycode == key2VolTab[i])
				break;
		}

		if (i == KEY2VOL_ENTRIES)
			i = -1; // invalid key for slot
	}
	else if (cursor.object == CURSOR_EFX0)
	{
		// effect type (mixed keys)

		for (i = 0; i < KEY2EFX_ENTRIES; i++)
		{
			if (keycode == key2EfxTab[i])
				break;
		}

		if (i == KEY2EFX_ENTRIES)
			i = -1; // invalid key for slot
	}
	else
	{
		// all other slots (hex keys)

		for (i = 0; i < KEY2HEX_ENTRIES; i++)
		{
			if (keycode == key2HexTab[i])
				break;
		}

		if (i == KEY2HEX_ENTRIES)
			i = -1; // invalid key for slot
	}

	if (i == -1 || !allocatePattern(editor.editPattern))
		return false; // no edit to be done

	// insert slot data

	tonTyp *ton = &patt[editor.editPattern][(editor.pattPos * MAX_VOICES) + cursor.ch];
	switch (cursor.object)
	{
		case CURSOR_INST1:
		{
			oldVal = ton->instr;

			ton->instr = (ton->instr & 0x0F) | (i << 4);
			if (ton->instr > MAX_INST)
				ton->instr = MAX_INST;

			if (ton->instr != oldVal)
				setSongModifiedFlag();
		}
		break;

		case CURSOR_INST2:
		{
			oldVal = ton->instr;
			ton->instr = (ton->instr & 0xF0) | i;

			if (ton->instr != oldVal)
				setSongModifiedFlag();
		}
		break;

		case CURSOR_VOL1:
		{
			oldVal = ton->vol;

			ton->vol = (ton->vol & 0x0F) | ((i + 1) << 4);
			if (ton->vol >= 0x51 && ton->vol <= 0x5F)
				ton->vol = 0x50;

			if (ton->vol != oldVal)
				setSongModifiedFlag();
		}
		break;

		case CURSOR_VOL2:
		{
			oldVal = ton->vol;

			if (ton->vol < 0x10)
				ton->vol = 0x10 + i;
			else
				ton->vol = (ton->vol & 0xF0) | i;

			if (ton->vol >= 0x51 && ton->vol <= 0x5F)
				ton->vol = 0x50;

			if (ton->vol != oldVal)
				setSongModifiedFlag();
		}
		break;

		case CURSOR_EFX0:
		{
			oldVal = ton->effTyp;

			ton->effTyp = i;

			if (ton->effTyp != oldVal)
				setSongModifiedFlag();
		}
		break;

		case CURSOR_EFX1:
		{
			oldVal = ton->eff;

			ton->eff = (ton->eff & 0x0F) | (i << 4);

			if (ton->eff != oldVal)
				setSongModifiedFlag();
		}
		break;

		case CURSOR_EFX2:
		{
			oldVal = ton->eff;

			ton->eff = (ton->eff & 0xF0) | i;

			if (ton->eff != oldVal)
				setSongModifiedFlag();
		}
		break;

		default: break;
	}

	// increase row (only in edit mode)

	const int16_t pattLen = pattLens[editor.editPattern];
	if (playMode == PLAYMODE_EDIT && pattLen >= 1)
		setPos(-1, (editor.pattPos + editor.ID_Add) % pattLen, true);

	if (i == 0) // if we inserted a zero, check if pattern is empty, for killing
		killPatternIfUnused(editor.editPattern);

	ui.updatePatternEditor = true;
	return true;

#pragma endregion BOOOOORING
}

// directly ported from the original FT2 code (fun fact: named EvulateTimeStamp() in the FT2 code)
static void evaluateTimeStamp(int16_t *songPos, int16_t *pattNr, int16_t *pattPos, int16_t *tick)
{
	int16_t sp = editor.songPos;
	int16_t nr = editor.editPattern;
	int16_t row = editor.pattPos;
	int16_t t = editor.tempo - editor.timer;

	t = CLAMP(t, 0, editor.tempo - 1);

	// this is needed, but also breaks quantization on speed>15
	if (t > 15)
		t = 15;

	const int16_t pattLen = pattLens[nr];

	if (config.recQuant > 0)
	{
		int16_t r;
		if (config.recQuantRes >= 16)
		{
			t += (editor.tempo >> 1) + 1;
		}
		else
		{
			r = tickArr[config.recQuantRes-1];

			int16_t p = row & (r - 1);
			if (p < (r >> 1))
				row -= p;
			else
				row = (row + r) - p;

			t = 0;
		}
	}

	if (t > editor.tempo)
	{
		t -= editor.tempo;
		row++;
	}

	if (row >= pattLen)
	{
		if (playMode == PLAYMODE_RECSONG)
			sp++;

		row = 0;
		if (sp >= song.len)
			sp = song.repS;

		nr = song.songTab[sp];
	}

	*songPos = sp;
	*pattNr = nr;
	*pattPos = row;
	*tick = t;
}

uint8_t power(uint8_t base, uint8_t power) {
	uint8_t outNum = 1;
	while (power--) {
		outNum *= base;
	}

	return outNum;
}

// directly ported from the original FT2 code - what a mess, but it works...
void recordNote(uint8_t note, int8_t vol, bool changeNote)
{
	if (!changeNote) {	// If false it means that we don't want to change a note
		editor.nextMove = 0;	// Just reset nextMove
		return;
	}

	// vol = -1 from before
	int8_t i;
	int16_t nr, sp, pattpos, tick;
	int32_t time;
	tonTyp *noteData;

	const int16_t oldpattpos = editor.pattPos;

	if (songPlaying)
	{
		// row quantization
		evaluateTimeStamp(&sp, &nr, &pattpos, &tick);
	}
	else
	{
		sp = editor.songPos;
		nr = editor.editPattern;
		pattpos = editor.pattPos;
		tick = 0;
	}

	bool editmode = (playMode == PLAYMODE_EDIT);
	bool recmode = (playMode == PLAYMODE_RECSONG) || (playMode == PLAYMODE_RECPATT);

	if (note == 97)	// Silenciar?
		vol = 0;

	int8_t c = cursor.ch;
	int8_t k = -1;

	for (i = 0; i < song.antChn; i++)
	{
		if (note == editor.keyOnTab[i])
			k = i;
	}

	if (vol != 0)
	{
		if (c < 0)	// If cursor not set (def. val = -1)
			return;

		// play note

		editor.keyOnTab[c] = note;

		if (!songPlaying)	// If we arent playing the song
		{
			if (allocatePattern(nr))
			{
				const int16_t pattLen  = pattLens[nr];
				noteData = &patt[nr][(pattpos * MAX_VOICES) + c];

				if (!editor.nextMove)	// If we are about to enter the first number
					noteData->ton = 0;	// Clear the cell

				uint8_t pow = power(10, 2 - editor.nextMove);
				uint16_t preNote = note * pow;	// Need more space than 8 bits in case he introduces a number bigger than 255
				
				if (noteData->ton + preNote >= maxValuesRegs[c % 14] + 1)	// If we are going to overflow in the current register
					noteData->ton = (maxValuesRegs[c % 14] / (pow)) * pow;
				else // If we ok
					noteData->ton += (uint8_t) preNote;	// Insert data the good ol way

				noteData->instr = 1;	// Always 1 in Amadeus card as this will be a check to see if the value 0 has been entered by the user or it means that there's no change
				
				editor.nextMove++;		// We've added a number !

				// increase row (only in edit mode)
				if (editor.nextMove == 3)	// move row to the right
				{
					cursorRight();	// Move to the right
					editor.nextMove = 0;	// Reset counter
				}
				ui.updatePatternEditor = true;
				setSongModifiedFlag();
			}
		}
	}
	else
#pragma region idk
	{
		// note off

		if (k != -1)
			c = k;

		if (c < 0)
			return;

		editor.keyOnTab[c]   = 0;
		editor.keyOffTime[c] = ++editor.keyOffNr;
		
		if (config.recRelease && recmode)
		{
			if (allocatePattern(nr))
			{
				// insert data

				int16_t pattLen = pattLens[nr];
				noteData = &patt[nr][(pattpos * MAX_VOICES) + c];

				if (noteData->ton != 0)
					pattpos++;

				if (pattpos >= pattLen)
				{
					if (songPlaying)
						sp++;

					if (sp >= song.len)
						sp  = song.repS;

					nr = song.songTab[sp];
					pattpos = 0;
					pattLen = pattLens[nr];
				}

				noteData = &patt[nr][(pattpos * MAX_VOICES) + c];
				noteData->ton = 97;

				if (!recmode)
				{
					// increase row (only in edit mode)
					if (pattLen >= 1)
						setPos(-1, (editor.pattPos + editor.ID_Add) % pattLen, true);
				}
				else
				{
					// apply tick delay for note if quantization is disabled
					if (!config.recQuant && tick > 0)
					{
						noteData->effTyp = 0x0E;
						noteData->eff = 0xD0 + (tick & 0x0F);
					}
				}

				ui.updatePatternEditor = true;
				setSongModifiedFlag();
			}
		}
	}

#pragma endregion idk
}

// Called when pressing a note
bool handleEditKeys(SDL_Keycode keycode, SDL_Scancode scancode)
{
#pragma region DEL
	if (keycode == SDLK_DELETE)// special case for delete - manipulate note data
	{
		tonTyp *note = &patt[editor.editPattern][(editor.pattPos * MAX_VOICES) + cursor.ch];

		if (keyb.leftShiftPressed)
		{
			// delete all
			memset(note, 0, sizeof (tonTyp));
		}
		else if (keyb.leftCtrlPressed)
		{
			// delete volume column + effect
			note->vol = 0;
			note->effTyp = 0;
			note->eff = 0;
		}
		else if (keyb.leftAltPressed)
		{
			// delete effect
			note->effTyp = 0;
			note->eff = 0;
		}
		else
		{
			if (cursor.object == CURSOR_VOL1 || cursor.object == CURSOR_VOL2)
			{
				// delete volume column
				note->vol = 0;
			}
			else
			{
				// delete note + instrument
				note->ton = 0;
				note->instr = 0;
			}
		}

		killPatternIfUnused(editor.editPattern);

		// increase row (only in edit mode)
		const int16_t pattLen = pattLens[editor.editPattern];
		if (playMode == PLAYMODE_EDIT && pattLen >= 1)
			setPos(-1, (editor.pattPos + editor.ID_Add) % pattLen, true);

		ui.updatePatternEditor = true;
		setSongModifiedFlag();

		return true;
	}

#pragma endregion DEL

	// a kludge for french keyb. layouts to allow writing numbers in the pattern data with left SHIFT
	const bool frKeybHack = keyb.leftShiftPressed && !keyb.leftAltPressed && !keyb.leftCtrlPressed &&
	               (scancode >= SDL_SCANCODE_1) && (scancode <= SDL_SCANCODE_0);

	if (frKeybHack || !keyb.keyModifierDown)
		return (testEditKeys(scancode, keycode)); // The real deal

	return false;
}

void writeToMacroSlot(uint8_t slot)
{
	uint16_t writeVol = 0;
	uint16_t writeEff = 0;

	if (patt[editor.editPattern] != NULL)
	{
		tonTyp *note = &patt[editor.editPattern][(editor.pattPos * MAX_VOICES) + cursor.ch];
		writeVol = note->vol;
		writeEff = (note->effTyp << 8) | note->eff;
	}

	if (cursor.object == CURSOR_VOL1 || cursor.object == CURSOR_VOL2)
		config.volMacro[slot] = writeVol;
	else
		config.comMacro[slot] = writeEff;
}

void writeFromMacroSlot(uint8_t slot)
{
	if (playMode != PLAYMODE_EDIT && playMode != PLAYMODE_RECSONG && playMode != PLAYMODE_RECPATT)
		return;

	if (!allocatePattern(editor.editPattern))
		return;

	const int16_t pattLen = pattLens[editor.editPattern];
	tonTyp *note = &patt[editor.editPattern][(editor.pattPos * MAX_VOICES) + cursor.ch];

	if (cursor.object == CURSOR_VOL1 || cursor.object == CURSOR_VOL2)
	{
		note->vol = (uint8_t)config.volMacro[slot];
	}
	else
	{
		uint8_t effTyp = (uint8_t)(config.comMacro[slot] >> 8);
		if (effTyp > 35)
		{
			// illegal effect
			note->effTyp = 0;
			note->eff = 0;
		}
		else
		{
			note->effTyp = effTyp;
			note->eff = config.comMacro[slot] & 0xFF;
		}
	}

	if (playMode == PLAYMODE_EDIT && pattLen >= 1)
		setPos(-1, (editor.pattPos + editor.ID_Add) % pattLen, true);

	killPatternIfUnused(editor.editPattern);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void insertPatternNote(void)
{
	if (playMode != PLAYMODE_EDIT && playMode != PLAYMODE_RECPATT && playMode != PLAYMODE_RECSONG)
		return;

	const int16_t nr = editor.editPattern;

	tonTyp *pattPtr = patt[nr];
	if (pattPtr == NULL)
		return;

	const int16_t pattPos = editor.pattPos;
	const int16_t pattLen = pattLens[nr];

	if (pattLen > 1)
	{
		for (int32_t i = pattLen-2; i >= pattPos; i--)
			pattPtr[((i + 1) * MAX_VOICES) + cursor.ch] = pattPtr[(i * MAX_VOICES) + cursor.ch];
	}

	memset(&pattPtr[(pattPos * MAX_VOICES) + cursor.ch], 0, sizeof (tonTyp));

	killPatternIfUnused(nr);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void insertPatternLine(void)
{
	if (playMode != PLAYMODE_EDIT && playMode != PLAYMODE_RECPATT && playMode != PLAYMODE_RECSONG)
		return;

	const int16_t nr = editor.editPattern;

	setPatternLen(nr, pattLens[nr] + config.recTrueInsert); // config.recTrueInsert is 0 or 1

	tonTyp *pattPtr = patt[nr];
	if (pattPtr != NULL)
	{
		const int16_t pattPos = editor.pattPos;
		const int16_t pattLen = pattLens[nr];

		if (pattLen > 1)
		{
			for (int32_t i = pattLen-2; i >= pattPos; i--)
			{
				for (int32_t j = 0; j < MAX_VOICES; j++)
					pattPtr[((i + 1) * MAX_VOICES) + j] = pattPtr[(i * MAX_VOICES) + j];
			}
		}

		memset(&pattPtr[pattPos * MAX_VOICES], 0, TRACK_WIDTH);

		killPatternIfUnused(nr);
	}

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void deletePatternNote(void)
{
	if (playMode != PLAYMODE_EDIT && playMode != PLAYMODE_RECPATT && playMode != PLAYMODE_RECSONG)
		return;

	const int16_t nr = editor.editPattern;
	int16_t pattPos = editor.pattPos;
	const int16_t pattLen = pattLens[nr];

	tonTyp *pattPtr = patt[editor.editPattern];
	if (pattPtr != NULL)
	{
		if (pattPos > 0)
		{
			pattPos--;
			editor.pattPos = song.pattPos = pattPos;

			for (int32_t i = pattPos; i < pattLen-1; i++)
				pattPtr[(i * MAX_VOICES) + cursor.ch] = pattPtr[((i + 1) * MAX_VOICES) + cursor.ch];

			memset(&pattPtr[((pattLen - 1) * MAX_VOICES) + cursor.ch], 0, sizeof (tonTyp));
		}
	}
	else
	{
		if (pattPos > 0)
		{
			pattPos--;
			editor.pattPos = song.pattPos = pattPos;
		}
	}

	killPatternIfUnused(nr);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void deletePatternLine(void)
{
	if (playMode != PLAYMODE_EDIT && playMode != PLAYMODE_RECPATT && playMode != PLAYMODE_RECSONG)
		return;

	const int16_t nr = editor.editPattern;
	int16_t pattPos = editor.pattPos;
	const int16_t pattLen = pattLens[nr];

	tonTyp *pattPtr = patt[editor.editPattern];
	if (pattPtr != NULL)
	{
		if (pattPos > 0)
		{
			pattPos--;
			editor.pattPos = song.pattPos = pattPos;

			for (int32_t i = pattPos; i < pattLen-1; i++)
			{
				for (int32_t j = 0; j < MAX_VOICES; j++)
					pattPtr[(i * MAX_VOICES) + j] = pattPtr[((i + 1) * MAX_VOICES) + j];
			}

			memset(&pattPtr[(pattLen - 1) * MAX_VOICES], 0, TRACK_WIDTH);
		}
	}
	else
	{
		if (pattPos > 0)
		{
			pattPos--;
			editor.pattPos = song.pattPos = pattPos;
		}
	}

	if (config.recTrueInsert && pattLen > 1)
		setPatternLen(nr, pattLen - 1);

	killPatternIfUnused(nr);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

// ----- TRANSPOSE FUNCTIONS -----

static void countOverflowingNotes(uint8_t currInsOnly, uint8_t transpMode, int8_t addVal)
{
	uint8_t ton;
	uint16_t p, pattLen, ch, row;
	tonTyp *pattPtr;

	transpDelNotes = 0;
	switch (transpMode)
	{
		case TRANSP_TRACK:
		{
			pattPtr = patt[editor.editPattern];
			if (pattPtr == NULL)
				return; // empty pattern

			pattPtr += cursor.ch;

			pattLen = pattLens[editor.editPattern];
			for (row = 0; row < pattLen; row++)
			{
				ton = pattPtr->ton;
				if ((ton >= 1 && ton <= 96) && (!currInsOnly || pattPtr->instr == editor.curInstr))
				{
					if ((int8_t)ton+addVal > 96 || (int8_t)ton+addVal <= 0)
						transpDelNotes++;
				}

				pattPtr += MAX_VOICES;
			}
		}
		break;

		case TRANSP_PATT:
		{
			pattPtr = patt[editor.editPattern];
			if (pattPtr == NULL)
				return; // empty pattern

			pattLen = pattLens[editor.editPattern];
			for (row = 0; row < pattLen; row++)
			{
				for (ch = 0; ch < song.antChn; ch++)
				{
					ton = pattPtr->ton;
					if ((ton >= 1 && ton <= 96) && (!currInsOnly || pattPtr->instr == editor.curInstr))
					{
						if ((int8_t)ton+addVal > 96 || (int8_t)ton+addVal <= 0)
							transpDelNotes++;
					}

					pattPtr++;
				}

				pattPtr += MAX_VOICES - song.antChn;
			}
		}
		break;

		case TRANSP_SONG:
		{
			for (p = 0; p < MAX_PATTERNS; p++)
			{
				pattPtr = patt[p];
				if (pattPtr == NULL)
					continue; // empty pattern

				pattLen = pattLens[p];
				for (row = 0; row < pattLen; row++)
				{
					for (ch = 0; ch < song.antChn; ch++)
					{
						ton = pattPtr->ton;
						if ((ton >= 1 && ton <= 96) && (!currInsOnly || pattPtr->instr == editor.curInstr))
						{
							if ((int8_t)ton+addVal > 96 || (int8_t)ton+addVal <= 0)
								transpDelNotes++;
						}

						pattPtr++;
					}

					pattPtr += MAX_VOICES - song.antChn;
				}
			}
		}
		break;

		case TRANSP_BLOCK:
		{
			if (pattMark.markY1 == pattMark.markY2)
				return; // no pattern marking

			pattPtr = patt[editor.editPattern];
			if (pattPtr == NULL)
				return; // empty pattern

			pattPtr += (pattMark.markY1 * MAX_VOICES) + pattMark.markX1;

			pattLen = pattLens[editor.editPattern];
			for (row = pattMark.markY1; row < pattMark.markY2; row++)
			{
				for (ch = pattMark.markX1; ch <= pattMark.markX2; ch++)
				{
					ton = pattPtr->ton;
					if ((ton >= 1 && ton <= 96) && (!currInsOnly || pattPtr->instr == editor.curInstr))
					{
						if ((int8_t)ton+addVal > 96 || (int8_t)ton+addVal <= 0)
							transpDelNotes++;
					}

					pattPtr++;
				}

				pattPtr += MAX_VOICES - ((pattMark.markX2 + 1) - pattMark.markX1);
			}
		}
		break;

		default: break;
	}
}

void doTranspose(void)
{
	char text[48];
	uint8_t ton;
	uint16_t p, pattLen, ch, row;
	tonTyp *pattPtr;

	countOverflowingNotes(lastInsMode, lastTranspMode, lastTranspVal);
	if (transpDelNotes > 0)
	{
		sprintf(text, "%d note(s) will be erased! Proceed?", (int32_t)transpDelNotes);
		if (okBox(2, "System request", text) != 1)
			return;
	}

	// lastTranspVal is never <-12 or >12, so unsigned testing for >96 is safe
	switch (lastTranspMode)
	{
		case TRANSP_TRACK:
		{
			pattPtr = patt[editor.editPattern];
			if (pattPtr == NULL)
				return; // empty pattern

			pattPtr += cursor.ch;

			pattLen = pattLens[editor.editPattern];
			for (row = 0; row < pattLen; row++)
			{
				ton = pattPtr->ton;
				if ((ton >= 1 && ton <= 96) && (!lastInsMode || pattPtr->instr == editor.curInstr))
				{
					ton += lastTranspVal;
					if (ton > 96)
						ton = 0; // also handles underflow

					pattPtr->ton = ton;
				}

				pattPtr += MAX_VOICES;
			}
		}
		break;

		case TRANSP_PATT:
		{
			pattPtr = patt[editor.editPattern];
			if (pattPtr == NULL)
				return; // empty pattern

			pattLen = pattLens[editor.editPattern];
			for (row = 0; row < pattLen; row++)
			{
				for (ch = 0; ch < song.antChn; ch++)
				{
					ton = pattPtr->ton;
					if ((ton >= 1 && ton <= 96) && (!lastInsMode || pattPtr->instr == editor.curInstr))
					{
						ton += lastTranspVal;
						if (ton > 96)
							ton = 0; // also handles underflow

						pattPtr->ton = ton;
					}

					pattPtr++;
				}

				pattPtr += MAX_VOICES - song.antChn;
			}
		}
		break;

		case TRANSP_SONG:
		{
			for (p = 0; p < MAX_PATTERNS; p++)
			{
				pattPtr = patt[p];
				if (pattPtr == NULL)
					continue; // empty pattern

				pattLen  = pattLens[p];
				for (row = 0; row < pattLen; row++)
				{
					for (ch = 0; ch < song.antChn; ch++)
					{
						ton = pattPtr->ton;
						if ((ton >= 1 && ton <= 96) && (!lastInsMode || pattPtr->instr == editor.curInstr))
						{
							ton += lastTranspVal;
							if (ton > 96)
								ton = 0; // also handles underflow

							pattPtr->ton = ton;
						}

						pattPtr++;
					}

					pattPtr += MAX_VOICES - song.antChn;
				}
			}
		}
		break;

		case TRANSP_BLOCK:
		{
			if (pattMark.markY1 == pattMark.markY2)
				return; // no pattern marking

			pattPtr = patt[editor.editPattern];
			if (pattPtr == NULL)
				return; // empty pattern

			pattPtr += (pattMark.markY1 * MAX_VOICES) + pattMark.markX1;

			pattLen = pattLens[editor.editPattern];
			for (row = pattMark.markY1; row < pattMark.markY2; row++)
			{
				for (ch = pattMark.markX1; ch <= pattMark.markX2; ch++)
				{
					ton = pattPtr->ton;
					if ((ton >= 1 && ton <= 96) && (!lastInsMode || pattPtr->instr == editor.curInstr))
					{
						ton += lastTranspVal;
						if (ton > 96)
							ton = 0; // also handles underflow

						pattPtr->ton = ton;
					}

					pattPtr++;
				}

				pattPtr += MAX_VOICES - ((pattMark.markX2 + 1) - pattMark.markX1);
			}
		}
		break;

		default: break;
	}

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void trackTranspCurInsUp(void)
{
	lastTranspMode = TRANSP_TRACK;
	lastTranspVal = 1;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void trackTranspCurInsDn(void)
{
	lastTranspMode = TRANSP_TRACK;
	lastTranspVal = -1;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void trackTranspCurIns12Up(void)
{
	lastTranspMode = TRANSP_TRACK;
	lastTranspVal = 12;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void trackTranspCurIns12Dn(void)
{
	lastTranspMode = TRANSP_TRACK;
	lastTranspVal = -12;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void trackTranspAllInsUp(void)
{
	lastTranspMode = TRANSP_TRACK;
	lastTranspVal = 1;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void trackTranspAllInsDn(void)
{
	lastTranspMode = TRANSP_TRACK;
	lastTranspVal = -1;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void trackTranspAllIns12Up(void)
{
	lastTranspMode = TRANSP_TRACK;
	lastTranspVal = 12;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void trackTranspAllIns12Dn(void)
{
	lastTranspMode = TRANSP_TRACK;
	lastTranspVal = -12;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void pattTranspCurInsUp(void)
{
	lastTranspMode = TRANSP_PATT;
	lastTranspVal = 1;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void pattTranspCurInsDn(void)
{
	lastTranspMode = TRANSP_PATT;
	lastTranspVal = -1;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void pattTranspCurIns12Up(void)
{
	lastTranspMode = TRANSP_PATT;
	lastTranspVal = 12;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void pattTranspCurIns12Dn(void)
{
	lastTranspMode = TRANSP_PATT;
	lastTranspVal = -12;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void pattTranspAllInsUp(void)
{
	lastTranspMode = TRANSP_PATT;
	lastTranspVal = 1;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void pattTranspAllInsDn(void)
{
	lastTranspMode = TRANSP_PATT;
	lastTranspVal = -1;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void pattTranspAllIns12Up(void)
{
	lastTranspMode = TRANSP_PATT;
	lastTranspVal = 12;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void pattTranspAllIns12Dn(void)
{
	lastTranspMode = TRANSP_PATT;
	lastTranspVal = -12;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void songTranspCurInsUp(void)
{
	lastTranspMode = TRANSP_SONG;
	lastTranspVal = 1;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void songTranspCurInsDn(void)
{
	lastTranspMode = TRANSP_SONG;
	lastTranspVal = -1;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void songTranspCurIns12Up(void)
{
	lastTranspMode = TRANSP_SONG;
	lastTranspVal = 12;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void songTranspCurIns12Dn(void)
{
	lastTranspMode = TRANSP_SONG;
	lastTranspVal = -12;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void songTranspAllInsUp(void)
{
	lastTranspMode = TRANSP_SONG;
	lastTranspVal = 1;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void songTranspAllInsDn(void)
{
	lastTranspMode = TRANSP_SONG;
	lastTranspVal = -1;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void songTranspAllIns12Up(void)
{
	lastTranspMode = TRANSP_SONG;
	lastTranspVal = 12;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void songTranspAllIns12Dn(void)
{
	lastTranspMode = TRANSP_SONG;
	lastTranspVal = -12;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void blockTranspCurInsUp(void)
{
	lastTranspMode = TRANSP_BLOCK;
	lastTranspVal = 1;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void blockTranspCurInsDn(void)
{
	lastTranspMode = TRANSP_BLOCK;
	lastTranspVal = -1;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void blockTranspCurIns12Up(void)
{
	lastTranspMode = TRANSP_BLOCK;
	lastTranspVal = 12;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void blockTranspCurIns12Dn(void)
{
	lastTranspMode = TRANSP_BLOCK;
	lastTranspVal = -12;
	lastInsMode = TRANSP_CUR_INST;
	doTranspose();
}

void blockTranspAllInsUp(void)
{
	lastTranspMode = TRANSP_BLOCK;
	lastTranspVal = 1;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void blockTranspAllInsDn(void)
{
	lastTranspMode = TRANSP_BLOCK;
	lastTranspVal = -1;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void blockTranspAllIns12Up(void)
{
	lastTranspMode = TRANSP_BLOCK;
	lastTranspVal = 12;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void blockTranspAllIns12Dn(void)
{
	lastTranspMode = TRANSP_BLOCK;
	lastTranspVal = -12;
	lastInsMode = TRANSP_ALL_INST;
	doTranspose();
}

void copyNote(tonTyp *src, tonTyp *dst)
{
	if (editor.copyMaskEnable)
	{
		if (editor.copyMask[0]) dst->ton = src->ton;
		if (editor.copyMask[1]) dst->instr = src->instr;
		if (editor.copyMask[2]) dst->vol = src->vol;
		if (editor.copyMask[3]) dst->effTyp = src->effTyp;
		if (editor.copyMask[4]) dst->eff = src->eff;
	}
	else
	{
		*dst = *src;
	}
}

void pasteNote(tonTyp *src, tonTyp *dst)
{
	if (editor.copyMaskEnable)
	{
		if (editor.copyMask[0] && (src->ton    != 0 || !editor.transpMask[0])) dst->ton = src->ton;
		if (editor.copyMask[1] && (src->instr  != 0 || !editor.transpMask[1])) dst->instr = src->instr;
		if (editor.copyMask[2] && (src->vol    != 0 || !editor.transpMask[2])) dst->vol = src->vol;
		if (editor.copyMask[3] && (src->effTyp != 0 || !editor.transpMask[3])) dst->effTyp = src->effTyp;
		if (editor.copyMask[4] && (src->eff    != 0 || !editor.transpMask[4])) dst->eff = src->eff;
	}
	else
	{
		*dst = *src;
	}
}

void cutTrack(void)
{
	tonTyp *pattPtr = patt[editor.editPattern];
	if (pattPtr == NULL)
		return;

	const int16_t pattLen = pattLens[editor.editPattern];

	if (config.ptnCutToBuffer)
	{
		memset(trackCopyBuff, 0, MAX_PATT_LEN * sizeof (tonTyp));
		for (int16_t i = 0; i < pattLen; i++)
			copyNote(&pattPtr[(i * MAX_VOICES) + cursor.ch], &trackCopyBuff[i]);

		trkBufLen = pattLen;
	}

	pauseMusic();
	for (int16_t i = 0; i < pattLen; i++)
		pasteNote(&clearNote, &pattPtr[(i * MAX_VOICES) + cursor.ch]);
	resumeMusic();

	killPatternIfUnused(editor.editPattern);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void copyTrack(void)
{
	tonTyp *pattPtr = patt[editor.editPattern];
	if (pattPtr == NULL)
		return;

	const int16_t pattLen = pattLens[editor.editPattern];

	memset(trackCopyBuff, 0, MAX_PATT_LEN * sizeof (tonTyp));
	for (int16_t i = 0; i < pattLen; i++)
		copyNote(&pattPtr[(i * MAX_VOICES) + cursor.ch], &trackCopyBuff[i]);

	trkBufLen = pattLen;
}

void pasteTrack(void)
{
	if (trkBufLen == 0 || !allocatePattern(editor.editPattern))
		return;

	tonTyp *pattPtr = patt[editor.editPattern];
	const int16_t pattLen = pattLens[editor.editPattern];

	pauseMusic();
	for (int16_t i = 0; i < pattLen; i++)
		pasteNote(&trackCopyBuff[i], &pattPtr[(i * MAX_VOICES) + cursor.ch]);
	resumeMusic();

	killPatternIfUnused(editor.editPattern);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void cutPattern(void)
{
	tonTyp *pattPtr = patt[editor.editPattern];
	if (pattPtr == NULL)
		return;

	const int16_t pattLen = pattLens[editor.editPattern];

	if (config.ptnCutToBuffer)
	{
		memset(ptnCopyBuff, 0, (MAX_PATT_LEN * MAX_VOICES) * sizeof (tonTyp));
		for (int16_t x = 0; x < song.antChn; x++)
		{
			for (int16_t i = 0; i < pattLen; i++)
				copyNote(&pattPtr[(i * MAX_VOICES) + x], &ptnCopyBuff[(i * MAX_VOICES) + x]);
		}

		ptnBufLen = pattLen;
	}

	pauseMusic();
	for (int16_t x = 0; x < song.antChn; x++)
	{
		for (int16_t i = 0; i < pattLen; i++)
			pasteNote(&clearNote, &pattPtr[(i * MAX_VOICES) + x]);
	}
	resumeMusic();

	killPatternIfUnused(editor.editPattern);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void copyPattern(void)
{
	tonTyp *pattPtr = patt[editor.editPattern];
	if (pattPtr == NULL)
		return;

	const int16_t pattLen = pattLens[editor.editPattern];

	memset(ptnCopyBuff, 0, (MAX_PATT_LEN * MAX_VOICES) * sizeof (tonTyp));
	for (int16_t x = 0; x < song.antChn; x++)
	{
		for (int16_t i = 0; i < pattLen; i++)
			copyNote(&pattPtr[(i * MAX_VOICES) + x], &ptnCopyBuff[(i * MAX_VOICES) + x]);
	}

	ptnBufLen = pattLen;

	ui.updatePatternEditor = true;
}

void pastePattern(void)
{
	if (ptnBufLen == 0)
		return;

	if (pattLens[editor.editPattern] != ptnBufLen)
	{
		if (okBox(1, "System request", "Change pattern length to copybuffer's length?") == 1)
			setPatternLen(editor.editPattern, ptnBufLen);
	}

	if (!allocatePattern(editor.editPattern))
		return;

	tonTyp *pattPtr = patt[editor.editPattern];
	const int16_t pattLen = pattLens[editor.editPattern];

	pauseMusic();
	for (int16_t x = 0; x < song.antChn; x++)
	{
		for (int16_t i = 0; i < pattLen; i++)
			pasteNote(&ptnCopyBuff[(i * MAX_VOICES) + x], &pattPtr[(i * MAX_VOICES) + x]);
	}
	resumeMusic();

	killPatternIfUnused(editor.editPattern);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void cutBlock(void)
{
	if (pattMark.markY1 == pattMark.markY2 || pattMark.markY1 > pattMark.markY2)
		return;

	tonTyp *pattPtr = patt[editor.editPattern];
	if (pattPtr == NULL)
		return;

	if (config.ptnCutToBuffer)
	{
		for (int16_t x = pattMark.markX1; x <= pattMark.markX2; x++)
		{
			for (int16_t y = pattMark.markY1; y < pattMark.markY2; y++)
			{
				assert(x < song.antChn && y < pattLens[editor.editPattern]);
				copyNote(&pattPtr[(y * MAX_VOICES) + x],
				         &blkCopyBuff[((y - pattMark.markY1) * MAX_VOICES) + (x - pattMark.markX1)]);
			}
		}
	}

	pauseMusic();
	for (int16_t x = pattMark.markX1; x <= pattMark.markX2; x++)
	{
		for (int16_t y = pattMark.markY1; y < pattMark.markY2; y++)
			pasteNote(&clearNote, &pattPtr[(y * MAX_VOICES) + x]);
	}
	resumeMusic();

	markXSize = pattMark.markX2 - pattMark.markX1;
	markYSize = pattMark.markY2 - pattMark.markY1;
	blockCopied = true;

	killPatternIfUnused(editor.editPattern);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void copyBlock(void)
{
	if (pattMark.markY1 == pattMark.markY2 || pattMark.markY1 > pattMark.markY2)
		return;

	tonTyp *pattPtr = patt[editor.editPattern];
	if (pattPtr == NULL)
		return;

	for (int16_t x = pattMark.markX1; x <= pattMark.markX2; x++)
	{
		for (int16_t y = pattMark.markY1; y < pattMark.markY2; y++)
		{
			assert(x < song.antChn && y < pattLens[editor.editPattern]);
			copyNote(&pattPtr[(y * MAX_VOICES) + x],
			         &blkCopyBuff[((y - pattMark.markY1) * MAX_VOICES) + (x - pattMark.markX1)]);
		}
	}

	markXSize = pattMark.markX2 - pattMark.markX1;
	markYSize = pattMark.markY2 - pattMark.markY1;
	blockCopied = true;
}

void pasteBlock(void)
{
	if (!blockCopied || !allocatePattern(editor.editPattern))
		return;

	const int16_t pattLen = pattLens[editor.editPattern];

	const int32_t xpos = cursor.ch;
	const int32_t ypos = editor.pattPos;

	int32_t j = markXSize;
	if (j+xpos >= song.antChn)
		j = song.antChn - xpos - 1;

	int32_t k = markYSize;
	if (k+ypos >= pattLen)
		k = pattLen - ypos;

	tonTyp *pattPtr = patt[editor.editPattern];

	pauseMusic();
	for (int32_t x = xpos; x <= xpos+j; x++)
	{
		for (int32_t y = ypos; y < ypos+k; y++)
		{
			assert(x < song.antChn && y < pattLen);
			pasteNote(&blkCopyBuff[((y - ypos) * MAX_VOICES) + (x - xpos)], &pattPtr[(y * MAX_VOICES) + x]);
		}
	}
	resumeMusic();

	killPatternIfUnused(editor.editPattern);

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

static void remapInstrXY(uint16_t nr, uint16_t x1, uint16_t y1, uint16_t x2, uint16_t y2, uint8_t src, uint8_t dst)
{
	// this routine is only used sanely, so no need to check input

	tonTyp *pattPtr = patt[nr];
	if (pattPtr == NULL)
		return;

	const int32_t noteSkipLen = MAX_VOICES - ((x2 + 1) - x1);
	tonTyp *note = &pattPtr[(y1 * MAX_VOICES) + x1];

	for (uint16_t y = y1; y <= y2; y++)
	{
		for (uint16_t x = x1; x <= x2; x++)
		{
			assert(x < song.antChn && y < pattLens[nr]);
			if (note->instr == src)
				note->instr = dst;

			note++;
		}

		note += noteSkipLen;
	}
}

void remapBlock(void)
{
	if (editor.srcInstr == editor.curInstr || pattMark.markY1 == pattMark.markY2 || pattMark.markY1 > pattMark.markY2)
		return;

	pauseMusic();
	remapInstrXY(editor.editPattern,
	             pattMark.markX1, pattMark.markY1,
	             pattMark.markX2, pattMark.markY2 - 1,
	             editor.srcInstr, editor.curInstr);
	resumeMusic();

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void remapTrack(void)
{
	if (editor.srcInstr == editor.curInstr)
		return;

	pauseMusic();
	remapInstrXY(editor.editPattern,
	             cursor.ch, 0,
	             cursor.ch, pattLens[editor.editPattern] - 1,
	             editor.srcInstr, editor.curInstr);
	resumeMusic();

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void remapPattern(void)
{
	if (editor.srcInstr == editor.curInstr)
		return;

	pauseMusic();
	remapInstrXY(editor.editPattern,
	             0, 0,
	             (uint16_t)(song.antChn - 1), pattLens[editor.editPattern] - 1,
	             editor.srcInstr, editor.curInstr);
	resumeMusic();

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

void remapSong(void)
{
	if (editor.srcInstr == editor.curInstr)
		return;

	pauseMusic();
	for (int32_t i = 0; i < MAX_PATTERNS; i++)
	{
		const uint8_t pattNr = (uint8_t)i;

		remapInstrXY(pattNr,
		             0, 0,
		             (uint16_t)(song.antChn - 1), pattLens[pattNr] - 1,
		             editor.srcInstr, editor.curInstr);
	}
	resumeMusic();

	ui.updatePatternEditor = true;
	setSongModifiedFlag();
}

// "scale-fade volume" routines

static int8_t getNoteVolume(tonTyp *note)
{
	int8_t nv, vv, ev;

	if (note->vol >= 0x10 && note->vol <= 0x50)
		vv = note->vol - 0x10;
	else
		vv = -1;

	if (note->effTyp == 0xC)
		ev = MIN(note->eff, 64);
	else
		ev = -1;

	if (note->instr != 0 && instr[note->instr] != NULL)
		nv = (int8_t)instr[note->instr]->samp[0].vol;
	else
		nv = -1;

	int8_t finalv = -1;
	if (nv >= 0) finalv = nv;
	if (vv >= 0) finalv = vv;
	if (ev >= 0) finalv = ev;

	return finalv;
}

static void setNoteVolume(tonTyp *note, int8_t newVol)
{
	if (newVol < 0)
		return;

	const int8_t oldv = getNoteVolume(note);
	if (note->vol == oldv)
		return; // volume is the same

	if (note->effTyp == 0x0C)
		note->eff = newVol; // Cxx effect
	else
		note->vol = 0x10 + newVol; // volume column
}

static void scaleNote(uint16_t ptn, int8_t ch, int16_t row, double dScale)
{
	if (patt[ptn] == NULL)
		return;

	const int16_t pattLen = pattLens[ptn];
	if (row < 0 || row >= pattLen || ch < 0 || ch >= song.antChn)
		return;

	tonTyp *note = &patt[ptn][(row * MAX_VOICES) + ch];

	int32_t vol = getNoteVolume(note);
	if (vol >= 0)
	{
		vol = (int32_t)((vol * dScale) + 0.5); // rounded
		vol = MIN(MAX(0, vol), 64);
		setNoteVolume(note, (int8_t)vol);
	}
}

static bool askForScaleFade(char *msg)
{
	char volstr[32+1];

	sprintf(volstr, "%0.2f,%0.2f", dVolScaleFK1, dVolScaleFK2);
	if (inputBox(1, msg, volstr, sizeof (volstr) - 1) != 1)
		return false;

	bool err = false;

	char *val1 = volstr;
	if (strlen(val1) < 3)
		err = true;

	char *val2 = strchr(volstr, ',');
	if (val2 == NULL || strlen(val2) < 3)
		err = true;

	if (err)
	{
		okBox(0, "System message", "Invalid constant expressions.");
		return false;
	}

	dVolScaleFK1 = atof(val1);
	dVolScaleFK2 = atof(val2+1);

	return true;
}

void scaleFadeVolumeTrack(void)
{
	if (!askForScaleFade("Volume scale-fade track (start-, end scale)"))
		return;

	if (patt[editor.editPattern] == NULL)
		return;

	const int32_t pattLen = pattLens[editor.editPattern];

	double dIPy = 0.0;
	if (pattLen > 0)
		dIPy = (dVolScaleFK2 - dVolScaleFK1) / pattLen;

	double dVol = dVolScaleFK1;

	pauseMusic();
	for (int16_t row = 0; row < pattLen; row++)
	{
		scaleNote(editor.editPattern, cursor.ch, row, dVol);
		dVol += dIPy;
	}
	resumeMusic();
}

void scaleFadeVolumePattern(void)
{
	if (!askForScaleFade("Volume scale-fade pattern (start-, end scale)"))
		return;

	if (patt[editor.editPattern] == NULL)
		return;

	const int32_t pattLen = pattLens[editor.editPattern];

	double dIPy = 0.0;
	if (pattLen > 0)
		dIPy = (dVolScaleFK2 - dVolScaleFK1) / pattLen;

	double dVol = dVolScaleFK1;

	pauseMusic();
	for (int16_t row = 0; row < pattLen; row++)
	{
		for (int8_t ch = 0; ch < song.antChn; ch++)
			scaleNote(editor.editPattern, ch, row, dVol);

		dVol += dIPy;
	}
	resumeMusic();
}

void scaleFadeVolumeBlock(void)
{
	if (!askForScaleFade("Volume scale-fade block (start-, end scale)"))
		return;

	if (patt[editor.editPattern] == NULL || pattMark.markY1 == pattMark.markY2 || pattMark.markY1 > pattMark.markY2)
		return;

	const int32_t dy = pattMark.markY2 - pattMark.markY1;

	double dIPy = 0.0;
	if (dy > 0)
		dIPy = (dVolScaleFK2 - dVolScaleFK1) / dy;

	double dVol = dVolScaleFK1;

	pauseMusic();
	for (int16_t row = pattMark.markY1; row < pattMark.markY2; row++)
	{
		for (int16_t ch = pattMark.markX1; ch <= pattMark.markX2; ch++)
			scaleNote(editor.editPattern, (uint8_t)ch, row, dVol);

		dVol += dIPy;
	}
	resumeMusic();
}

void toggleCopyMaskEnable(void) { editor.copyMaskEnable ^= 1; }
void toggleCopyMask0(void) { editor.copyMask[0] ^= 1; };
void toggleCopyMask1(void) { editor.copyMask[1] ^= 1; };
void toggleCopyMask2(void) { editor.copyMask[2] ^= 1; };
void toggleCopyMask3(void) { editor.copyMask[3] ^= 1; };
void toggleCopyMask4(void) { editor.copyMask[4] ^= 1; };
void togglePasteMask0(void) { editor.pasteMask[0] ^= 1; };
void togglePasteMask1(void) { editor.pasteMask[1] ^= 1; };
void togglePasteMask2(void) { editor.pasteMask[2] ^= 1; };
void togglePasteMask3(void) { editor.pasteMask[3] ^= 1; };
void togglePasteMask4(void) { editor.pasteMask[4] ^= 1; };
void toggleTranspMask0(void) { editor.transpMask[0] ^= 1; };
void toggleTranspMask1(void) { editor.transpMask[1] ^= 1; };
void toggleTranspMask2(void) { editor.transpMask[2] ^= 1; };
void toggleTranspMask3(void) { editor.transpMask[3] ^= 1; };
void toggleTranspMask4(void) { editor.transpMask[4] ^= 1; };
