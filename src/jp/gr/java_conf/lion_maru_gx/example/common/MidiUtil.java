package jp.gr.java_conf.lion_maru_gx.example.common;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;
import javax.xml.bind.DatatypeConverter;

/**
 * Midiデバイスの取得
 *
 * @author lion-maru-gx
 *
 */
public class MidiUtil {
	private static MidiDevice.Info[] outputMidiDeviceInfo;
	private static MidiDevice.Info[] inputMidiDeviceInfo;

	private static String[] outputNames;
	private static String[] inputNames;

	private static Receiver receiver;
	private static MidiDevice inputPort = null;
	private static MidiDevice outputPort = null;
	private static LinkedList<MidiMessage> inputMessages = new LinkedList<>();

	/**
	 * staticの初期化
	 */
	static {
		try {
			outputMidiDeviceInfo = createOutputMidiDeviceInfo();
			inputMidiDeviceInfo = createInputMidiDeviceInfo();

			outputNames = new String[outputMidiDeviceInfo.length];
			for (int i = 0; i < outputMidiDeviceInfo.length; i++) {
				outputNames[i] = outputMidiDeviceInfo[i].getName();
			}

			inputNames = new String[inputMidiDeviceInfo.length];
			for (int i = 0; i < inputMidiDeviceInfo.length; i++) {
				inputNames[i] = inputMidiDeviceInfo[i].getName();
			}
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}

		// MIDI入力メッセージ
		// receiver = new MessageReceiver(inputMessages);
		receiver = new Receiver() {

			@Override
			public void close() {
				inputMessages.clear();
			}

			@Override
			public void send(MidiMessage message, long timeStamp) {
				if (message == null) {
					return;
				}
				System.out.println(message.getMessage()[0]);
				if (message.getMessage()[0] == (byte) 0xf8) {
					return;
				}
				if (message.getMessage()[0] == (byte) 0xfe) {
					return;
				}

				inputMessages.offer(message);
			}

		};

	}
	/** emulate the no MIDI device condition for debugging */
	private final static boolean EMULATE_NO_MIDI_IN = false;
	private final static boolean EMULATE_NO_MIDI_OUT = false;

