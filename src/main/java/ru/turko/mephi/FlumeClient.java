package ru.turko.mephi;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;
import java.nio.charset.Charset;


/**
 * Class Flume client
 */
public class FlumeClient {
    private RpcClient client;
    private String host;
    private int port;

	/**
	* Constructor
	*/
    public FlumeClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.client = RpcClientFactory.getDefaultInstance(host, port);
    }
	
	/**
	* Create a Flume Event object
	*/
    public void sendData(String data) {
        Event event = EventBuilder.withBody(data, Charset.forName("UTF-8"));
        try {
            client.append(event);
        } catch (EventDeliveryException e) {
            client.close();
            client = RpcClientFactory.getDefaultInstance(host, port);
        }
    }

	/**
	* Close the RPC connection
	*/
    public void clean() {
        client.close();
    }
}
