package rabbitMQ;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

public class AmqpClient {
	private String exchangeName;
	Connection connection;
	Channel channel;
	String host;
	String[] routingKeys;
	String[] queueNames;
	
	public static class Message {
		byte[] body;
		String routingKey;
		
		public Message(byte[] body, String routingKey) {
			this.body = body;
			this.routingKey = routingKey;
		}
		
		public Message(ObjectMapper mapper, Object object, String routingKey) throws JsonProcessingException {
			body = mapper.writeValueAsBytes(object);
			this.routingKey = routingKey;
		}
		
		public <T> T getBody(ObjectMapper mapper, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
			return mapper.readValue(body, clazz);
		}
	}

	public AmqpClient(String exchangeName, String host, String[] routingKeys) throws Exception {
		this.exchangeName = exchangeName;
		this.host = host;
		this.routingKeys = routingKeys;
	}

	public void connect() throws Exception {
		// basic initialization
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		connection = factory.newConnection();
		channel = connection.createChannel();
		channel.exchangeDeclare(exchangeName, "topic");
		
		// create a queue for every routing key that we are interested in
		queueNames = new String[routingKeys.length];
		for (int i=0; i<routingKeys.length; i++) {
			queueNames[i] = channel.queueDeclare().getQueue();
			channel.queueBind(queueNames[i], exchangeName, routingKeys[i]);
		}
	}
	
	public void disconnect() throws Exception {
		channel.close();
		connection.close();
	}
	
	public void sendMessage(Message m) {
		try {
			channel.basicPublish(exchangeName, m.routingKey, null, m.body);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Message> receiveMessages() {
		List<Message> messages = new ArrayList<Message>();
		for (String queueName : queueNames) {
			try {
				GetResponse response = channel.basicGet(queueName, false);
				if (response != null) {
					messages.add(new Message(response.getBody(), response.getEnvelope().getRoutingKey()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return messages;
	}
}
