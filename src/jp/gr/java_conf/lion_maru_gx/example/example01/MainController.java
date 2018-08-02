package jp.gr.java_conf.lion_maru_gx.example.example01;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.Duration;
import jp.gr.java_conf.lion_maru_gx.example.common.MidiUtil;

/**
 * メイン画面処理
 *
 * @author lion-maru-gx
 *
 */
public class MainController implements Initializable {
	/**
	 * 入力デバイスChoiceBox
	 */
	@FXML
	ChoiceBox<String> inputChoice;
	/**
	 * 出力デバイスChoiceBox
	 */
	@FXML
	ChoiceBox<String> outputChoice;
	/**
	 * 送信メッセージ入力TextField
	 */
	@FXML
	TextField sendText;
	/**
	 * 受信メッセージ出力TextArea
	 */
	@FXML
	TextArea log;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		// 入力デバイスChoiceBoxの初期化
		inputChoice.setItems(FXCollections.observableArrayList(MidiUtil.getInputNames()));
		// 選択時の処理を追加
		inputChoice.addEventHandler(ActionEvent.ACTION, (event) ->{
			@SuppressWarnings("unchecked")
			ChoiceBox<String> c = (ChoiceBox<String>) event.getSource();
			MidiUtil.setInputDeviceName(c.getValue());
		});

		// 出力デバイスChoiceBoxの初期化
		outputChoice.setItems(FXCollections.observableArrayList(MidiUtil.getOutputNames()));
		// 選択時の処理を追加
		outputChoice.addEventHandler(ActionEvent.ACTION, (event) ->{
			@SuppressWarnings("unchecked")
			ChoiceBox<String> c = (ChoiceBox<String>) event.getSource();
			MidiUtil.setOutputDeviceName(c.getValue());
		});

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

	}

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

}
