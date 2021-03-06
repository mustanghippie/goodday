package goodday;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.textfield.TextFields;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by Paulo on 02/05/2017.
 */
public class SettingPageController extends AnchorPane implements Initializable {

    private GoodDayModel gdm = new GoodDayModel();

    @FXML
    TextField locationName;
    @FXML
    RadioButton celsiusRadioButton;
    @FXML
    RadioButton fahrenheitRadioButton;
    @FXML
    Label labelErrorMessage, labelSuccessMessage;
    @FXML
    Button closeSettingPageBtn;

    public SettingPageController() {
        loadFXML();
    }

    private void loadFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("SettingPage.fxml"));
        fxmlLoader.setRoot(this);

        // Sets controller in fxml file myself
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * APPLY CHANGES BUTTON
     * This method applies all the changes made to location
     * and temperature unit.
     *
     * @param
     * @param
     * @return
     * @author Paulo
     */
    @FXML
    protected void applyChanges() {

        String location = locationName.getText();
        int unit = 0;

        if (location.equals("")) location = gdm.getUserData().get("cityName");

        if (fahrenheitRadioButton.isSelected() == true) unit = 2;
        if (celsiusRadioButton.isSelected() == true) unit = 1;

        boolean successFlag = gdm.setUserSetting(location, unit);

        if(!successFlag) {
            labelSuccessMessage.setText("");
            labelErrorMessage.setText("Couldn't find the location. Please choose a city.");
        } else {
            labelErrorMessage.setText("");
            labelSuccessMessage.setText("Your setting is updated successfully");
            gdm.deleteFileFunction("src/weatherInformation.json");
        }
    }

    @FXML
    protected void goToWeatherInformationPage() {
        Main.getInstance().sendWeatherInformationPage();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // User setting
        Map<String,String> userData = gdm.getUserData();
        // Sets user's city name
        locationName.setPromptText(userData.get("cityName"));
        // Sets user's unit
        if (userData.get("unit").equals("C°")) celsiusRadioButton.setSelected(true);
        else fahrenheitRadioButton.setSelected(true);

        // suggestion function
        String[] suggestion = new String[6556];

        try {
            File file = new File("src/goodday/files/Suggestion_list.txt");
            FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < suggestion.length; i++) {
                suggestion[i] = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException");
        }
        TextFields.bindAutoCompletion(locationName, suggestion);
    }
}
