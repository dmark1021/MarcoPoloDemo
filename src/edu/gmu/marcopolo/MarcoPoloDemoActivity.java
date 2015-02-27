package edu.gmu.marcopolo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.gmu.network.UdpCommPackage;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MarcoPoloDemoActivity extends Activity implements Observer{

	private UdpCommPackage comms;
	private TextView textView;
	private String polo = "Polo!";

	//For more information on locks and concurrency: 
	//http://download.oracle.com/javase/tutorial/essential/concurrency/newlocks.html
	private Lock lock = new ReentrantLock();

	/** Called when the activity is first created. */
	//For more information on the Android Activity lifecycle:
	//http://developer.android.com/guide/topics/fundamentals/activities.html
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.marco);
		textView = (TextView) this.findViewById(R.id.textView);
		try {
			comms = new UdpCommPackage();
			comms.addObserver(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		//For more information on Android menus, also known as the options menu: 
		//http://developer.android.com/guide/topics/ui/menus.html
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.switch_view:
			switchContentView();
			return true;
		case R.id.set_polo:
			showDialog(1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	@Override
	protected Dialog onCreateDialog(int id){
		//For more information on Android dialogs:
		//http://developer.android.com/guide/topics/ui/dialogs.html

		Dialog dialog;
		switch (id){
		case 1:
			dialog = new Dialog (this);

			dialog.setContentView(R.layout.polo_dialog);
			dialog.setTitle("Set Polo Response");

			EditText poloEditText = (EditText) dialog.findViewById(R.id.poloEditText);

			lock.lock();
			poloEditText.setText(polo);
			lock.unlock();

			//when the dialog is dismissed, set the "polo" value to the contents of the EditText 	
			dialog.setOnDismissListener(new PoloDialogDismissListener(poloEditText));

			break;
		default:
			dialog = null;
		}

		return dialog;
	}

	class PoloDialogDismissListener implements DialogInterface.OnDismissListener{

		private EditText poloEditText;
		PoloDialogDismissListener(EditText poloEditText){
			this.poloEditText = poloEditText;
		}
		public void onDismiss(DialogInterface dialog) {

			lock.lock();
			//checks for the "Marco!" as a polo response. 
			if(poloEditText.getText().toString().equals("Marco!")){
				toast("\"Marco!\" is not allowed as the Polo Response, sorry.");
			}
			else{
				polo = poloEditText.getText().toString();
				toast("The Polo Response is set to: \""+polo+"\"");
			}
			lock.unlock();
		}


	}

	private void switchContentView() {
		//switches the content view between the single-button "marco" layout and the "main" layout which can customize the sent messages

		lock.lock();

		CharSequence setText = textView.getText();

		if (this.findViewById(R.layout.main)==null){
			setContentView(R.layout.main);
		}
		else{
			setContentView(R.layout.marco);
		}

		textView = (TextView) this.findViewById(R.id.textView);
		textView.setText(setText);

		lock.unlock();

	}

	public void update(Observable commsObservable, Object packetObject) {
		/*This is called whenever the UdpCommsPackage receives a packet

		//Uses the Observer/Observable pattern
		//http://download.oracle.com/javase/1.4.2/docs/api/java/util/Observer.html
		//http://download.oracle.com/javase/1.4.2/docs/api/java/util/Observable.html
		 */

		//get the datagram packet
		DatagramPacket packet = (DatagramPacket) packetObject;

		//attempts to filter out messages from this device
		String localAddress = comms.getNetworkInterface().getInetAddresses().nextElement().getHostAddress();

		//checks the packet to ensure that it did not originate from this device
		if( !packet.getAddress().getHostAddress().equals(localAddress) ){

			//get packet message
			String str = byteArrayToString( packet.getData() );

			//update the GUI to display the message
			//construct the string to set the text on the main textView
			String setText = "\n" +	packet.getAddress().getHostAddress()+": "+str;

			//Update the text from the UI thread to avoid CalledFromWrongThreadException
			//prevents main UI thread, receive/update thread and the broadcast thread from interfering
			this.runOnUiThread(new TextUpdater(setText));

			//if the message is "Marco!", send the a response of "Polo!"
			if(str.equals("Marco!")){
				broadcastMessage(polo);
			}

		}
	}

	public void broadcastMessage(String str){
		//attempts to broadcast the message through the comms package
		try {
			comms.sendPayload( stringToByteArray(str) );

			//update the GUI to display the message
			//construct the string to set the text on the main textView
			String setText = "\n" +
					"Me: "+str;

			//Update the text from the UI thread to avoid CalledFromWrongThreadException
			//prevents main UI thread, receive/update thread and the broadcast thread from interfering
			this.runOnUiThread(new TextUpdater(setText));

		} catch (Exception e) {
			toast("There was an error sending the message.");
		}
	}

	//
	//onClick methods for buttons
	//
	public void marco_OnClick(View view){
		//upon pressing the "Marco!" button, broadcast "Marco!" 
		broadcastMessage("Marco!");
	}
	public void send_OnClick(View view){
		//upon pressing the "Send" button, broadcast the contents of the EditText box 
		EditText editText = (EditText) this.findViewById(R.id.editText);
		String str = editText.getText().toString();
		broadcastMessage(str);
	}

	public void toast(String messageText){
		//used to make toast messages used to notify the user. The message hovers momentarily and then disappears.
		//http://developer.android.com/guide/topics/ui/notifiers/toasts.html

		Toast toast = Toast.makeText(
				this,
				messageText,
				Toast.LENGTH_LONG
				);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	private class TextUpdater implements Runnable {

		final private String string;
		TextUpdater(String string){
			this.string = string;
		}
		public void run() {
			lock.lock();

			textView.setText(textView.getText()+string);

			lock.unlock();
		}

	}


	//String and byte array conversions
	private String byteArrayToString(byte[] bytes){
		//converts a byte array into a String

		String str = "";

		try {
			ObjectInputStream objStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
			str = objStream.readUTF();
			objStream.close();
		} catch (StreamCorruptedException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return str;
	}

	private byte[] stringToByteArray(String str){
		//converts a String into a byte array

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream ();
		try {
			ObjectOutputStream objStream = new ObjectOutputStream (byteStream);
			objStream.writeUTF(str);
			objStream.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return byteStream.toByteArray();
	}
}