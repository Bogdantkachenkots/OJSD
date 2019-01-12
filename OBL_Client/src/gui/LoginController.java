package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import entities.DBMessage;
import entities.DBMessage.DBAction;
import entities.User;
import gui.GuiManager.SCREENS;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import javafx.stage.Stage;

public class LoginController implements IClientUI, Initializable
{

	private Node thisNode;
	@FXML
	private JFXTextField userNameTextField;

	@FXML
	private Label warningLabel;
	@FXML
	private JFXPasswordField passwordTextFIeld;
	@FXML
	private JFXButton loginBtn;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		if (GuiManager.client == null)
		{
			warningLabel.setText("Error connecting to server");
			loginBtn.setDisable(true);
		}
	}

	@FXML
	void loginBtnClick(ActionEvent event)
	{ // press on login button
		try
		{
			warningLabel.setText("");
			thisNode = ((Node) event.getSource());
			GuiManager.client.CheckValidUser(new User(userNameTextField.getText(), passwordTextFIeld.getText()));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@FXML
	void openSearchScreen(MouseEvent event) throws IOException
	{
		((Node) event.getSource()).getScene().getWindow().hide(); // hiding primary window
		GuiManager.SwitchScene(SCREENS.searchBook);

	}

	@Override
	public void getMessageFromServer(DBMessage msg)
	{
		try
		{
			if (msg.Action == DBAction.RETCheckUser)
			{
				if (((User) msg.Data) != null)// if the user exist
				{
					if (((User) msg.Data).getUserName() == null)// if the user already connected
					{
						Platform.runLater(() -> {
							warningLabel.setText("User aleady connected");
						});
					} else
					{
						;
						// Avoid throwing IllegalStateException by running from a non-JavaFX thread.
						Platform.runLater(() -> {
							thisNode.getScene().getWindow().hide();
							GuiManager.SwitchScene(GuiManager.userTypeFromString.get(((User) msg.Data).getType()));
							GuiManager.CurrentGuiController.setUserLogedIn(((User) msg.Data));
						});
					}
				} else
				{
					Platform.runLater(() -> {
						warningLabel.setText("Wrong user name or password.");
					});
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void setUserLogedIn(User userLoged)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public User getUserLogedIn()
	{
		// TODO Auto-generated method stub
		return null;
	}

}