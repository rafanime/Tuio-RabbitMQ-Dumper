package rabbitMQ;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HandInfo {

	private static AmqpClient client;
	private static ObjectMapper mapper;
	private static boolean finished;
	
	public static void main(String[] args) throws Exception {

		client = new AmqpClient("tuio", "localhost", new String[]{"*.position"});
		client.connect(); 
		mapper = new ObjectMapper();

		client.sendMessage(new AmqpClient.Message(mapper, "coisas", "kinect.handPosition"));

		while(!finished) {
			List<AmqpClient.Message> messages = client.receiveMessages();
			if (!messages.isEmpty()) {
				System.out.println(messages.get(0).getBody(mapper, object.class).getName());
			}
		}
		
		client.disconnect();	
	}
}
