package createAccount.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import createAccount.SendEmail;
import createAccount.VeriCode;
import createAccount.model.CodeBean;
import createAccount.service.CodeServiceImpl;

@WebServlet("/createAccount/emailServlet.do")
public class EmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// 產生驗證碼
	VeriCode createCode = new VeriCode();
	String code;
	String email;
	String name;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		// 與client連線＆取得輸入的email
		email = request.getParameter("memberEmail");
		name = request.getParameter("memberName");

		// 檢查Email是否為空值
		if (email != null && email.trim().length() != 0) {
			CodeServiceImpl service = new CodeServiceImpl();
//			 檢查email是否已有驗證碼, 不重複產生驗證碼
			if (service.emailExists(email)) {
				code = service.queryCode(email);
			} else {
				// Email沒有舊的驗證碼，產生新的＆存入資料庫
				code = createCode.verifyCode();
				CodeBean cb = new CodeBean(null, email, code);
				service.saveCode(cb);
			}
			
			//沒有輸入姓名，用New Member取代
			if (name == null || name.trim().length() == 0) {
				name = "New Member";
			} else {
				name = request.getParameter("memberName");
			}
			
			// 發送email
			SendEmail send = new SendEmail();
			send.sendEmail(email, name, code);

			//發送response回前端
			PrintWriter out = response.getWriter();
			out.print("已傳送驗證碼至:" + email);
			out.flush();
			out.close();
		} else {
			System.out.println("empty");
			PrintWriter out = response.getWriter();
			out.print("Email不可空白");
			out.flush();
			out.close();
		}

	}

}