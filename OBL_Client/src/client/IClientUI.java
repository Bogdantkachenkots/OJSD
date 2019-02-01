package client;

import entities.DBMessage;
import entities.User;
/**
 * Every GUI controller that need to get messages from server, will have to implement this.
 * Help us to route the messages from server and to identify the user logged in.
 *
 */
public interface IClientUI
{

	public User getUserLogedIn();
	public void setUserLogedIn(User userLoged);
	public void getMessageFromServer(DBMessage msg);
}
