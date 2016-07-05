package cn.ibadi.python;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.alibaba.fastjson.JSONObject;

import cn.ibadi.model.User;

public class TestPost {
	static HttpClientContext clientContext = null;
	private static String host = "http://**/";
	static BasicCookieStore cookieStore = new BasicCookieStore();

	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		String loginUrl = host + "ajaxLogin.html";
		String myContactUrl = host + "user/myperson.html";
		String updateContactInfo = host+"user/ajaxAddOrUpdatePassenger.html";
		String token = StringUtils.EMPTY;
		List<User> listOfUser = new ArrayList<>();
		doLogin(loginUrl);

		String htmlStr = doGet(myContactUrl);
		if (StringUtils.isNotBlank(htmlStr)) {
			Document document = Jsoup.parse(htmlStr);// 转换成为一个文档对象
			if (document != null) {
				token = document.getElementById("accToken").val();// 获取token
				Element inputJson = document.getElementById("passengerJson");
				if (inputJson != null) {

					JSONObject json = JSONObject.parseObject(inputJson.val());
					listOfUser = json.parseArray(json.getString("object"), User.class);
					for (User user : listOfUser) {
						Map<String, String> map = new HashMap<>();
						map.put("accToken", token);
						map.put("passengerId", user.getPassengerId()+"");
						map.put("passengerName", "测试");
						map.put("certificateNo", user.getCertificateNo());
						map.put("mobilePhone", user.getMobilePhone());

						String responseStr = doPost(updateContactInfo, map);
						System.out.println(responseStr);
					}
				}
			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public static void doLogin(String url) {

		CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		HttpPost httpPost = new HttpPost(url);
		Map paramMap = new HashMap<>();
		String email = "***";
		String pwd = "**";
		paramMap.put("userName", email);
		paramMap.put("pwd", pwd);
		UrlEncodedFormEntity postEntity = null;
		try {
			postEntity = new UrlEncodedFormEntity(getParam(paramMap), "UTF-8");
			httpPost.setEntity(postEntity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			// 执行http_post请求
			HttpResponse httpResponse = client.execute(httpPost);
			List<Cookie> listOfCookie = cookieStore.getCookies();
			/*
			 * System.out.println("-----print cookie info start-----"); if
			 * (listofCookie != null && !listofCookie.isEmpty()) { for (Cookie
			 * cookie : listofCookie) {
			 * System.out.println(String.format("%s:%s", cookie.getName(),
			 * cookie.getValue())); } }
			 * 
			 * System.out.println("-----print cookie info end-----\n"); if
			 * (httpResponse.getStatusLine().getStatusCode() == 200) {
			 * System.out.println(EntityUtils.toString(httpResponse.getEntity())
			 * ); }
			 */

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("rawtypes")
	public static String doPost(String url, Map paramMap) {
		CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		List<Cookie> listOfCookie = cookieStore.getCookies();
		HttpPost httpPost = new HttpPost(url);
		if (listOfCookie != null && !listOfCookie.isEmpty()) {
			UrlEncodedFormEntity postEntity = null;
			try {

				postEntity = new UrlEncodedFormEntity(getParam(paramMap), "UTF-8");
				httpPost.setEntity(postEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			try {

				HttpResponse httpResponse = client.execute(httpPost);
				return EntityUtils.toString(httpResponse.getEntity(), Charset.forName("UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return StringUtils.EMPTY;
	}

	public static String doGet(String url) {
		CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		List<Cookie> listOfCookie = cookieStore.getCookies();
		if (listOfCookie != null && !listOfCookie.isEmpty()) {
			HttpGet httpGet = new HttpGet(url);
			try {
				HttpResponse httpResponse = client.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					return EntityUtils.toString(httpResponse.getEntity(), Charset.forName("UTF-8"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return "";
	}

	/**
	 * @return
	 */
	public static Map<String, String> getMyContact() {
		Map<String, String> map = new HashMap<>();

		return map;
	}

	/**
	 * 设置参数
	 * 
	 * @param parameterMap
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<NameValuePair> getParam(Map parameterMap) {
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		Iterator it = parameterMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry parmEntry = (Entry) it.next();
			param.add(new BasicNameValuePair((String) parmEntry.getKey(), (String) parmEntry.getValue()));
		}
		return param;
	}

	/**
	 * 打印返回信息
	 * 
	 * @param httpResponse
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void printResponse(HttpResponse httpResponse) throws ParseException, IOException {
		// 获取消息实体
		HttpEntity entity = httpResponse.getEntity();
		// 响应状态
		System.out.println(String.format("status:%s", httpResponse.getStatusLine()));
		System.out.println("response header");
		HeaderIterator iterator = httpResponse.headerIterator();
		while (iterator.hasNext()) {
			System.out.println("\t" + iterator.next());
		} // 判断响应实体是否为空
		if (entity != null) {
			String responseString = EntityUtils.toString(entity);
			System.out.println("response length:" + responseString.length());
			System.out.println("response content:" + responseString.replace("\r\n", ""));
		}
	}
}
