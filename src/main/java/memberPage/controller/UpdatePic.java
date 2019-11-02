package memberPage.controller;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import createAccount.model.MemberBean;
import createAccount.service.MemberServiceImpl;

@WebServlet("/memberPage/updatePic.do")
public class UpdatePic extends HttpServlet {
	public static String filename = null;
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		System.out.println("post");
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
		String password = mb.getPassword();
		System.out.println(id);

		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		factory.setSizeThreshold(1024 * 1024);
		List items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			if (!item.isFormField()) {
				// 根據時間戳建立頭像檔案
				filename = id.toString() + ".jpg";
				File f = new File(getServletContext().getRealPath("MemberImage"));
//				File f = new File("D://image");
				if (!f.exists()) {
					f.mkdir();
				}
				String imgsrc = f + "/" + filename;
				System.out.println(imgsrc);
				// 複製檔案
				InputStream is = item.getInputStream();
				FileOutputStream fos = new FileOutputStream(imgsrc);
				byte b[] = new byte[1024 * 1024];
				int length = 0;
				while (-1 != (length = is.read(b))) {
					fos.write(b, 0, length);
				}

				Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
				MemberBean mb1 = new MemberBean(id, email, password, name, null, null, null, null, null, null, imgsrc,
						ts);
				MemberServiceImpl msi = new MemberServiceImpl();
				msi.updateMemInfo(mb1);
				session.setAttribute("mempic", imgsrc);

				fos.flush();
				fos.close();
			} else {
				System.out.println(item.getFieldName());
				String value = item.getString();
				value = new String(value.getBytes("ISO-8859-1"), "UTF-8");
				System.out.println(value);
			}
		}
		// request.setAttribute("filename", filename);
		// request.getRequestDispatcher("/showPhoto").forward(request, response);
//		response.sendRedirect("/ServletTest/showPhoto?filename=" + filename);
		response.sendRedirect("memberPage.jsp");
	}

}
