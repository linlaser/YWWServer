package com.ywangwang.service.main;

import java.io.IOException;
import java.io.Reader;

/**
 * 为底层字符输入流添加字符缓冲cb数组。提高效率
 * 
 * @version 1.1, 13/11/17
 * @author andyChen
 */

public class MyBufferedReader extends Reader {

	private Reader in;

	private char cb[];
	private int nChars, nextChar;

	private static final int INVALIDATED = -2;
	private static final int UNMARKED = -1;
	private int markedChar = UNMARKED;
	private int readAheadLimit = 0; /* Valid only when markedChar > 0 */

	/** If the next character is a line feed, skip it */
	private boolean skipLF = false;

	/** The skipLF flag when the mark was set */
	private boolean markedSkipLF = false;

	private static int defaultCharBufferSize = 8192;
	private static int defaultExpectedLineLength = 80;

	/**
	 * 根据指定大小和底层字符输入流创建BufferedReader。br
	 */
	public MyBufferedReader(Reader in, int sz) {
		super(in);
		if (sz <= 0)
			throw new IllegalArgumentException("Buffer size <= 0");
		this.in = in;
		cb = new char[sz];
		nextChar = nChars = 0;
	}

	/**
	 * 使用默认大小创建底层输出流的缓冲流
	 */
	public MyBufferedReader(Reader in) {
		this(in, defaultCharBufferSize);
	}

	/** 检测底层字符输入流in是否关闭 */
	private void ensureOpen() throws IOException {
		if (in == null)
			throw new IOException("Stream closed");
	}

	/**
	 * 填充cb。
	 */
	private void fill() throws IOException {
		int dst;
		if (markedChar <= UNMARKED) {
			/* No mark */
			dst = 0;
		} else {
			/* Marked */
			int delta = nextChar - markedChar;
			if (delta >= readAheadLimit) {
				/* Gone past read-ahead limit: Invalidate mark */
				markedChar = INVALIDATED;
				readAheadLimit = 0;
				dst = 0;
			} else {
				if (readAheadLimit <= cb.length) {
					/* Shuffle in the current buffer */
					System.arraycopy(cb, markedChar, cb, 0, delta);
					markedChar = 0;
					dst = delta;
				} else {
					/* Reallocate buffer to accommodate read-ahead limit */
					char ncb[] = new char[readAheadLimit];
					System.arraycopy(cb, markedChar, ncb, 0, delta);
					cb = ncb;
					markedChar = 0;
					dst = delta;
				}
				nextChar = nChars = delta;
			}
		}

		int n;
		do {
			n = in.read(cb, dst, cb.length - dst);
		} while (n == 0);
		if (n > 0) {
			nChars = dst + n;
			nextChar = dst;
		}
	}

	/**
	 * 读取单个字符、以整数形式返回。如果读到in的结尾则返回-1。
	 */
	public int read() throws IOException {
		synchronized (lock) {
			ensureOpen();
			for (;;) {
				if (nextChar >= nChars) {
					fill();
					if (nextChar >= nChars)
						return -1;
				}
				if (skipLF) {
					skipLF = false;
					if (cb[nextChar] == '\n') {
						nextChar++;
						continue;
					}
				}
				return cb[nextChar++];
			}
		}
	}

	/**
	 * 将in中len个字符读取到cbuf从下标off开始长度len中
	 */
	private int read1(char[] cbuf, int off, int len) throws IOException {
		if (nextChar >= nChars) {
			/*
			 * If the requested length is at least as large as the buffer, and if there is no mark/reset activity, and if line feeds are not being skipped, do not bother to copy the characters into the local buffer. In this way buffered streams will cascade harmlessly.
			 */
			if (len >= cb.length && markedChar <= UNMARKED && !skipLF) {
				return in.read(cbuf, off, len);
			}
			fill();
		}
		if (nextChar >= nChars)
			return -1;
		if (skipLF) {
			skipLF = false;
			if (cb[nextChar] == '\n') {
				nextChar++;
				if (nextChar >= nChars)
					fill();
				if (nextChar >= nChars)
					return -1;
			}
		}
		int n = Math.min(len, nChars - nextChar);
		System.arraycopy(cb, nextChar, cbuf, off, n);
		nextChar += n;
		return n;
	}

