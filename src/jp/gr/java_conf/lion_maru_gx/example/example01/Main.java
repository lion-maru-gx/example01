package jp.gr.java_conf.lion_maru_gx.example.example01;

import java.util.Properties;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jp.gr.java_conf.lion_maru_gx.example.common.MidiUtil;

/**
 * MIDI Monitor
 * @author lion-maru-gx
 *
 */
public class Main extends Application {
	private static Properties prop;
	private static final String configFile = "config.xml";


	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("main.fxml"));
			Scene scene = new Scene(root,800,600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		prop = new Properties();
		/**
		try {
			prop.loadFromXML(new FileInputStream(configFile));
		} catch (InvalidPropertiesFormatException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		**/
		launch(args);
	}

	@Override
	public void stop() throws Exception{
		MidiUtil.close();
		super.stop();
	}
	/**
	 * Propertiesを取得します。
	 *
	 * @return prop
	 */
	public static Properties getProperties() {
	    return prop;
	}
	/**
	 * Property値を取得します。
	 *
	 * @return prop
	 */
	public static String getProperty(String key) {
	    return prop.getProperty(key);
	}

}
