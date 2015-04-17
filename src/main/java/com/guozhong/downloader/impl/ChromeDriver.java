package com.guozhong.downloader.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.JsonToBeanConverter;

import com.google.common.collect.ImmutableMap;

public final class ChromeDriver extends org.openqa.selenium.chrome.ChromeDriver {
	private int index ;
	

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ChromeDriver() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ChromeDriver(Capabilities capabilities) {
		super(capabilities);
		// TODO Auto-generated constructor stub
	}

	public ChromeDriver(ChromeDriverService service, Capabilities capabilities) {
		super(service, capabilities);
		// TODO Auto-generated constructor stub
	}

	public ChromeDriver(ChromeDriverService service, ChromeOptions options) {
		super(service, options);
		// TODO Auto-generated constructor stub
	}

	public ChromeDriver(ChromeDriverService service) {
		super(service);
		// TODO Auto-generated constructor stub
	}

	public ChromeDriver(ChromeOptions options) {
		super(options);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void get(String url) {
		super.get(url);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    @SuppressWarnings({"unchecked"})
    public Set<Cookie> getCookies() {
      Object returned = execute(DriverCommand.GET_ALL_COOKIES).getValue();

      List<Map<String, Object>> cookies =
          new JsonToBeanConverter().convert(List.class, returned);
      Set<Cookie> toReturn = new HashSet<Cookie>();
      for (Map<String, Object> rawCookie : cookies) {
        String name = (String) rawCookie.get("name");
        String value = (String) rawCookie.get("value");
        String path = (String) rawCookie.get("path");
        String domain = (String) rawCookie.get("domain");
        boolean secure = rawCookie.containsKey("secure") && (Boolean) rawCookie.get("secure");

        Number expiryNum = (Number) rawCookie.get("expiry");
        Date expiry = expiryNum == null ? null : new Date(
            TimeUnit.SECONDS.toMillis(expiryNum.longValue()));

        toReturn.add(new Cookie.Builder(name, value)
            .path(path)
            .domain(domain)
            .isSecure(secure)
            .expiresOn(expiry)
            .build());
      }

      return toReturn;
    }
    
    public Cookie getCookieNamed(String name) {
        Set<Cookie> allCookies = getCookies();
        for (Cookie cookie : allCookies) {
          if (cookie.getName().equals(name)) {
            return cookie;
          }
        }
        return null;
      }

    public void addCookie(Cookie cookie) {
        cookie.validate();
        execute(DriverCommand.ADD_COOKIE, ImmutableMap.of("cookie", cookie));
      }

      public void deleteCookieNamed(String name) {
        execute(DriverCommand.DELETE_COOKIE, ImmutableMap.of("name", name));
      }

      public void deleteCookie(Cookie cookie) {
        deleteCookieNamed(cookie.getName());
      }

      public void deleteAllCookies() {
        execute(DriverCommand.DELETE_ALL_COOKIES);
      }
	
}