	/**
	 * 将in中len个字符读取到cbuf从下标off开始长度len中
	 */
	public int read(char cbuf[], int off, int len) throws IOException {
		synchronized (lock) {
			ensureOpen();
			if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}

			int n = read1(cbuf, off, len);
			if (n <= 0)
				return n;
			while ((n < len) && in.ready()) {
				int n1 = read1(cbuf, off + n, len - n);
				if (n1 <= 0)
					break;
				n += n1;
			}
			return n;
		}
	}

	/**
	 * 从in中读取一行、是否忽略换行符
	 */
	@SuppressWarnings("unused")
	String readLine(boolean ignoreLF) throws IOException {
		StringBuffer s = null;
		int startChar;

		synchronized (lock) {
			ensureOpen();
			boolean omitLF = ignoreLF || skipLF;

			long lastTime = System.currentTimeMillis();

			bufferLoop: for (;;) {
				if (in.ready()) {
					if (nextChar >= nChars)
						fill();
					if (nextChar >= nChars) { /* EOF */
						if (s != null && s.length() > 0)
							return s.toString();
						else
							return null;
					}
					boolean eol = false;
					char c = 0;
					int i;

					/* Skip a leftover '\n', if necessary */
					if (omitLF && (cb[nextChar] == '\n'))
						nextChar++;
					skipLF = false;
					omitLF = false;

					charLoop: for (i = nextChar; i < nChars; i++) {
						c = cb[i];
						if ((c == '\n') || (c == '\r')) {
							eol = true;
							break charLoop;
						}
					}

					startChar = nextChar;
					nextChar = i;

					if (eol) {
						String str;
						if (s == null) {
							str = new String(cb, startChar, i - startChar);
						} else {
							s.append(cb, startChar, i - startChar);
							str = s.toString();
						}
						nextChar++;
						if (c == '\r') {
							skipLF = true;
						}
						return str;
					}

					if (s == null)
						s = new StringBuffer(defaultExpectedLineLength);
					s.append(cb, startChar, i - startChar);
				}
				// 超时退出
				if ((System.currentTimeMillis() - lastTime) > 5000) {
					if (s != null && s.length() > 0)
						return s.toString();
					else
						return null;
				}
			}
		}
	}

	/**
	 * 从in中读取一行、
	 */
	public String readLine() throws IOException {
		return readLine(false);
	}

	/**
	 * 丢弃in中n个字符
	 */
	public long skip(long n) throws IOException {
		if (n < 0L) {
			throw new IllegalArgumentException("skip value is negative");
		}
		synchronized (lock) {
			ensureOpen();
			long r = n;
			while (r > 0) {
				if (nextChar >= nChars)
					fill();
				if (nextChar >= nChars) /* EOF */
					break;
				if (skipLF) {
					skipLF = false;
					if (cb[nextChar] == '\n') {
						nextChar++;
					}
				}
				long d = nChars - nextChar;
				if (r <= d) {
					nextChar += r;
					r = 0;
					break;
				} else {
					r -= d;
					nextChar = nChars;
				}
			}
			return n - r;
		}
	}

	/**
	 * 判断cb中是否为空、或者底层in中是否有可读字符。
	 */
	public boolean ready() throws IOException {
		synchronized (lock) {
			ensureOpen();

			/*
			 * If newline needs to be skipped and the next char to be read is a newline character, then just skip it right away.
			 */
			if (skipLF) {
				/*
				 * Note that in.ready() will return true if and only if the next read on the stream will not block.
				 */
				if (nextChar >= nChars && in.ready()) {
					fill();
				}
				if (nextChar < nChars) {
					if (cb[nextChar] == '\n')
						nextChar++;
					skipLF = false;
				}
			}
			return (nextChar < nChars) || in.ready();
		}
	}

	/**
	 * 判断此流是否支持标记
	 */
	public boolean markSupported() {
		return true;
	}

	/**
	 * 标记此流此时的位置、当调用reset方法失效前最多允许读取readAheadLimit个字符。
	 */
	public void mark(int readAheadLimit) throws IOException {
		if (readAheadLimit < 0) {
			throw new IllegalArgumentException("Read-ahead limit < 0");
		}
		synchronized (lock) {
			ensureOpen();
			this.readAheadLimit = readAheadLimit;
			markedChar = nextChar;
			markedSkipLF = skipLF;
		}
	}

	/**
	 * 重置in被最后一次mark的位置。即下一个字符从被最后一次mark的位置开始读取。
	 */
	public void reset() throws IOException {
		synchronized (lock) {
			ensureOpen();
			if (markedChar < 0)
				throw new IOException((markedChar == INVALIDATED) ? "Mark invalid" : "Stream not marked");
			nextChar = markedChar;
			skipLF = markedSkipLF;
		}
	}

	// 关闭此流、释放与此流有关的所有资源
	public void close() throws IOException {
		synchronized (lock) {
			if (in == null)
				return;
			in.close();
			in = null;
			cb = null;
		}
	}
}