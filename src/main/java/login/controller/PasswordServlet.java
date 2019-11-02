package login.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.RandomStringUtils;

import createAccount.SendEmail;
import createAccount.model.EncrypAES;
import createAccount.model.MemberBean;
import createAccount.service.MemberServiceImpl;
import login.service.LoginServiceImpl;

@WebServlet("/login/passwordServlet.do")
public class PasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	String email;
	String newPassword;
	String encodedPassword;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		// 與client連線＆取得輸入的email
		email = request.getParameter("Email1forPwd");

		MemberBean mb = null;
		MemberServiceImpl msi = new MemberServiceImpl();
		LoginServiceImpl lsi = new LoginServiceImpl();

//		 檢查email是否已存在
		if (msi.emailExists(email)) {
			encodedPassword = lsi.queryPassword(email);
			mb = lsi.queryMB(email);
			String newPassword = RandomStringUtils.randomAlphanumeric(8, 12);

			SendEmail send = new SendEmail();
			send.sendPassword(email, newPassword, encodedPassword);

			PrintWriter out = response.getWriter();
			out.print("密碼已發送至: " + email);
			out.flush();
			out.close();

			HttpSession session = request.getSession();
			mb.setPassword(EncrypAES.getMD5Endocing(EncrypAES.encryptString(newPassword)));
			msi.updateMemInfo(mb);
			session.setAttribute("LoginOK", mb);
			System.out.println("密碼已更新至DB");

		} else {
			PrintWriter out = response.getWriter();
			out.print("Email不存在");
			out.flush();
			out.close();
		}

	}

}