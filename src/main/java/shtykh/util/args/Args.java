package shtykh.util.args;

/**
 * Created by shtykh on 10/04/15.
 */
public abstract class Args {
	private ArgsReceiver receiver;

	protected Args(ArgsReceiver receiver) {
		this.receiver = receiver;
	}

	protected void send(String[] args) {
		receiver.receive(args);
	}
}
