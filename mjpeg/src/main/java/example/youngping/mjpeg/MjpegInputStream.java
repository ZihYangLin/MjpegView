package example.youngping.mjpeg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.*;
import java.util.Properties;

/**
 * https://stackoverflow.com/questions/3205191/android-and-mjpeg
 */
public class MjpegInputStream extends DataInputStream {
    interface OnStreamFinishedListener {
        void onFinished();
    }

    private static final String TAG = "MjpegInputStream";

    private final byte[] SOI_MARKER = {(byte) 0xFF, (byte) 0xD8};
    private final byte[] EOF_MARKER = {(byte) 0xFF, (byte) 0xD9};
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;
    private OnStreamFinishedListener mListener = null;


    public MjpegInputStream(InputStream in) {
        super(new BufferedInputStream(in));
    }

    public void setListener(OnStreamFinishedListener listener) {
        this.mListener = listener;
    }

    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        byte c = 0;
        for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();

            if (c == sequence[seqIndex]) {
                seqIndex++;
                if (seqIndex == sequence.length) {
                    return i + 1;
                }
            } else {
                seqIndex = 0;
            }
        }
        return -1;
    }

    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }

    // no more accessible
    Bitmap readMjpegFrame() throws IOException {
        mark(FRAME_MAX_LENGTH);
        int headerLen = -1;
        try {
            headerLen = getStartOfSequence(this, SOI_MARKER);
        } catch (IOException e) {
            if (mListener != null) {
                Log.d(TAG, "getStartOfSequence: IOException");
                mListener.onFinished();
                return null;
            }
        }
        if (headerLen < 0) {
            /*stream end*/
            if (mListener != null) {
                Log.d(TAG, "OnMjpegCompeletedListener: headerLen < 0");
                mListener.onFinished();
            }
            return null;
        }
        reset();
        byte[] header = new byte[headerLen];
        readFully(header);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            mContentLength = getEndOfSeqeunce(this, EOF_MARKER);
        }
        reset();
        byte[] frameData = new byte[mContentLength];
        skipBytes(headerLen);
        readFully(frameData);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }
}
