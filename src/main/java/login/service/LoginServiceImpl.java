package login.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import createAccount.model.HibernateUtils;
import createAccount.model.MemberBean;
import createAccount.repository.MemberDaoImpl;

public class LoginServiceImpl {
	MemberDaoImpl dao;
	SessionFactory factory;

	public LoginServiceImpl() {
		dao = new MemberDaoImpl();
		factory = HibernateUtils.getSessionFactory();
	}

	public MemberBean checkIDPassword(String email, String password) {
		MemberBean mb = null;
		Session session = factory.getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			mb = dao.checkIDPassword(email, password);
			tx.commit();
		} catch (Exception ex) {
			if (tx != null)
				tx.rollback();
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		return mb;
	}

	public String queryPassword(String email) {
		String password;
		Session session = factory.getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			password = dao.queryPassword(email);
			tx.commit();
		} catch (Exception ex) {
			if (tx != null)
				tx.rollback();
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		return password;
	}
	
	public MemberBean queryMB(String email) {
		MemberBean mb;
		Session session = factory.getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			mb = dao.queryMB(email);
			tx.commit();
		} catch (Exception ex) {
			if (tx != null)
				tx.rollback();
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		return mb;
	}

}
