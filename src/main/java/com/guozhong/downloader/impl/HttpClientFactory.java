package com.guozhong.downloader.impl;

import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientFactory {
	
	public static final int USE_DEFAULT_HTTPCLIENT = 0;
	public static final int USE_HTTPS_HTTPCLIENT = 1;
	
	/**
	 * 创建模式默认是USE_DEFAULT_HTTPCLIENT，需要可手动更改为USE_HTTPS_HTTPCLIENT
	 */
	public static int CREATE_MODE = USE_DEFAULT_HTTPCLIENT;
	
	public static final String defaultEncoding = "utf-8";
	private static HttpClientFactory instance = null;
	private PoolingHttpClientConnectionManager connManager=null;
	static{
		instance = new HttpClientFactory();
	}
	public static HttpClientFactory getInstance(){
		return instance;
	}
	
	/**
	 * 构建一个普通的httpClient。
	 * @return
	 */
	public  CloseableHttpClient buildDefaultHttpClient(BasicCookieStore cookieStore) {
		CloseableHttpClient client = null;
		switch(CREATE_MODE){
		case USE_DEFAULT_HTTPCLIENT:
			HttpClientBuilder builder = HttpClientBuilder.
	        create().setDefaultCookieStore(cookieStore);
	        client = builder.build();
			break;
		case USE_HTTPS_HTTPCLIENT:
			client = buildHttpsClient(cookieStore);
			break;
		default:
				throw new RuntimeException("不支持该类型");
		}
		return client;
	}
	
	/**
	 * 获取HttpClient实例,http和https各种进！~.~！留下cookie的参数设计！应对验证码用户交互的登陆过程
	 */
	private CloseableHttpClient buildHttpsClient(BasicCookieStore cookieStore) {
        //设置连接参数
        ConnectionConfig connConfig = ConnectionConfig.custom().setCharset(Charset.forName(defaultEncoding)).build();
        SocketConfig socketConfig = SocketConfig.custom().build();
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);
        try { 
            //指定信任密钥存储对象和连接套接字工厂
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            SSLContext sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore,
                    new AnyTrustStrategy()).build();
            LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            registryBuilder.register("https", sslSF);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        //设置连接管理器
        if(connManager == null){
        	 connManager = new PoolingHttpClientConnectionManager(registry);
        }
        connManager.setDefaultConnectionConfig(connConfig);
        connManager.setDefaultSocketConfig(socketConfig);
        //LaxRedirectStrategy可以自动重定向所有的HEAD，GET，POST请求，解除了http规范对post请求重定向的限制。
        //LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();
        //构建客户端
        HttpClientBuilder builder = HttpClientBuilder.
        create().
        setDefaultCookieStore(cookieStore).
        setConnectionManager(connManager);
        CloseableHttpClient httpClient = builder.build();
        return httpClient;
    	}
	}
final class AnyTrustStrategy implements TrustStrategy{
	//绕过安全证书！默认所有都通过
	@Override
	public boolean isTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		return true;
	}
	
}