	/**
	 * 入力デバイス取得
	 *
	 * @return
	 * @throws MidiUnavailableException
	 */
	private static MidiDevice.Info[] createInputMidiDeviceInfo() throws MidiUnavailableException {
		ArrayList<MidiDevice.Info> list = new ArrayList<>();

		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			// throws MidiUnavailableException
			MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
			if (device.getMaxTransmitters() != 0 && !(device instanceof Synthesizer) && !(device instanceof Sequencer)
					&& !EMULATE_NO_MIDI_IN)
				list.add(infos[i]);
		}
		return list.toArray(new MidiDevice.Info[0]);
	}

	/**
	 * 出力デバイス取得
	 *
	 * @return
	 * @throws MidiUnavailableException
	 */
	private static MidiDevice.Info[] createOutputMidiDeviceInfo() throws MidiUnavailableException {
		ArrayList<MidiDevice.Info> list = new ArrayList<>();

		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			// throws MidiUnavailableException
			MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
			if (device.getMaxReceivers() != 0 && !(device instanceof Synthesizer) && !(device instanceof Sequencer)
					&& !EMULATE_NO_MIDI_OUT)
				list.add(infos[i]);
		}
		return list.toArray(new MidiDevice.Info[0]);
	}

	/**
	 * 出力デバイス取得
	 *
	 * @return
	 */
	public static MidiDevice.Info[] getOutputMidiDeviceInfo() {
		return outputMidiDeviceInfo;
	}

	/**
	 * 入力デバイス取得
	 *
	 * @return
	 */
	public static MidiDevice.Info[] getInputMidiDeviceInfo() {
		return inputMidiDeviceInfo;
	}

	/**
	 *
	 * @return
	 */
	public static String[] getOutputNames() {
		return outputNames;
	}

	/**
	 *
	 * @return
	 */
	public static String[] getInputNames() {
		return inputNames;
	}

	/**
	 *
	 * @param deviceName
	 * @return
	 */
	public static MidiDevice getInputMidiDevice(String deviceName) {
		for (MidiDevice.Info info : inputMidiDeviceInfo) {
			if (info.getName().equals(deviceName)) {
				try {
					return MidiSystem.getMidiDevice(info);
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;

	}

	/**
	 *
	 * @param deviceName
	 * @return
	 */
	public static MidiDevice getOutputMidiDevice(String deviceName) {
		for (MidiDevice.Info info : outputMidiDeviceInfo) {
			if (info.getName().equals(deviceName)) {
				try {
					return MidiSystem.getMidiDevice(info);
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;

	}

	/**
	 *
	 * @return
	 */
	public static MidiDevice getInputPort() {
		return inputPort;
	}

	/**
	 *
	 * @param iInputPort
	 */
	public static void setInputPort(MidiDevice iInputPort) {
		inputPort = iInputPort;
		if (!inputPort.isOpen()) {
			try {
				inputPort.open();
			} catch (MidiUnavailableException e1) {
				e1.printStackTrace();
			}
		}
		try {
			inputPort.getTransmitter().setReceiver(receiver);
		} catch (MidiUnavailableException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 *
	 * @param sInputPort
	 */
	public static void setInputPort(String sInputPort) {
		inputPort = getInputMidiDevice(sInputPort);
		if (!inputPort.isOpen()) {
			try {
				inputPort.open();
			} catch (MidiUnavailableException e1) {
				e1.printStackTrace();
			}
		}
		try {
			inputPort.getTransmitter().setReceiver(receiver);
		} catch (MidiUnavailableException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 *
	 * @return
	 */
	public static MidiDevice getOutputPort() {
		return outputPort;
	}

	/**
	 *
	 * @param iOutputPort
	 */
	public static void setOutputPort(MidiDevice iOutputPort) {
		outputPort = iOutputPort;
		if (!outputPort.isOpen()) {
			try {
				outputPort.open();
			} catch (MidiUnavailableException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 *
	 * @param sOutputPort
	 */
	public static void setOutputPort(String sOutputPort) {
		outputPort = getOutputMidiDevice(sOutputPort);
		if (!outputPort.isOpen()) {
			try {
				outputPort.open();
			} catch (MidiUnavailableException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public static String getInputMessage() {
		String msg = "";
		if (!inputMessages.isEmpty()) {
			MidiMessage midiMsg = inputMessages.poll();
			if (midiMsg instanceof ShortMessage) {
				msg = DatatypeConverter.printHexBinary(midiMsg.getMessage());
			} else if (midiMsg instanceof SysexMessage) {
				msg = DatatypeConverter.printHexBinary(midiMsg.getMessage());
			}
		}
		return msg;
	}

	/**
	 *
	 * @return
	 */
	public static ShortMessage getInputShortMessage() {
		while (!inputMessages.isEmpty()) {
			MidiMessage midiMsg = inputMessages.poll();
			if (midiMsg instanceof ShortMessage) {
				return (ShortMessage) midiMsg;
			}
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	public static SysexMessage getInputSysexMessage() {
		while (!inputMessages.isEmpty()) {
			MidiMessage midiMsg = inputMessages.poll();
			if (midiMsg instanceof SysexMessage) {
				return (SysexMessage) midiMsg;
			}
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	public static String getInputMessages() {
		String msg = "";
		while (!inputMessages.isEmpty()) {
			MidiMessage midiMsg = inputMessages.poll();
			if (midiMsg instanceof ShortMessage) {
				msg = msg + DatatypeConverter.printHexBinary(midiMsg.getMessage()) + "\n";
			} else if (midiMsg instanceof SysexMessage) {
				msg = msg + DatatypeConverter.printHexBinary(midiMsg.getMessage()) + "\n";
			}
		}
		return msg;
	}

	/**
	 *
	 */
	public static void close() {
		if (outputPort != null) {
			if (outputPort.isOpen()) {
				outputPort.close();
			}
		}
		if (inputPort != null) {
			if (inputPort.isOpen()) {
				inputPort.close();
			}
		}
	}

	/**
	 *
	 * @param message
	 */
	public static void sendMessage(String message) {
		byte[] buff = DatatypeConverter.parseHexBinary(message);
		sendMessage(buff);
	}

	/**
	 *
	 * @param message
	 */
	public static void sendMessage(byte[] message) {
		SysexMessage msg = new SysexMessage();
		try {
			msg.setMessage(message, message.length);
			getOutputPort().getReceiver().send(msg, 0);
		} catch (InvalidMidiDataException | MidiUnavailableException e) {
			e.printStackTrace();
		}
	}

	public static String getInputDeviceName() {
		if (inputPort != null) {
			return inputPort.getDeviceInfo().getName();
		}
		return "";
	}

	public static String getOutputDeviceName() {
		if (outputPort != null) {
			return outputPort.getDeviceInfo().getName();
		}
		return "";
	}
}
