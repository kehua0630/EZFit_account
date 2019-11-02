package memberPage.controller;

import java.io.IOException;
import java.util.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import createAccount.model.MemberBean;
import createAccount.service.MemberServiceImpl;

@WebServlet("/memberPage/updateInfo.do")
public class UpdateMemInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");

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
		Integer id = mb.getPkey();
		String email = mb.getEmail();
		String name = mb.getName();
		String password = mb.getPassword();
		String gender = mb.getGender();
		Double height = mb.getHeight();
		Double weight = mb.getWeight();
		java.sql.Date birthday = mb.getBirthday();
		String address = mb.getAddress();
		String tel = mb.getTel();
		String img = mb.getMemberImage();
		Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());

		name = request.getParameter("memberName");
		gender = request.getParameter("memberSex");
		height = Double.parseDouble(request.getParameter("memberHeight"));
		weight = Double.parseDouble(request.getParameter("memberWeight"));
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
//		birthday = request.getParameter("memberBir");
		java.util.Date parsed;
		try {
			parsed = (Date) format.parse(request.getParameter("memberBir"));
			birthday = new java.sql.Date(parsed.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tel = request.getParameter("memberTel");
		address = request.getParameter("zipcode") + request.getParameter("county") + request.getParameter("district")
				+ request.getParameter("memberAddr");

		mb = new MemberBean(id, email, password, name, gender, height, weight, birthday, address, tel, img, ts);
		MemberServiceImpl msi = new MemberServiceImpl();
		msi.updateMemInfo(mb);
		session.setAttribute("LoginOK", mb);

		String[] bir = birthday.toString().split("-");
		System.out.println(bir[1] + "/" + bir[2] + "/" + bir[0]);
		session.setAttribute("birthday", bir[1] + "/" + bir[2] + "/" + bir[0]);
		
		

		response.sendRedirect("memberPage.jsp");
	}

}
