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
 * メイン画面
 * 
 * @author lion-maru-gx
 *
 */
public class MainController implements Initializable {
	@FXML
	TextArea log;

	@FXML
	ChoiceBox<String> inputChoice;
	@FXML
	ChoiceBox<String> outputChoice;
	@FXML
	TextField sendText;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		inputChoice.setItems(FXCollections.observableArrayList(MidiUtil.getInputNames()));

		EventHandler<ActionEvent> inputChoiceChanged = (event) -> this.inputChoiceChanged(event);

		inputChoice.addEventHandler(ActionEvent.ACTION, inputChoiceChanged);
		outputChoice.setItems(FXCollections.observableArrayList(MidiUtil.getOutputNames()));
		EventHandler<ActionEvent> outputChoiceChanged = (event) -> this.outputChoiceChanged(event);

		outputChoice.addEventHandler(ActionEvent.ACTION, outputChoiceChanged);

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

		// 正規表現のパターンを作成
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
	}

	@FXML
	public void handleExit() {
		MidiUtil.close();
		Platform.exit();
	}

	@FXML
	public void handleSend() {
		if (MidiUtil.getOutputPort() == null) {
			return;
		}
		String text = sendText.getText();
		if (text.length() > 0) {
			MidiUtil.sendMessage(text);
		}

	}

	@SuppressWarnings("unchecked")
	private void inputChoiceChanged(ActionEvent e) {
		ChoiceBox<String> c = (ChoiceBox<String>) e.getSource();
		MidiUtil.setInputPort(c.getValue());
	}

	private void outputChoiceChanged(ActionEvent e) {
		@SuppressWarnings("unchecked")
		ChoiceBox<String> c = (ChoiceBox<String>) e.getSource();
		MidiUtil.setOutputPort(c.getValue());
	}

}
