package TUIO;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import TUIO.*;
import rabbitMQ.AmqpClient;

public class TuioDump implements TuioListener {

	public void addTuioObject(TuioObject tobj) {
		try {
			rabbit.sendMessage(new AmqpClient.Message(mapper, tobj, "tuio.object_add"));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateTuioObject(TuioObject tobj) {
		try {
			rabbit.sendMessage(new AmqpClient.Message(mapper, tobj, "tuio.object_update"));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeTuioObject(TuioObject tobj) {
		try {
			rabbit.sendMessage(new AmqpClient.Message(mapper, tobj, "tuio.object_remove"));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addTuioCursor(TuioCursor tcur) {
		try {
			rabbit.sendMessage(new AmqpClient.Message(mapper, tcur, "tuio.touch_add"));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateTuioCursor(TuioCursor tcur) {

		try {
			rabbit.sendMessage(new AmqpClient.Message(mapper, tcur, "tuio.touch_update"));

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeTuioCursor(TuioCursor tcur) {
		try {
			rabbit.sendMessage(new AmqpClient.Message(mapper, tcur, "tuio.touch_remove"));

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addTuioBlob(TuioBlob tblb) {
		System.out.println("add blb " + tblb.getBlobID() + " (" + tblb.getSessionID() + ") " + tblb.getX() + " "
				+ tblb.getY() + " " + tblb.getAngle() + " " + tblb.getWidth() + " " + tblb.getHeight() + " "
				+ tblb.getArea());
	}

	public void updateTuioBlob(TuioBlob tblb) {
		System.out.println("set blb " + tblb.getBlobID() + " (" + tblb.getSessionID() + ") " + tblb.getX() + " "
				+ tblb.getY() + " " + tblb.getAngle() + " " + tblb.getWidth() + " " + tblb.getHeight() + " "
				+ tblb.getArea() + " " + tblb.getMotionSpeed() + " " + tblb.getRotationSpeed() + " "
				+ tblb.getMotionAccel() + " " + tblb.getRotationAccel());
	}

	public void removeTuioBlob(TuioBlob tblb) {
		System.out.println("del blb " + tblb.getBlobID() + " (" + tblb.getSessionID() + ")");
	}

	public void refresh(TuioTime frameTime) {
		// System.out.println("frame "+frameTime.getFrameID()+"
		// "+frameTime.getTotalMilliseconds());
	}

	private static AmqpClient rabbit;
	private static ObjectMapper mapper;
	private static int port = 3333;
	private static TuioClient client;

	public static void main(String argv[]) {

		showUI();

	}

	private static JFrame frame;
	private static JLabel connection;
	private static void showUI() {
		frame = new JFrame("Rafanime's Tuio to RabbitMQ Dumper");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 300);
		frame.setLocation(500, 300);

		FlowLayout layout = new FlowLayout();
		Container con = frame.getContentPane();
		con.setLayout(layout);

		connection = new JLabel("Tuio RabbitMQ Server: Offline", SwingConstants.CENTER);
		frame.getContentPane().add(connection);
		addButtons();

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private static void addButtons() {
		//add buttons
		JButton connect = new JButton("Connect");
		JButton exit = new JButton("Exit");
		List<JButton> buttons = new ArrayList<JButton>();
		buttons.add(connect);
		buttons.add(exit);

		for (JButton button : buttons) {
			button.setPreferredSize(new Dimension(100, 30));
			frame.getContentPane().add(button);
		}


		for (JButton button : buttons) {
			switch (button.getText()) {

			case "Exit":
				button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}

				});
				break;
			case "Connect":
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						if(button.getText().equals("Connect")) {
							client = new TuioClient(port);
							mapper = new ObjectMapper();

							System.out.println("listening to TUIO messages at port " + port);
							client.addTuioListener(new TuioDump());
							client.connect();

							try {
								rabbit = new AmqpClient("tuio", "localhost", new String[] { "html.*" });
								rabbit.connect();
								connection.setText("Tuio RabbitMQ Server: Online");
								button.setText("Disconnect");
							} catch (Exception e1) {
							}
						} else {
							try {
								connection.setText("Tuio RabbitMQ Server: Offline");
								button.setText("Connect");

								rabbit.disconnect();
								client.disconnect();
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}

				});
				break;
			}
		}
	}
}

