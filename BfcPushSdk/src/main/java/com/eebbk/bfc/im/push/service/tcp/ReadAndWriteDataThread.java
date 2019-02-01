package com.eebbk.bfc.im.push.service.tcp;

import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.exception.WriteDataException;
import com.eebbk.bfc.im.push.listener.OnConnectInterruptListener;
import com.eebbk.bfc.im.push.tlv.TLVByteBuffer;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

/**
 * 单独开一个线程用来处理数据接收和发送，在android端可能会把线程换成Service来实现以降低线程被销毁的几率
 */
public class ReadAndWriteDataThread extends Thread {
	private static final String TAG = "ReadAndWriteDataThread";
    /**
     * 网络数据读取流
     */
	private InputStream is;

    /**
     * 网络数据写入流
     */
	private OutputStream os;

    /**
     * 数据读取写入线程是否继续运行的标志，true表示继续运行，反之
     */
	private volatile boolean run;

    /**
     * 网络数据读取的缓存大小
     */
	private int receiveBufSize;

    /**
     * 连接断开监听
     */
	private OnConnectInterruptListener interruptListener;

    /**
     * 数据读取写入监听
     */
	private OnDataListener onDataListener;

	/**
	 * TCP数据发送与接收监听
	 */
	public interface OnDataListener {

		/**
		 * 成功写出数据
		 *
		 * @param writeByte
		 *            写出的字节数据长度
		 */
		void onWrite(int writeByte);

		/**
		 * 写数据失败
		 *
		 * @param errorMsg
		 *            异常，包含所写的数据
		 */
		void onWriteError(String errorMsg);

		/**
		 * 读取到数据
		 *
		 * @param data
		 *            经过TLV解码的结果
		 */
		void onRead(byte[] data);
	}
	
	public ReadAndWriteDataThread(InputStream is, OutputStream os,
			OnConnectInterruptListener interruptListener) {
		this.is = is;
		this.os = os;
		this.interruptListener = interruptListener;
		run = true;
		receiveBufSize = 1024;
	}

	@Override
	public void run() {
		LogUtils.d(LogTagConfig.LOG_TAG_IO,"thread run ....");
		int readLength = 0;
		byte[] receiveBuf = new byte[receiveBufSize];
		TLVByteBuffer buffer = new TLVByteBuffer();
		try {
			while ((readLength = is.read(receiveBuf)) != -1 && run) {
				buffer.write(receiveBuf, 0, readLength);
				LogUtils.e(LogTagConfig.LOG_TAG_IO,"read [" + readLength + "] bytes,all buffer bytes:" + buffer.size());
                readBuffer(buffer);
            }
			LogUtils.w("run:" + run + ",readLength:" + readLength);
		} catch (IOException e) {
			LogUtils.e(e);
            if (e instanceof InterruptedIOException) {
                LogUtils.e( TAG, "ReadAndWriteDataThread was interrupted!");
            }
		} finally {
            // 如果连接被中断，但是缓存数据已经读取了部分数据，那么就把这些数据中的完整数据包继续抛给上层sdk，尽量保证数据接收的完整性
			if (buffer.size() > 0) {
				LogUtils.i("buffer size == " + buffer.size() + ",continue to read buffer...");
				readBuffer(buffer);
			}
            receiveBuf = null;
			close();
            buffer.reset();
            LogUtils.w("onInterrupt:" + run);
            interruptListener.onInterrupt(run);
        }
	}

    /**
     * 为了解决TCP粘包问题,这里一定要循环读取,读取到缓冲数组里面的数据不是一个完整的包数据才不读
     * 因为read到数据的时候会将数据write到TLVByteBuffer中这时候调用hasNextTLVData()会更新里面的firstTotalSize
     * firstTotalSize只是代表一个完整数据包的大小，要是同时读取了多个数据包,firstTotalSize就是最早读入的那个数据包的大小
     * 再调用cutNextTLVData()就只会截取最早读入的那个数据包,而后面的数据包将无法被截取出来,导致数据虽然已经读取到TLVByteBuffer
     * 但是没办法截取到下一个完整的数据包而不能把数据抛出到sdk外面,外面自然就是收不到数据导致请求超时,
     * 当寻循环读取的时候就能截取完这个数据包后继续调用hasNextTLVData()重新初始化下一个数据包的firstTotalSize
     * 发现任然存在完整的数据包数据就继续截取直到检测到TLVByteBuffer中的数据不是一个完整的数据包数据为止
     */
    private void readBuffer(TLVByteBuffer buffer) {
        while (buffer.hasNextTLVData()) {
            byte[] data = buffer.cutNextTLVData();
            if (data != null && data.length > 0) {
                LogUtils.i("cut data[" + data.length + "] bytes,rest buffer bytes:" + buffer.size());
                onRead(data);
            } else {
                LogUtils.e( TAG, "data read completely,but cut tlv bytes is null or length = 0.");
            }
        }
    }

	/**
	 * 写数据
	 */
	public void write(byte[] data) throws WriteDataException {
		LogUtils.e(LogTagConfig.LOG_TAG_IO, "write: " + data);
		try {
			if (os != null) {
				os.write(data);
				os.flush();
				onWrite(data == null ? 0 : data.length);
			} else {
				WriteDataException error = new WriteDataException("write data error:write out put stream is null.");
				onWriteError(error);
				throw error;
			}
		} catch (IOException e) {
			WriteDataException error = new WriteDataException("write data error:" + e.toString());
			onWriteError(error);
			throw error;
		}
	}
	
	private void onRead(byte[] data) {
		if (onDataListener != null) {
			onDataListener.onRead(data);
		} else {
			LogUtils.w("onDataListener is null");
		}
	}
	
	private void onWrite(int length) {
		if (onDataListener != null) {
			onDataListener.onWrite(length);
		} else {
			LogUtils.w("onDataListener is null");
		}
	}
	
	private void onWriteError(WriteDataException error) {
		if (onDataListener != null) {
			onDataListener.onWriteError(error.toString());
		} else {
			LogUtils.w("onDataListener is null");
		}
	}
	
	/**
	 * 数据监听
	 */
	public void setOnDataListener(OnDataListener onDataListener) {
		this.onDataListener = onDataListener;
	}
	
	/**
	 * 中断线程
	 */
	public void shutdown() {
		run = false;
		this.interrupt(); // 在linux平台上线程被io阻塞的时候调用interrupt()会抛出InterruptedIOException，但是windows平台不会
		// 关闭读取数据流，避免因read函数阻塞，导致无法退出线程的bug
		close();
	}

	private void close() {
		try {
			if (is != null) {
				is.close();
			}
			if (os != null) {
				os.close();
			}
		} catch (IOException e) {
			LogUtils.e(e);
		}
	}
	
	/**
	 * true表示线程被异常中断，false表示线程被正常中断
	 */
	public boolean isRunnable() {
		return run;
	}

	public int getReceiveBufSize() {
		return receiveBufSize;
	}

	/**
	 * 接收数据缓冲区大小
	 */
	public void setReceiveBufSize(int receiveBufSize) {
		this.receiveBufSize = receiveBufSize;
	}
}
