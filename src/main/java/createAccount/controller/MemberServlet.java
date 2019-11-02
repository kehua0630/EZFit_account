package createAccount.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import createAccount.model.EncrypAES;
import createAccount.model.MemberBean;
import createAccount.service.CodeServiceImpl;
import createAccount.service.MemberServiceImpl;

@WebServlet("/createAccount/memberServlet.do")
public class MemberServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		// 錯誤訊息
		Map<String, String> errorMsg = new HashMap<String, String>();
		// 註冊成功
		Map<String, String> msgOK = new HashMap<String, String>();

		HttpSession session = request.getSession();
		request.setAttribute("MsgMap", errorMsg); // 顯示錯誤訊息
		session.setAttribute("MsgOK", msgOK); // 顯示正常訊息

		String name = request.getParameter("memberName");
		String email = request.getParameter("memberEmail");
		String password = request.getParameter("memberPassword");

		String code = request.getParameter("veriCode").trim();
		CodeServiceImpl csi = new CodeServiceImpl();
		MemberServiceImpl msi = new MemberServiceImpl();
		String correctCode = csi.queryCode(email);

		// 檢查驗證碼
		if (!code.equals(correctCode)) {
			errorMsg.put("errorCode", "驗證碼錯誤");

		} else if (msi.emailExists(email)) {
			errorMsg.put("errorEmail", "email重複申請");

		} else {
			// 密碼加密
			password = EncrypAES.getMD5Endocing(EncrypAES.encryptString(password));
			Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());

			MemberBean mb = new MemberBean(null, email, password, name, null, null, null, null, null, null, null, ts);
			msi.saveMember(mb);

			response.sendRedirect("../login/login.jsp");
			return;

		}

		// 如果 errorMsgMap 不是空的，表示有錯誤
		if (!errorMsg.isEmpty()) {
			RequestDispatcher rd = request.getRequestDispatcher("/createAccount/createAccount.jsp");
			rd.forward(request, response);
			return;
		}

	}

	// for FB login
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		MemberServiceImpl msi = new MemberServiceImpl();

		String access_token = request.getParameter("access_token");

		// 和Graph API連線，用access_tokenf取得基本資料
		String url = "https://graph.facebook.com/v4.0/me?fields=id,name,picture,email&access_token=" + access_token;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		// optional default is GET
		con.setRequestMethod("GET");
		// add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer stringBuffer = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			stringBuffer.append(inputLine);
		}
		in.close();
		System.out.println(stringBuffer);
		MemberBean mb = new MemberBean();
		Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		try {
			JSONObject myResponse = new JSONObject(stringBuffer.toString());
			mb.setName(myResponse.getString("name"));
			mb.setEmail(myResponse.getString("email"));
			mb.setRegisterTime(ts);
			JSONObject picture_reponse = myResponse.getJSONObject("picture");
			JSONObject data_response = picture_reponse.getJSONObject("data");
			System.out.println("URL : " + data_response.getString("url"));
			mb.setMemberImage(data_response.getString("url"));

		} catch (JSONException e) {
			e.printStackTrace();
		}
		msi.updateMemInfo(mb);
//		msi.saveMember(mb);
		session.setAttribute("FB", access_token);
		session.setAttribute("LoginOK", mb);
		System.out.println("LoginOK");
		response.sendRedirect("createAccount.jsp");
		return;
	}

}
