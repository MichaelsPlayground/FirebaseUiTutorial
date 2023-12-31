package de.androidcrypto.firebaseuitutorial.utils;

import android.content.Context;
import android.net.Uri;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class Okhttp3ProgressDownloader {

    private String downloadUrl;
    private LinearProgressIndicator progressIndicator;
    private Context context;
    private Uri storageUri;

    public Okhttp3ProgressDownloader(String downloadUrl, LinearProgressIndicator progressIndicator, Context context, Uri storageUri) {
        this.downloadUrl = downloadUrl;
        this.progressIndicator = progressIndicator;
        this.context = context;
        this.storageUri = storageUri;
    }

    public void run() {

        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();

        final ProgressListener progressListener = new ProgressListener() {
            boolean firstUpdate = true;

            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                if (done) {
                } else {
                    if (firstUpdate) {
                        firstUpdate = false;
                        if (contentLength == -1) {
                            //System.out.println("content-length: unknown");
                        } else {
                            //System.out.format("content-length: %d\n", contentLength);
                        }
                    }

                    System.out.println(bytesRead);

                    if (contentLength != -1) {
                        //System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
                        progressIndicator.setProgressCompat((int) ((100 * bytesRead) / contentLength), true);
                    }
                }
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                })
                .build();

        // new code
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                // you code to handle response
                // this is using a stream
                InputStream is = response.body().byteStream();
                boolean success = copyInputStreamFromInputStream(context, is, storageUri);
            }
        });
    }

    public boolean copyInputStreamFromInputStream(Context context, InputStream inputStream, Uri outputUri) {

        try (BufferedInputStream in = new BufferedInputStream(inputStream);
             OutputStream out = context.getContentResolver().openOutputStream(outputUri)) {

            // todo check that out is not null !
            byte[] buffer = new byte[8192];
            int nread;
            while ((nread = in.read(buffer)) > 0) {
                out.write(buffer, 0, nread);
            }
            out.flush();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private class ProgressResponseBody extends ResponseBody {
        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }

}
