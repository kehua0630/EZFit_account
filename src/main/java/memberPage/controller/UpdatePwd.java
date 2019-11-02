package memberPage.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import createAccount.model.EncrypAES;
import createAccount.model.MemberBean;
import createAccount.service.MemberServiceImpl;
import login.service.LoginServiceImpl;

@WebServlet("/memberPage/updatePwd.do")
public class UpdatePwd extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("post");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		// 先取出session物件
		HttpSession session = request.getSession(false);

		// 如果session物件不存在
		if (session == null) {
			// 請使用者登入
			response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/login/login.jsp"));
			return;
		}
		// 登入成功後，Session範圍內才會有LoginOK對應的MemberBean物件
		MemberBean mb = (MemberBean) session.getAttribute("LoginOK");
		// 取出使用者的memberId，後面的Cookie會用到
		Integer id = mb.getPkey();
		String email = mb.getEmail();
		String name = mb.getName();
		String gender = mb.getGender();
		Double height = mb.getHeight();
		Double weight = mb.getWeight();
		java.sql.Date birthday = mb.getBirthday();
		String address = mb.getAddress();
		String tel = mb.getTel();
		String img = mb.getMemberImage();

		String password = request.getParameter("oldPwd");
		String newPassword = request.getParameter("newPwd");
		String newPasswordCheck = request.getParameter("newPwdCheck");

		password = EncrypAES.getMD5Endocing(EncrypAES.encryptString(password));

		Pattern pattern = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,12})");
		Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		Matcher matcher = null;

		// 定義存放錯誤訊息的Map物件
		Map<String, String> errorMsgMap = new HashMap<String, String>();

		// 將errorMsgMap放入request物件內，識別字串為 "ErrorMsgKey"
		request.setAttribute("ErrorMsgKey", errorMsgMap);

		LoginServiceImpl lsi = new LoginServiceImpl();

		// 舊密碼錯誤
		if (!password.equals(lsi.queryPassword(email))) {
			errorMsgMap.put("ErrorOldPwd", "密碼錯誤");
		}

		// 新密碼格式錯誤
		matcher = pattern.matcher(newPassword);
		if (!matcher.matches()) {
			errorMsgMap.put("ErrorFormat", "格式錯誤");

		}

		// 新密碼＆確認不符
		if (!newPassword.equals(newPasswordCheck)) {
			errorMsgMap.put("ErrorNewPwd", "密碼不相符");

		}

		if (errorMsgMap.isEmpty()) {
			// update
			newPassword = EncrypAES.getMD5Endocing(EncrypAES.encryptString(newPassword));

			mb = new MemberBean(id, email, newPassword, name, gender, height, weight, birthday, address, tel, img, ts);
			MemberServiceImpl msi = new MemberServiceImpl();
			msi.updateMemInfo(mb);
			System.out.println(EncrypAES.decryptString(newPassword));

//			response.sendRedirect("memberPage.jsp");
			PrintWriter out = response.getWriter();
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Password updated successfully!');");
			out.println("location='memberPage.jsp';");
			out.println("</script>");
			out.flush();
			out.close();
			return;

		} else {
			RequestDispatcher rd = request.getRequestDispatcher("/memberPage/memberPage.jsp");
			rd.forward(request, response);
			return;
		}
	}

}
