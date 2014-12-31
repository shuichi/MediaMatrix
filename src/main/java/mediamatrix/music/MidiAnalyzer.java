/* Library for Tonality -- MIDI File Analyzer
 * Copyright (C) 2007 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package mediamatrix.music;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MidiAnalyzer {

    private final Sequence sequence;
    private final InputStream in;
    private URL source;

    public MidiAnalyzer(InputStream in) throws IOException, InvalidMidiDataException {
        this.in = in;
        this.sequence = MidiSystem.getSequence(in);
    }

    public MidiAnalyzer(URL source) throws IOException, InvalidMidiDataException {
        this(source.openStream());
        this.source = source;
    }

    public MusicScore parseSpecificTracks(int[] targets) {
        MusicScore score = new MusicScore();
        if (source != null) {
            score.setName(source.toString());
        }
        score.setLengthAsTick(sequence.getTickLength());
        score.setLengthAsMicrosecond(sequence.getMicrosecondLength());
        score.setResolution(sequence.getResolution());

        boolean isTickPerBeat = false;
        float fDivisionType = sequence.getDivisionType();
        String strDivisionType = null;
        if (fDivisionType == Sequence.PPQ) {
            strDivisionType = "PPQ";
            isTickPerBeat = true;
        } else if (fDivisionType == Sequence.SMPTE_24) {
            strDivisionType = "SMPTE, 24 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_25) {
            strDivisionType = "SMPTE, 25 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_30DROP) {
            strDivisionType = "SMPTE, 29.97 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_30) {
            strDivisionType = "SMPTE, 30 frames per second";
        }

        Track[] tracks = sequence.getTracks();
        for (int i = 0; i < targets.length; i++) {
            for (int nEvent = 0; nEvent < tracks[targets[i]].size(); nEvent++) {
                MidiEvent event = tracks[targets[i]].get(nEvent);
                if (event.getMessage() instanceof ShortMessage) {
                    ShortMessage message = (ShortMessage) event.getMessage();
                    if (message.getCommand() == MIDIMessageCode.ProgramChange) {
                        score.setProgramCode(message.getData1());
                    } else if (message.getCommand() == 0xB0) {
                        if (message.getData1() == 0) {
                            score.setProgramLSB(message.getData2());
                        }
                        if (message.getData1() == 32) {
                            score.setProgramMSB(message.getData2());
                        }
                    }
                    Note note = decodeShortMessage(message, tracks[targets[i]], nEvent, score.getUnitTime());
                    score.addNote(note);
                } else if (event.getMessage() instanceof MetaMessage) {
                    double startTime = tracks[targets[i]].get(nEvent).getTick() * score.getUnitTime();
                    decodeMetaMessage((MetaMessage) event.getMessage(), score, startTime);
                }
            }
        }
        return score;
    }

    public MusicScore parse() {
        MusicScore score = new MusicScore();
        if (source != null) {
            score.setName(source.toString());
        }
        score.setLengthAsTick(sequence.getTickLength());
        score.setLengthAsMicrosecond(sequence.getMicrosecondLength());
        score.setResolution(sequence.getResolution());

        boolean isTickPerBeat = false;
        float fDivisionType = sequence.getDivisionType();
        String strDivisionType = null;
        if (fDivisionType == Sequence.PPQ) {
            strDivisionType = "PPQ";
            isTickPerBeat = true;
        } else if (fDivisionType == Sequence.SMPTE_24) {
            strDivisionType = "SMPTE, 24 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_25) {
            strDivisionType = "SMPTE, 25 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_30DROP) {
            strDivisionType = "SMPTE, 29.97 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_30) {
            strDivisionType = "SMPTE, 30 frames per second";
        }

        Track[] tracks = sequence.getTracks();
        for (int nTrack = 0; nTrack < tracks.length; nTrack++) {
            for (int nEvent = 0; nEvent < tracks[nTrack].size(); nEvent++) {
                MidiEvent event = tracks[nTrack].get(nEvent);
                if (event.getMessage() instanceof ShortMessage) {
                    ShortMessage message = (ShortMessage) event.getMessage();
                    if (message.getCommand() == MIDIMessageCode.ProgramChange) {
                        score.setProgramCode(message.getData1());
                    } else if (message.getCommand() == 0xB0) {
                        if (message.getData1() == 0) {
                            score.setProgramLSB(message.getData2());
                        }
                        if (message.getData1() == 32) {
                            score.setProgramMSB(message.getData2());
                        }
                    }
                    Note note = decodeShortMessage(message, tracks[nTrack], nEvent, score.getUnitTime());
                    score.addNote(note);
                } else if (event.getMessage() instanceof MetaMessage) {
                    double startTime = tracks[nTrack].get(nEvent).getTick() * score.getUnitTime();
                    decodeMetaMessage((MetaMessage) event.getMessage(), score, startTime);
                }
            }
        }
        return score;
    }

    public MusicScore[] parseMultiTrack() {
        boolean isTickPerBeat = false;
        float fDivisionType = sequence.getDivisionType();
        String strDivisionType = null;
        if (fDivisionType == Sequence.PPQ) {
            strDivisionType = "PPQ";
            isTickPerBeat = true;
        } else if (fDivisionType == Sequence.SMPTE_24) {
            strDivisionType = "SMPTE, 24 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_25) {
            strDivisionType = "SMPTE, 25 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_30DROP) {
            strDivisionType = "SMPTE, 29.97 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_30) {
            strDivisionType = "SMPTE, 30 frames per second";
        }

        final List<MusicScore> result = new ArrayList<MusicScore>();
        final Track[] tracks = sequence.getTracks();
        List<Tempo> tempo = null;
        for (int nTrack = 0; nTrack < tracks.length; nTrack++) {
            final MusicScore score = new MusicScore();
            if (source != null) {
                score.setName(source.toString());
            }
            result.add(score);
            score.setLengthAsTick(sequence.getTickLength());
            score.setLengthAsMicrosecond(sequence.getMicrosecondLength());
            score.setResolution(sequence.getResolution());
            for (int nEvent = 0; nEvent < tracks[nTrack].size(); nEvent++) {
                final MidiEvent event = tracks[nTrack].get(nEvent);
                if (event.getMessage() instanceof ShortMessage) {
                    ShortMessage message = (ShortMessage) event.getMessage();
                    if (message.getCommand() == MIDIMessageCode.ProgramChange) {
                        score.setProgramCode(message.getData1());
                    } else if (message.getCommand() == 0xB0) {
                        if (message.getData1() == 0) {
                            score.setProgramLSB(message.getData2());
                        }
                        if (message.getData1() == 32) {
                            score.setProgramMSB(message.getData2());
                        }
                    }
                    final Note note = decodeShortMessage(message, tracks[nTrack], nEvent, score.getUnitTime());
                    score.addNote(note);
                } else if (event.getMessage() instanceof MetaMessage) {
                    double startTime = tracks[nTrack].get(nEvent).getTick() * score.getUnitTime();
                    decodeMetaMessage((MetaMessage) event.getMessage(), score, startTime);
                }
            }
            if (tempo == null && score.getTempo().size() > 0) {
                tempo = score.getTempo();
            } else {
                score.setMicrosecondsPerQuarterNote((long) tempo.get(0).getTempo());
                score.setTempo(tempo);
            }
        }
        return result.toArray(new MusicScore[result.size()]);
    }

    public void close() throws IOException {
        in.close();
    }

    private long findNearestNoteOff(Track track, int nEvent, ShortMessage noteOn) {
        for (int i = nEvent; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            ShortMessage message = null;
            if (event.getMessage() instanceof ShortMessage) {
                message = (ShortMessage) event.getMessage();
                if (message.getCommand() == MIDIMessageCode.NoteOff || (message.getCommand() == MIDIMessageCode.NoteOn && message.getData2() == 0)) {
                    if (message.getData1() == noteOn.getData1()) {
                        return event.getTick();
                    }
                }
            }
        }
        return -1;
    }

    private Note decodeShortMessage(ShortMessage message, Track track, int nEvent, double unitTime) {
        if ((message.getCommand() != MIDIMessageCode.NoteOn) || (message.getData2() <= 0)) {
            return null;
        }
        int keyNumber = message.getData1();
        int velocity = message.getData2();
        double startTime = track.get(nEvent).getTick() * unitTime;
        double length = 0;
        long noteOffTime = findNearestNoteOff(track, nEvent, message);
        if (noteOffTime > -1) {
            length = (noteOffTime - track.get(nEvent).getTick()) * unitTime;
        }
        return new Note(keyNumber, velocity, startTime, length);
    }

    private void decodeMetaMessage(MetaMessage message, MusicScore score, double startTime) {
        byte[] abMessage = message.getMessage();
        byte[] abData = message.getData();
        int nDataLength = message.getLength();

        switch (message.getType()) {
            case MIDIMessageCode.MicrosecondsPerQuarterNote:
                int sec = ((abData[0] & 0xFF) << 16) | ((abData[1] & 0xFF) << 8) | (abData[2] & 0xFF);
                score.addTempo(new Tempo(startTime, sec));
                score.setMicrosecondsPerQuarterNote(sec);
                break;

            case MIDIMessageCode.SMPTEOffset:
                String offset = (abData[0] & 0xFF) + ":" + (abData[1] & 0xFF) + ":" + (abData[2] & 0xFF) + "." + (abData[3] & 0xFF) + "." + (abData[4] & 0xFF);
                break;

            case MIDIMessageCode.TimeSignature:
                int timeSigElement = abData[0] & 0xFF;
                int timeSigDenominator = 1 << (abData[1] & 0xFF);
                int clocksperMetronome = abData[2] & 0xFF;
                int midiClock = abData[3] & 0xFF;
                score.setClocksperMetronome(clocksperMetronome);
                score.setTimeSigDenominator(timeSigDenominator);
                score.setTimeSigElement(timeSigElement);
                score.setMidiClock(midiClock);
                break;

            case MIDIMessageCode.KeySignature:
                if (abData[1] == 1) {
                    score.setGender(false);
                } else {
                    score.setGender(true);
                }
                score.setKeySignature(abData[0] + 7);
                break;

            default:
                break;
        }
    }
}

class MIDIMessageCode {

    public static final int NoteOff = 0x80;
    public static final int NoteOn = 0x90;
    public static final int ProgramChange = 0xC0;
    public static final int MicrosecondsPerQuarterNote = 0x51;
    public static final int SMPTEOffset = 0x54;
    public static final int TimeSignature = 0x58;
    public static final int KeySignature = 0x59;
}
