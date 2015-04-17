package com.guozhong.proxy;

public final class ProxyIp {
	
	private final String ip;
	
	private final int port ;
	
	/**
	 * IP被请求了多少次
	 */
	private int requestCount = 0;
	
	
	
	/**
	 * 取得时的时间
	 */
	private final long fetchTime ;
	
	private ProxyIpPool owner ;
	
	public ProxyIp(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.fetchTime = System.currentTimeMillis();
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public ProxyIpPool getOwner() {
		return owner;
	}

	protected void setOwner(ProxyIpPool owner) {
		this.owner = owner;
	}
	
	public long getFetchTime() {
		return fetchTime;
	}
	
	public final int incrementRequestCount(){
		return ++requestCount;
	}
	
	/**
	 * 标记缓存
	 */
	public final void markCache(){
		owner.cache(this);
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProxyIp other = (ProxyIp) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	public String toString(){
		return  ip +":" + port;
	}
}
