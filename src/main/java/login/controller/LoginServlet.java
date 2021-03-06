package login.controller;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import createAccount.model.EncrypAES;
import createAccount.model.MemberBean;
import login.service.LoginServiceImpl;

@WebServlet("/login/loginServlet.do")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = -6506682026701304964L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		// 定義存放錯誤訊息的Map物件
		Map<String, String> errorMsgMap = new HashMap<String, String>();

		// for verify bug
		Map<String, String> verifybug = new HashMap<String, String>();

		// 將errorMsgMap放入request物件內，識別字串為 "ErrorMsgKey"
		request.setAttribute("ErrorMsgKey", errorMsgMap);
		// for verify bug
		request.setAttribute("verifybug", verifybug);

		// 1. 讀取使用者輸入資料
		String email = request.getParameter("memberEmail");
		String password = request.getParameter("memberPassword");
		String rm = request.getParameter("rememberMe");
		String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
		System.out.println(gRecaptchaResponse);
		boolean verify = VerifyRecaptcha.verify(gRecaptchaResponse);
//		String requestURI = (String) session.getAttribute("requestURI");
		// 2. 進行必要的資料轉換
		// 無
		// 3. 檢查使用者輸入資料
		// 如果 userId 欄位為空白，放一個錯誤訊息到 errorMsgMap 之內
		if (email == null || email.trim().length() == 0) {
			errorMsgMap.put("AccountEmptyError", "帳號欄必須輸入");
		}
		// 如果 password 欄位為空白，放一個錯誤訊息到 errorMsgMap 之內
		if (password == null || password.trim().length() == 0) {
			errorMsgMap.put("PasswordEmptyError", "密碼欄必須輸入");
		}
		System.out.println(verify);
		if (verify == false) {
			verifybug.put("verifybug", "請勾選機器人");
		}

		// 如果 errorMsgMap 不是空的，表示有錯誤，交棒給login.jsp
		if (!errorMsgMap.isEmpty()) {
			RequestDispatcher rd = request.getRequestDispatcher("/login/login.jsp");
			rd.forward(request, response);
			return;
		}

		// **********Remember Me****************************
		Cookie cookieUser = null;
		Cookie cookiePassword = null;
		Cookie cookieRememberMe = null;
		// rm存放瀏覽器送來之RememberMe的選項，如果使用者對RememberMe打勾，rm就不會是null
		if (rm != null) {
			cookieUser = new Cookie("email", email);
			cookieUser.setMaxAge(7 * 24 * 60 * 60); // Cookie的存活期: 七天
			cookieUser.setPath(request.getContextPath());

			String encodePassword = EncrypAES.encryptString(password);
			cookiePassword = new Cookie("password", encodePassword);
			cookiePassword.setMaxAge(7 * 24 * 60 * 60);
			cookiePassword.setPath(request.getContextPath());

			cookieRememberMe = new Cookie("rm", "true");
			cookieRememberMe.setMaxAge(7 * 24 * 60 * 60);
			cookieRememberMe.setPath(request.getContextPath());
		} else { // 使用者沒有對 RememberMe 打勾
			cookieUser = new Cookie("email", email);
			cookieUser.setMaxAge(0); // MaxAge==0 表示要請瀏覽器刪除此Cookie
			cookieUser.setPath(request.getContextPath());

			String encodePassword = EncrypAES.encryptString(password);
			cookiePassword = new Cookie("password", encodePassword);
			cookiePassword.setMaxAge(0);
			cookiePassword.setPath(request.getContextPath());

			cookieRememberMe = new Cookie("rm", "true");
			cookieRememberMe.setMaxAge(0);
			cookieRememberMe.setPath(request.getContextPath());
		}
		response.addCookie(cookieUser);
		response.addCookie(cookiePassword);
		response.addCookie(cookieRememberMe);
		// ********************************************

		// 4. 進行 Business Logic 運算
		// 將LoginServiceImpl類別new為物件，存放物件參考的變數為 loginService
		LoginServiceImpl loginService = new LoginServiceImpl();

		// 將密碼加密兩次，以便與存放在表格內的密碼比對
		password = EncrypAES.getMD5Endocing(EncrypAES.encryptString(password));
		MemberBean mb = null;
		try {
			// 呼叫 loginService物件的 checkIDPassword()，傳入userid與password兩個參數
			mb = loginService.checkIDPassword(email, password);
			
			// OK, 登入成功, 將mb物件放入Session範圍內，識別字串為"LoginOK"

//			if (mb != null && verify) {
			if (mb != null) {
				session.setAttribute("LoginOK", mb);
				session.setAttribute("Id", mb.getPkey());
				response.sendRedirect("../index.jsp");
				return;
			} else {
				System.out.println(verify);
				// NG, 登入失敗, userid與密碼的組合錯誤，放相關的錯誤訊息到 errorMsgMap 之內
				errorMsgMap.put("LoginError", "該帳號不存在或密碼錯誤");
				RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
				rd.forward(request, response);
				return;
			}

		} catch (

		RuntimeException ex) {
			errorMsgMap.put("LoginError", ex.getMessage());
		}

		// 5.依照 Business Logic 運算結果來挑選適當的畫面
		// 如果 errorMsgMap 是空的，表示沒有任何錯誤，交棒給下一棒
//		if (errorMsgMap.isEmpty() && verify) {
//			if (requestURI != null) {
//				System.out.println("requestURI=" + requestURI + "*");
//				requestURI = (requestURI.length() == 0 ? request.getContextPath() : requestURI);
//				response.sendRedirect("indext.jsp");
////				response.sendRedirect(response.encodeRedirectURL(requestURI));
//				return;
//			} else {
//				response.sendRedirect(response.encodeRedirectURL(request.getContextPath()));
//				return;
//			}
//		} else {
//			// 如果errorMsgMap不是空的，表示有錯誤，交棒給login.jsp
//			RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
//			rd.forward(request, response);
//			return;
//		}
	}
}
