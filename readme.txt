example01(MIDI Moniter)の説明
0.初めに
　今後MIDI音源モジュール用のファイラーを作成するにあたって、MIDI関連クラスJavaFXの試用及び、MIDI通信のモニタリングがするために本プログラムを作成した。
そのためにかなり怪しい使い方になっているとは思うが、とりあえず動作したので公開します。

１．画面構成（main.fxml）
　画面はSceneBuliderで作成しました。構成は以下です。
　・終了ボタン（Button）
　・送信ボタン（Button）
　・MIDI出力選択（ChoiceBox）
　・送信メッセージ入力（TextField）
　・MIDI入力選択（ChoiceBox）
　・受信メッセージ出力（TextArea）
　選択にはComboBoxとChoiceBoxがありますが選択項目以外を指定する必要がない場合はChoiceBoxを使用します。

２．Main.java
　JavaFXアプリケーションのお約束でjavafx.application.Applicationから継承しています。
　今後のひな型とするためにプロパティファイルの読み書きをするようにしています。
　XMLファイルにしましたがデフォルトではネスティングを行えないためあまり意味がなさそうです。
　今回は入力デバイスと出力デバイスを指定できるようにしています。
　初回開始時はファイルが無いためにexceptionダンプが出力されますが処理には影響はありません。
　そして、お約束のlaunch(args)を呼び出すことによりstart(Stage primaryStage)が呼び出されます。
　start()内では単純にJavaFXのfxmlを呼び出して画面を初期化し表示しています。

３．MainController.java
　main画面のコントローラクラスです。
　入力デバイスChoiceBox／入力デバイスChoiceBoxの初期化を行っています。
　MidiUtilから入力デバイス名のリストを取得して設定します。
		inputChoice.setItems(FXCollections.observableArrayList(MidiUtil.getInputNames()));

　選択時にデバイス変更を行うイベントの追加します。
　本来のイベント処理はSceneBulider(fxml)で設定するのですが、選択時のイベントが用意されていないため追加する必要があります。
　外部(fxml)から呼び出す必要がありませのでラムダ式で記述しています。
　選択が行われた場合にはMidiUtilでデバイスの変更を行います。
		// 選択時の処理を追加
		inputChoice.addEventHandler(ActionEvent.ACTION, (event) ->{
			@SuppressWarnings("unchecked")
			ChoiceBox<String> c = (ChoiceBox<String>) event.getSource();
			MidiUtil.setInputDeviceName(c.getValue());
		});
　送信メッセージ入力は16進で指定します。そのため不必要な文字が入力されないようにTextFormatterで指定します。
　正規表現のパターンでしていします。
		// 送信メッセージ入力TextFieldの初期化
		// 正規表現のパターンを作成(16進)
		Pattern hexPattern = Pattern.compile("[^0-9a-fA-F]+");
		TextFormatter<String> lowerFormatter = new TextFormatter<>(change -> {
			String newStr = hexPattern.matcher(change.getText()).replaceAll("");
			int diffcount = change.getText().length() - newStr.length();
			change.setAnchor(change.getAnchor() - diffcount);
			change.setCaretPosition(change.getCaretPosition() - diffcount);
			change.setText(newStr);
			return change;
		});
		sendText.setTextFormatter(lowerFormatter);

　受信メッセージをタイマー監視で出力するようします。
　最初はMidiUtil内で受信時に直接書き換えるようにしていたのですが、スレッド競合でエラーが発生してしまいました。
　あまり良いやり方ではないのですがスレッド競合をさけるためにこの方法をとりました。
		// メッセージ受信をタイマーで監視
		Timeline timer = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String msg = MidiUtil.getInputMessages();
				if (msg.length() > 0) {
					log.appendText(msg);
				}
			}
		}));

		timer.setCycleCount(Animation.INDEFINITE);
		timer.play();
　終了ボタン処理と送信ボタン処理はSceneBulider(fxml)で設定を行いますので@FXMLを指定してpublicで定義します。
	/**
	 * 終了ボタン処理
	 */
	@FXML
	public void handleExit() {
		MidiUtil.close();
		Platform.exit();
	}

	/**
	 * 送信ボタン処理
	 */
	@FXML
	public void handleSend() {
		if (MidiUtil.getOutputDevice() == null) {
			return;
		}
		String text = sendText.getText();
		if (text.length() > 0) {
			MidiUtil.sendMessage(text);
		}

	}

４．MidiUtil.java
　MIDI関連処理をまとめて定義しています。
　まず最初にMIDI入力用のレシーバの定義を行います。
　非同期通信のため受信メッセージはQueueで管理するようにします。
　動作確認でYAMAHA を使った際に0xf8、0xfeを大量に受信したために拭っています。
	/**
	 * staticの初期化
	 */
	static {

		// MIDI入力用レシーバの定義
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

　MIDIデバイス情報はMidiSystem.getMidiDeviceInfo()で取得できますが入力／出力／その他の区別が無いために
　 MidiSystem.getMidiDevice()で取得後にgetMaxReceivers()とgetMaxTransmitters()で判定します。
　またSynthesizerとSequencerを除くためにinstanceofで除きます。
	/**
	 * 出力デバイス情報リスト取得
	 *
	 * @return
	 */
	public static MidiDevice.Info[] getOutputMidiDeviceInfo() {
		ArrayList<MidiDevice.Info> list = new ArrayList<>();

		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			// throws MidiUnavailableException
			try {
				MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
				if (device.getMaxReceivers() != 0 && !(device instanceof Synthesizer) && !(device instanceof Sequencer))
					list.add(infos[i]);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
		return list.toArray(new MidiDevice.Info[0]);
	}

	/**
	 * 入力デバイス情報リストの取得
	 *
	 * @return デバイス情報リスト
	 */
	public static MidiDevice.Info[] getInputMidiDeviceInfo() {
		ArrayList<MidiDevice.Info> list = new ArrayList<>();

		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			try {
				MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
				if (device.getMaxTransmitters() != 0 && !(device instanceof Synthesizer)
						&& !(device instanceof Sequencer))
					list.add(infos[i]);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
		return list.toArray(new MidiDevice.Info[0]);
	}

　入力メッセージはByte列で取得されるので、表示用に16文字列に変換します。
　以前は個別に変換処理を作成する必要がありましたが現行バージョンでは標準パッケージjavax.xml.bind.DatatypeConverteのprintHexBinary()で簡単に変換することができます。
	/**
	 * 入力メッセージの取得
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

　メッセージ送信時の変換も同様にparseHexBinary()で簡単に変換できます。
	/**
	 * メッセージ送信
	 *
	 * @param message
	 */
	public static void sendMessage(String message) {
		byte[] buff = DatatypeConverter.parseHexBinary(message);
		sendMessage(buff);
	}


