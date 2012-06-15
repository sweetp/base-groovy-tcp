package org.hoschi.sweetp.services.base.tcp.groovy

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test

/**
 * @author Stefan Gojan
 */
@WithGMock
class TcpServiceTest {
	TcpService service

	@Before
	@SuppressWarnings('ThrowException')
	void setUp() {
		service = [handleMessage: {-> throw new Exception()}] as TcpService
	}

	@Test
	void connectToTcpServerDefaultIsLocalhostAt5000() {
		mock(Socket, constructor('localhost', 5000))

		play {
			service.connect()
		}
	}

	@Test
	void connectToServer() {
		mock(Socket, constructor('hoschi', 5555))

		play {
			service.connect('hoschi', 5555)
		}
	}

	@Test(expected = ConnectException)
	void throwsExceptionIfHostIsNotAvailable() {
		play {
			service.connect('localhost', 5555)
		}
	}

	@Test
	void startNewThreadOnListen() {
		def thread = mock(Thread)
		thread.static.start('listener', match {true})

		play {
			service.socket = new Socket()
			service.listen()
		}
	}

	@Test(expected = AssertionError)
	void listenIsOnlyOnceCallable() {
		def thread = mock(Thread)
		thread.static.start('listener', match {true}).returns(new Thread())

		play {
			service.socket = new Socket()
			service.listen()
			service.listen()
		}
	}
}
