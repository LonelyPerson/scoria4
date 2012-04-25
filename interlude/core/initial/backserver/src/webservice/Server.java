package webservice;

/**
 *
 * @author zenn
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class Server extends Thread {
	private ServerSocket _socket;
        private Timer timer;
        private Socket s;
	public Server() {
		try {
                         InetAddress SocksIps = InetAddress.getByName(Config.BACKSERVER_IP);
			_socket = new ServerSocket(Config.PORT, 0, SocksIps);
			start();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		for(;;) try {
			s = _socket.accept();
			new ClientSocket(s);
                        timer = new Timer();
                        timer.schedule(new CloseSocket(), 5 * 1000);
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
	}
        class CloseSocket extends TimerTask {
            public void run() {
                if(s.isConnected()) {
                    try {
                        s.close();
                    } catch(Exception a) {
                    }
                }
                }
            }
}
