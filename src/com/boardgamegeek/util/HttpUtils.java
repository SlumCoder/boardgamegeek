package com.boardgamegeek.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

public class HttpUtils {
	private final static String TAG = "HttpUtils";

	public static final String BASE_URL = "http://boardgamegeek.com/xmlapi/";
	public static final String BASE_URL_2 = "http://boardgamegeek.com/xmlapi2/";

	private static final int TIMEOUT_SECS = 60;
	private static final int BUFFER_SIZE = 8192;
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";

	public static String constructSearchUrl(String searchTerm, boolean useExact) {
		// http://boardgamegeek.com/xmlapi/search?search=puerto+rico
		// http://boardgamegeek.com/xmlapi2/search?query=puerto+rico
		String queryUrl = BASE_URL + "search?search=" + URLEncoder.encode(searchTerm);
		if (useExact) {
			queryUrl += "&exact=1";
		}
		Log.d(TAG, "Query: " + queryUrl);
		return queryUrl;
	}

	public static String constructHotnessUrl() {
		return BASE_URL_2 + "hot";
	}

	public static String constructGameUrl(String gameId) {
		// http://www.boardgamegeek.com/xmlapi/boardgame/13,1098&stats=1
		return BASE_URL + "boardgame/" + gameId + "?stats=1";
	}

	public static String constructDesignerUrl(int designerId) {
		// http://www.boardgamegeek.com/xmlapi/designer/13
		return BASE_URL + "designer/" + designerId;
	}

	public static String constructArtistUrl(int artistId) {
		// http://www.boardgamegeek.com/xmlapi/boardgameartist/9798
		return BASE_URL + "boardgameartist/" + artistId;
	}

	public static String constructPublisherUrl(int publisherId) {
		// http://www.boardgamegeek.com/xmlapi/publisher/10
		return BASE_URL + "publisher/" + publisherId;
	}

	public static String constructGameUrl(List<String> gameIds) {

		if (gameIds == null) {
			return null;
		}

		StringBuilder ids = new StringBuilder();
		for (int i = 0; i < gameIds.size(); i++) {
			if (i > 0) {
				ids.append(",");
			}
			if (gameIds.get(i) != null) {
				ids.append(gameIds.get(i));
			}
		}

		return constructGameUrl(ids.toString());
	}

	public static String constructUserUrl(String username) {
		return constructUserUrl(username, false);
	}

	public static String constructUserUrl(String username, boolean includeBuddies) {
		String url = BASE_URL_2 + "user?name=" + URLEncoder.encode(username);
		if (includeBuddies) {
			url = url + "&buddies=1";
		}
		return url;
	}

	public static String constructCollectionUrl(String username, String filter) {
		return BASE_URL + "collection/" + URLEncoder.encode(username)
				+ (TextUtils.isEmpty(filter) ? "" : "?" + filter + "=1");
	}

	public static String constructCollectionUrl(String username, String filterIn, List<String> filterOut) {
		String out = "";
		for (int i = 0; i < filterOut.size(); i++) {
			String outValue = filterOut.get(i).trim();
			if (!TextUtils.isEmpty(outValue)) {
				out += ";" + outValue + "=0";
			}
		}
		return BASE_URL + "collection/" + URLEncoder.encode(username)
				+ (TextUtils.isEmpty(filterIn) ? "" : "?" + filterIn + "=1" + out);
	}
	
	public static String constructCommentsUrl(int gameId, int page)
	{
		return BASE_URL_2 + "thing?id=" + gameId + "&comments=1&page=" + page;
	}
	
	public static String constructForumlistUrl(int gameId)
	{
		return BASE_URL_2 + "forumlist?id=" + gameId + "&type=thing";
	}
	
	public static String constructForumUrl(String forumId)
	{
		return BASE_URL_2 + "forum?id=" + forumId;
	}
	
	public static String constructThreadUrl(String mThreadId)
	{
		return BASE_URL_2 + "thread?id=" + mThreadId;
	}

	public static HttpClient createHttpClient(Context context, CookieStore cookieStore) {
		final HttpParams params = createHttpParams(context, false);
		final DefaultHttpClient client = new DefaultHttpClient(params);
		client.setCookieStore(cookieStore);
		return client;
	}

	public static HttpClient createHttpClient(Context context, boolean useGzip) {
		final HttpParams params = createHttpParams(context, useGzip);
		final DefaultHttpClient client = new DefaultHttpClient(params);
		if (useGzip) {
			addGzipInterceptors(client);
		}
		return client;
	}

	private static HttpParams createHttpParams(Context context, boolean useGzip) {
		final HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_SECS * (int) DateUtils.SECOND_IN_MILLIS);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT_SECS * (int) DateUtils.SECOND_IN_MILLIS);
		HttpConnectionParams.setSocketBufferSize(params, BUFFER_SIZE);
		if (useGzip) {
			HttpProtocolParams.setUserAgent(params, buildUserAgent(context));
		}
		return params;
	}

	private static String buildUserAgent(Context context) {
		try {
			final PackageManager manager = context.getPackageManager();
			final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

			// Some APIs require "(gzip)" in the user-agent string.
			return info.packageName + "/" + info.versionName + " (" + info.versionCode + ") (gzip)";
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	private static void addGzipInterceptors(DefaultHttpClient client) {
		client.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) {
				// Add header to accept gzip content
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}
			}
		});

		client.addResponseInterceptor(new HttpResponseInterceptor() {
			public void process(HttpResponse response, HttpContext context) {
				// Inflate any responses compressed with gzip
				final HttpEntity entity = response.getEntity();
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response.getEntity()));
							break;
						}
					}
				}
			}
		});
	}

	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		@Override
		public long getContentLength() {
			return -1;
		}
	}

	public static String parseResponse(HttpResponse response) throws IOException {
		if (response == null) {
			return null;
		}

		final HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null;
		}

		final InputStream stream = entity.getContent();
		if (stream == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.w(TAG, e.toString());
				return null;
			}
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			stream.close();
		}
		return sb.toString().trim();
	}
}
