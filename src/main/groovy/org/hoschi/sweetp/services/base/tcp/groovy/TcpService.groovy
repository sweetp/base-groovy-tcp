package org.hoschi.sweetp.services.base.tcp.groovy

import groovy.json.JsonBuilder
import net.sf.json.groovy.JsonSlurper

/**
 * Abstract class which provides common functionality for a TCP service written
 * in groovy. Concrete classes must provide a field 'log' with a logger in it.
 *
 * @author Stefan Gojan
 */
abstract class TcpService {
	Socket socket
	protected Thread listener

	/**
	 * Connect to server.
	 * @param host name of server
	 * @param port of server
	 */
	void connect(String host = 'localhost', int port = 5000) {
		socket = new Socket(host, port)
	}

	/**
	 * Start listen on the created socket by connect method.
	 */
	void listen() {
		assert socket
		assert listener == null, 'only one listener is save, and it exists one'

		listener = Thread.start('listener', {
			while (socket.isConnected() && !socket.isClosed()) {
				log.debug "socket status: connected:${socket.isConnected()} - bound:${socket.isBound()} - closed:${socket.isClosed()}"
				this.handleMessage(socket)
			}
			listener = null
			log.info 'disconnected, die'
		})
	}

	/**
	 * Handles messages from a given socket and call 'parseMessage' method
	 * with the message from the socket.
	 * <p>
	 * If the 'parseMessage' method don't throw any exception a 200 is returned
	 * with the Object returned from 'parseMessage'. If 'parseMessage' throws a
	 * MissingMethodException it returns 404. If another Exception is thrown,
	 * it returns 500.
	 *
	 * @param socket to read the input and write the output.
	 */
	@SuppressWarnings('CatchException')
	void handleMessage(Socket socket) {
		assert socket
		InputStream input = socket.inputStream
		OutputStream output = socket.outputStream

		BufferedReader reader = new BufferedReader(new InputStreamReader(input))
		def msg = reader.readLine()
		log.info "message from server: $msg"
		if (msg == null) {
			socket.close()
			log.info 'closing socket'
			return
		}

		def resp = new JsonBuilder()

		try {
			def parsed = parseMessage(msg)
			resp {
				status 200
				data parsed
			}
		} catch (MissingMethodException ex) {
			resp {
				status 404
				data ex.message
			}
			log.debug "404: $ex.message $ex.stackTrace"
		} catch (Exception ex) {
			resp {
				status 500
				data ex.message
			}
			log.debug "500: $ex.message $ex.stackTrace"
		}

		log.info "response is: $resp"
		def writer = new PrintWriter(output)
		writer.println(resp.toString())
		writer.flush()
		log.debug 'message is send'
	}

	/**
	 * Execute method from server on this instance.
	 * Attention: Make sure every Method has a parameter with type Map.
	 *
	 * @param message to scan
	 * @return what ever the called method returned
	 */
	Object parseMessage(String message) {
		def slurper = new JsonSlurper()
		def json = slurper.parseText(message)
		assert json

		def result = null

		if (json.containsKey('method')) {
			log.debug "call method $json.method"
			result = this."$json.method"(json.params)

		} else {
			log.error "no method property in json $json"
		}

		result
	}

	/**
	 * Get config of service as list of maps which provide configuration
	 * objects.
	 *
	 * @return list with configurations maps to convert that to json
	 */
	abstract List getConfig(Map params)

}
